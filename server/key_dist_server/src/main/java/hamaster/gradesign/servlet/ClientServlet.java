package hamaster.gradesign.servlet;

import hamaster.gradesgin.ibe.IBECipherText;
import hamaster.gradesgin.ibe.IBEConstraints;
import hamaster.gradesgin.ibe.IBEPlainText;
import hamaster.gradesgin.ibe.core.IBEEngine;
import hamaster.gradesgin.ibe.io.SecureByteArrayOutputStream;
import hamaster.gradesign.daemon.EJBClient;
import hamaster.gradesign.mgr.ClientManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 完成和客户端的交互 如分发密钥
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
@WebServlet("/client")
public class ClientServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private ClientManager clientManager;

	/*
     * (non-Javadoc)
     * @see HttpServlet#HttpServlet()
     */
    public ClientServlet() {
    }

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.clientManager = EJBClient.getInstance().getBean("clientManager", ClientManager.class);
	}

	/**
	 * 请求格式：<br>
	 * 操作名长度1字节<br>
	 * 操作名最多256字节<br>
	 * IBE加密后会话密钥384字节<br>
	 * 请求数据（AES_256_CBC加密）若干字节
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		EJBClient system = EJBClient.getInstance();
		OutputStream out = response.getOutputStream();
		InputStream in = request.getInputStream();
		int aLen = in.read();
		byte[] aData = new byte[aLen];	// 读取请求的操作
		if (aData.length != in.read(aData))
			return;
		String action = new String(aData);

		byte[] encSessKey = new byte[IBEConstraints.IBE_G_SIZE * 3];	// 读取IBE加密后的会话密钥
		if (encSessKey.length != in.read(encSessKey))
			return;
		IBECipherText cipher = new IBECipherText();
		cipher.setUvw(encSessKey);
		cipher.setLength(48);	// AES_256_CBC
		IBEPlainText plain = IBEEngine.decrypt(cipher, system.serverPrivateKey());
		byte[] sessKey = IBEPlainText.getSignificantBytes(plain);

		// 解密数据
		ByteArrayOutputStream buffer = new SecureByteArrayOutputStream(in.available() + 16);
		byte[] reqBody = null;
		byte[] key = new byte[32];
		byte[] iv = new byte[16];
		System.arraycopy(sessKey, 0, key, 0, 32);
		System.arraycopy(sessKey, 32, iv, 0, 16);
		try {
			int len;
			byte[] temp = new byte[16];
			Cipher cipher0 = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher0.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
			while ((len = in.read(temp)) > 0) {
				byte[] crypt = cipher0.update(temp, 0, len);
				buffer.write(crypt);
			}
			byte[] fin = cipher0.doFinal();
			buffer.write(fin);
			buffer.flush();
			reqBody = buffer.toByteArray();
		} catch (NoSuchAlgorithmException e) {
		} catch (NoSuchPaddingException e) {
		} catch (InvalidKeyException e) {
			throw new IOException(e);
		} catch (InvalidAlgorithmParameterException e) {
		} catch (IllegalBlockSizeException e) {
			throw new IOException(e);
		} catch (BadPaddingException e) {
			throw new IOException(e);
		} finally {
			Arrays.fill(key, (byte) 0);
			Arrays.fill(iv, (byte) 0);
			buffer.close();
		}

		byte[] resp = null;	// 处理请求
		try {
			Method method = clientManager.getClass().getMethod(action, byte[].class);
			resp = (byte[]) method.invoke(clientManager, reqBody);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			resp = new byte[1];
			resp[0] = ClientManager.ERR_UNKNOWN_OP;
			e.printStackTrace(); // TODO debug
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			resp = new byte[1];
			resp[0] = ClientManager.ERR_PROC_REQ;
			e.printStackTrace(); // TODO debug
		} finally {
			Arrays.fill(reqBody, (byte) 0);
			if (resp == null || resp.length < 1) {
				// TODO 无法处理请求
				return;
			}
		}

		System.arraycopy(sessKey, 0, key, 0, 32);
		System.arraycopy(sessKey, 32, iv, 0, 16);
		byte[] crypt0 = null;	// 将处理结果加密并返回
		try {
			Cipher cipher0 = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher0.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
			crypt0 = cipher0.doFinal(resp);
			// TODO 只返回了处理后的数据
			out.write(crypt0);
			out.flush();
		} catch (NoSuchAlgorithmException e) {
		} catch (NoSuchPaddingException e) {
		} catch (InvalidKeyException e) {
			throw new IOException(e);
		} catch (InvalidAlgorithmParameterException e) {
		} catch (IllegalBlockSizeException e) {
			throw new IOException(e);
		} catch (BadPaddingException e) {
			throw new IOException(e);
		} finally {
			Arrays.fill(key, (byte) 0);
			Arrays.fill(iv, (byte) 0);
			Arrays.fill(resp, (byte) 0);
			Arrays.fill(crypt0, (byte) 0);
			out.close();
		}
	}
}
