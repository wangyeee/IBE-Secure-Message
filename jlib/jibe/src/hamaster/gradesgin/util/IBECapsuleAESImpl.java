package hamaster.gradesgin.util;

import static hamaster.gradesign.ibe.util.Hex.bytesToInt;
import static hamaster.gradesign.ibe.util.Hex.intToByte;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
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

/**
 * IBECapsule的AES实现<br>
 * 加密方式 AES256_CBC_PKCS5Padding<br>
 * 密钥哈希函数 SHA-512<br>
 * 这个实现不是线程安全的
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
public class IBECapsuleAESImpl implements IBECapsule {
	private static final long serialVersionUID = 7608124228314005862L;

	private final static String HASH_ALGORITHM = "SHA-512";
	private final static String CRYPTO_ALGORITHM = "AES/CBC/PKCS5Padding";

	/**
	 * 明文数据
	 */
	private transient byte[] data;

	/**
	 * 加密密钥
	 */
	private transient byte[] key;

	/**
	 * 密文数据
	 */
	private transient byte[] cipherText;

	/**
	 * 密钥的SHA-512摘要
	 */
	private transient byte[] keyHash;

	public IBECapsuleAESImpl() {
	}

	/**
	 * 持久化字段格式<br>
	 * 加密算法名称（长度一字节 名称最多256字节）<br>
	 * 哈希算法名称（长度一字节 名称最多256字节）<br>
	 * 哈希值 长度由算法确定<br>
	 * 数据有效长度 4字节<br>
	 * 加密数据长度 4字节<br>
	 * 加密数据
	 * @see hamaster.gradesgin.ibe.IBEConstraints#writeExternal(java.io.OutputStream)
	 */
	@Override
	public void writeExternal(OutputStream out) throws IOException {
		ensureNotNull(out, data, key);
		byte[] key = Hash.sha256(this.key);
		byte[] iv = Hash.md5(this.key);
		byte[] keyHash = Hash.sha512(this.key);
		byte[] crypt = null;
		try {
			Cipher cipher = Cipher.getInstance(CRYPTO_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
			crypt = cipher.doFinal(data);
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
		}
		if (crypt == null)
			throw new IOException("Cannot encrypt data!");
		byte cl = (byte) CRYPTO_ALGORITHM.length();
		out.write(cl);
		out.write(CRYPTO_ALGORITHM.getBytes());
		out.write((byte) HASH_ALGORITHM.length());
		out.write(HASH_ALGORITHM.getBytes());
		out.write(keyHash);
		out.write(intToByte(data.length));
		out.write(intToByte(crypt.length));
		out.write(crypt);
		out.flush();
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesgin.ibe.IBEConstraints#readExternal(java.io.InputStream)
	 */
	@Override
	public void readExternal(InputStream in) throws IOException, ClassNotFoundException {
		int cLength = in.read();
		byte[] cBuffer = new byte[cLength];
		cLength = in.read(cBuffer);
		if (cLength != cBuffer.length)
			throw new IOException("Not enough bytes!");
		int hLength = in.read();
		byte[] hnBuffer = new byte[hLength];
		hLength = in.read(hnBuffer);
		if (hLength != hnBuffer.length)
			throw new IOException("Not enough bytes!");
		this.keyHash = new byte[64];
		in.read(keyHash);
		byte[] tmp = new byte[4];
		int tLength = in.read(tmp);
		if (tLength != tmp.length)
			throw new IOException("Not enough bytes!");
		int dataLength = bytesToInt(tmp);
		tLength = in.read(tmp);
		if (tLength != tmp.length)
			throw new IOException("Not enough bytes!");
		int cipherLength = bytesToInt(tmp);
		data = new byte[dataLength];
		cipherText = new byte[cipherLength];
		cipherLength = in.read(cipherText);
		if (cipherLength != cipherText.length)
			throw new IOException("Not enough bytes!");
		if (this.key != null)
			decrypt();
	}

	private void decrypt() throws IOException {
		byte[] key = Hash.sha256(this.key);
		byte[] iv = Hash.md5(this.key);
		try {
			Cipher cipher = Cipher.getInstance(CRYPTO_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
			byte[] tmp = cipher.doFinal(cipherText);
			System.arraycopy(tmp, 0, data, 0, data.length);
			Arrays.fill(tmp, (byte) 0);
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
		}
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesgin.util.IBECapsule#protect(byte[])
	 */
	@Override
	public void protect(byte[] data) {
		ensureNotNull(data);
		this.data = data;
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesgin.util.IBECapsule#protect(java.io.Serializable)
	 */
	@Override
	public void protect(Serializable object) {
		ensureNotNull(object);
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(buffer);
			out.writeObject(object);
			out.flush();
			buffer.flush();
			this.data = buffer.toByteArray();
			buffer.reset();
			buffer.close();
			out.close();
		} catch (IOException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesgin.util.IBECapsule#getData()
	 */
	@Override
	public byte[] getData() {
		return data;
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesgin.util.IBECapsule#getDataAsObject()
	 */
	@Override
	public Object getDataAsObject() throws ClassNotFoundException {
		try {
			ByteArrayInputStream buffer = new ByteArrayInputStream(getData());
			ObjectInputStream in = new ObjectInputStream(buffer);
			Object obj = in.readObject();
			in.close();
			buffer.reset();
			buffer.close();
			return obj;
		} catch (IOException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesgin.util.IBECapsule#setKey(byte[])
	 */
	@Override
	public void setKey(byte[] key) {
		ensureNotNull(key);
		this.key = key;
		if (this.cipherText != null) {
			byte [] exc = Hash.sha512(key);
			boolean eq = Arrays.equals(exc, keyHash);
			if (!eq)
				throw new IllegalArgumentException("Invalid key");
			try {
				decrypt();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesgin.util.IBECapsule#getHashAlgorithm()
	 */
	@Override
	public String getHashAlgorithm() {
		return HASH_ALGORITHM;
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesgin.util.IBECapsule#getCrypto()
	 */
	@Override
	public String getCrypto() {
		return CRYPTO_ALGORITHM;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		if (data != null)
			Arrays.fill(data, (byte) 0);
		if (key != null)
			Arrays.fill(key, (byte) 0);
		if (cipherText != null)
			Arrays.fill(cipherText, (byte) 0);
		if (keyHash != null)
			Arrays.fill(keyHash, (byte) 0);
		super.finalize();
	}

	private void ensureNotNull(Object ... objs) throws NullPointerException {
		if (objs == null)
			throw new NullPointerException();
		for (Object obj : objs) {
			if (obj == null)
				throw new NullPointerException();
		}
	}
}
