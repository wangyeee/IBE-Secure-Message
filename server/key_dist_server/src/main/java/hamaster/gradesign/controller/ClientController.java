package hamaster.gradesign.controller;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hamaster.gradesgin.ibe.IBECipherText;
import hamaster.gradesgin.ibe.IBEConstraints;
import hamaster.gradesgin.ibe.IBEPlainText;
import hamaster.gradesgin.ibe.core.IBEEngine;
import hamaster.gradesgin.ibe.io.SecureByteArrayInputStream;
import hamaster.gradesgin.ibe.io.SecureByteArrayOutputStream;
import hamaster.gradesign.client.Encoder;
import hamaster.gradesign.daemon.EJBClient;
import hamaster.gradesign.service.ClientService;
import hamaster.gradesign.service.impl.ClientManager;

@RestController
public class ClientController {

    private ClientService clientService;

    private Encoder base64;

    @Autowired
    public ClientController(ClientService clientService, @Qualifier("base64Encoder") Encoder base64) {
        this.clientService = requireNonNull(clientService);
        this.base64 = requireNonNull(base64);
    }

    /**
     * 请求格式：<br>
     * 操作名最多256字节<br>
     * IBE加密后会话密钥384字节<br>
     * 请求数据（AES_256_CBC加密）若干字节
     */
    @PostMapping("/client")
    public String processRequest(@RequestParam(value = "o", required = true) String operation,
            @RequestParam(value = "k", required = true) String sessionKey,
            @RequestParam(value = "p", required = true) String payload) {
        EJBClient system = EJBClient.getInstance();
        sessionKey = sessionKey.replace('*', '+');
        sessionKey = sessionKey.replace('-', '/');
        payload = payload.replace('*', '+');
        payload = payload.replace('-', '/');
        byte[] encSessKey = base64.decode(sessionKey);
        if (IBEConstraints.IBE_G_SIZE * 3 != encSessKey.length) {
            // TODO handle error
        }
        IBECipherText cipher = new IBECipherText();
        cipher.setUvw(encSessKey);
        cipher.setLength(48); // AES_256_CBC
        IBEPlainText plain = IBEEngine.decrypt(cipher, system.serverPrivateKey());
        byte[] sessKey = IBEPlainText.getSignificantBytes(plain);
        byte[] reqBody = null;
        byte[] key = new byte[32];
        byte[] iv = new byte[16];
        System.arraycopy(sessKey, 0, key, 0, 32);
        System.arraycopy(sessKey, 32, iv, 0, 16);
        ByteArrayInputStream in = new SecureByteArrayInputStream(base64.decode(payload));
        ByteArrayOutputStream buffer = new SecureByteArrayOutputStream(payload.length());
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
            //throw new IOException(e);
        } catch (InvalidAlgorithmParameterException e) {
        } catch (IllegalBlockSizeException e) {
            //throw new IOException(e);
        } catch (BadPaddingException e) {
            //throw new IOException(e);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Arrays.fill(key, (byte) 0);
            Arrays.fill(iv, (byte) 0);
            try {
                in.close();
                buffer.close();
            } catch (IOException e) {
            }
        }
        byte[] resp = null; // 处理请求
        try {
            Method method = clientService.getClass().getMethod(operation, byte[].class);
            resp = (byte[]) method.invoke(clientService, reqBody);
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
                return null;
            }
        }
        System.arraycopy(sessKey, 0, key, 0, 32);
        System.arraycopy(sessKey, 32, iv, 0, 16);
        byte[] crypt0 = null; // 将处理结果加密并返回
        try {
            Cipher cipher0 = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher0.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            crypt0 = cipher0.doFinal(resp);
            // TODO 只返回了处理后的数据
            return base64.encode(crypt0);
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchPaddingException e) {
        } catch (InvalidKeyException e) {
        } catch (InvalidAlgorithmParameterException e) {
        } catch (IllegalBlockSizeException e) {
        } catch (BadPaddingException e) {
        } finally {
            Arrays.fill(key, (byte) 0);
            Arrays.fill(iv, (byte) 0);
            Arrays.fill(resp, (byte) 0);
            Arrays.fill(crypt0, (byte) 0);
        }
        return null;
    }
}
