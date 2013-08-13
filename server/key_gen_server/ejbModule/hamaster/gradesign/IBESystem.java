package hamaster.gradesign;

import hamaster.gradesgin.ibe.IBESystemParameter;
import hamaster.gradesgin.ibs.IBSCertificate;

import java.io.Serializable;

/**
 * 一个独立的IBE系统 具有自己的主密钥和证书
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
public class IBESystem implements Serializable, SecureConstraints {
	private static final long serialVersionUID = 5143632174879783267L;

	/**
	 * 系统公共参数和主密钥
	 */
	private IBESystemParameter parameter;

	/**
	 * 系统签名证书
	 */
	private IBSCertificate certificate;

	/**
	 * 加密用的密钥
	 */
	private transient byte[] cryptionKeyAndIV;

	public IBESystem() {
	}

	public IBESystemParameter getParameter() {
		return parameter;
	}

	public void setParameter(IBESystemParameter parameter) {
		this.parameter = parameter;
	}

	public IBSCertificate getCertificate() {
		return certificate;
	}

	public void setCertificate(IBSCertificate certificate) {
		this.certificate = certificate;
	}

	public byte[] getCryptionKeyAndIV() {
		return cryptionKeyAndIV;
	}

	public void setCryptionKeyAndIV(byte[] cryptionKeyAndIV) {
		if (cryptionKeyAndIV == null || cryptionKeyAndIV.length != KEY_LENGTH_IN_BYTES + IV_LENGTH_IN_BYTES)
			throw new IllegalArgumentException("cryptionKeyAndIV must be 48 bytes!");
		this.cryptionKeyAndIV = cryptionKeyAndIV;
	}

//	public final static void writeEncryptedObjectsToExternal(ObjectOutput out, byte[] cryptionKeyAndIV, Serializable ... objs) throws IOException {
//		byte[] key = new byte[KEY_LENGTH_IN_BYTES];
//		byte[] iv = new byte[IV_LENGTH_IN_BYTES];
//		System.arraycopy(cryptionKeyAndIV, 0, key, 0, KEY_LENGTH_IN_BYTES);
//		System.arraycopy(cryptionKeyAndIV, KEY_LENGTH_IN_BYTES, iv, 0, IV_LENGTH_IN_BYTES);
//		try {
//			Cipher cipher = Cipher.getInstance(DATABASE_CRYPTION_ALGORITHM);
//			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
//			
//			ByteArrayOutputStream bout = new ByteArrayOutputStream();
//			ObjectOutputStream oout = new ObjectOutputStream(bout);
//			for (Serializable obj : objs)
//				oout.writeObject(obj);
//			oout.flush();
//			bout.flush();
//			byte[] secret = bout.toByteArray();
//			oout.close();
//			bout.reset();
//			bout.close();
//			byte[] enc = cipher.doFinal(secret);
//			Arrays.fill(secret, (byte) 0);
//			out.write(enc);
//			out.flush();
//			Arrays.fill(enc, (byte) 0);
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		} catch (NoSuchPaddingException e) {
//			e.printStackTrace();
//		} catch (InvalidKeyException e) {
//			throw new IOException(e);
//		} catch (InvalidAlgorithmParameterException e) {
//			e.printStackTrace();
//		} catch (IllegalBlockSizeException e) {
//			throw new IOException(e);
//		} catch (BadPaddingException e) {
//			throw new IOException(e);
//		} finally {
//			Arrays.fill(key, (byte) 0);
//			Arrays.fill(iv, (byte) 0);
//		}
//	}

//	@Override
//	public void writeExternal(ObjectOutput out) throws IOException {
//		writeEncryptedObjectsToExternal(out, cryptionKeyAndIV, parameter, certificate);
//	}

//	@Override
//	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//		byte[] key = new byte[KEY_LENGTH_IN_BYTES];
//		byte[] iv = new byte[IV_LENGTH_IN_BYTES];
//		System.arraycopy(cryptionKeyAndIV, 0, key, 0, KEY_LENGTH_IN_BYTES);
//		System.arraycopy(cryptionKeyAndIV, KEY_LENGTH_IN_BYTES, iv, 0, IV_LENGTH_IN_BYTES);
//		try {
//			Cipher cipher = Cipher.getInstance(DATABASE_CRYPTION_ALGORITHM);
//			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
//			
//			byte[] buffer = new byte[16]; // AES Block Size
//			int size;
//			ByteArrayOutputStream tout = new ByteArrayOutputStream();
//			while ((size = in.read(buffer)) > 0) {
//				byte[] t = cipher.update(buffer, 0, size);
//				tout.write(t);
//			}
//			byte[] dec = cipher.doFinal();
//			tout.write(dec);
//			tout.flush();
//			byte[] dec0 = tout.toByteArray();
//			tout.reset();
//			tout.close();
//			Arrays.fill(buffer, (byte) 0);
//			ByteArrayInputStream bin = new ByteArrayInputStream(dec0);
//			ObjectInputStream oin = new ObjectInputStream(bin);
//			parameter = (IBESystemParameter) oin.readObject();
//			certificate = (IBSCertificate) oin.readObject();
//			oin.close();
//			bin.reset();
//			bin.close();
//			Arrays.fill(dec, (byte) 0);
//			Arrays.fill(dec0, (byte) 0);
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		} catch (NoSuchPaddingException e) {
//			e.printStackTrace();
//		} catch (InvalidKeyException e) {
//			throw new IOException(e);
//		} catch (InvalidAlgorithmParameterException e) {
//			e.printStackTrace();
//		} catch (IllegalBlockSizeException e) {
//			throw new IOException(e);
//		} catch (BadPaddingException e) {
//			throw new IOException(e);
//		}
//	}

//	public final static void writeEncryptedObjectsToExternal(ObjectOutput out, byte[] cryptionKeyAndIV, Serializable ... objs) throws IOException {
//	byte[] key = new byte[KEY_LENGTH_IN_BYTES];
//	byte[] iv = new byte[IV_LENGTH_IN_BYTES];
//	System.arraycopy(cryptionKeyAndIV, 0, key, 0, KEY_LENGTH_IN_BYTES);
//	System.arraycopy(cryptionKeyAndIV, KEY_LENGTH_IN_BYTES, iv, 0, IV_LENGTH_IN_BYTES);
//	try {
//		Cipher cipher = Cipher.getInstance(DATABASE_CRYPTION_ALGORITHM);
//		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
//		OutputStream writer;
//		if (out instanceof OutputStream) {
//			writer = (OutputStream) out;
//		} else {
//			final ObjectOutput out0 = out;
//			writer = new OutputStream() {
//				@Override
//				public void write(int b) throws IOException {
//					out0.write(b);
//				}
//			};
//		}
//		CipherOutputStream sec = new CipherOutputStream(writer, cipher);
//		ObjectOutputStream secOut = new ObjectOutputStream(sec);
//		for (Serializable obj : objs)
//			secOut.writeObject(obj);
//		secOut.flush();
//		secOut.close();
//		sec.flush();
//		sec.close();
//	} catch (NoSuchAlgorithmException e) {
//		e.printStackTrace();
//	} catch (NoSuchPaddingException e) {
//		e.printStackTrace();
//	} catch (InvalidKeyException e) {
//		throw new IOException(e);
//	} catch (InvalidAlgorithmParameterException e) {
//		e.printStackTrace();
//	}
//}

//	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//		byte[] key = new byte[KEY_LENGTH_IN_BYTES];
//		byte[] iv = new byte[IV_LENGTH_IN_BYTES];
//		System.arraycopy(cryptionKeyAndIV, 0, key, 0, KEY_LENGTH_IN_BYTES);
//		System.arraycopy(cryptionKeyAndIV, KEY_LENGTH_IN_BYTES, iv, 0, IV_LENGTH_IN_BYTES);
//		try {
//			Cipher cipher = Cipher.getInstance(DATABASE_CRYPTION_ALGORITHM);
//			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
//			InputStream reader;
//			if (in instanceof InputStream) {
//				reader = (InputStream) in;
//			} else {
//				final ObjectInput in0 = in;
//				reader = new InputStream() {
//					@Override
//					public int read() throws IOException {
//						return in0.read();
//					}
//				};
//			}
//			CipherInputStream sec = new CipherInputStream(reader, cipher);
//			ObjectInputStream secIn = new ObjectInputStream(sec);
//			parameter = (IBESystemParameter) secIn.readObject();
//			certificate = (IBSCertificate) secIn.readObject();
//			secIn.close();
//			sec.close();
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		} catch (NoSuchPaddingException e) {
//			e.printStackTrace();
//		} catch (InvalidKeyException e) {
//			throw new IOException(e);
//		} catch (InvalidAlgorithmParameterException e) {
//			e.printStackTrace();
//		}
//	}
}
