package hamaster.gradesgin.ibe;

import static hamaster.gradesign.ibe.util.Hex.bytesToInt;
import static hamaster.gradesign.ibe.util.Hex.intToByte;
import hamaster.gradesgin.util.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.PrivateKey;
import java.util.Arrays;

/**
 * 用户私钥
 * The private key
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
public class IBEPrivateKey implements Serializable, IBEConstraints, PrivateKey {
	private static final long serialVersionUID = 1569046919144264563L;

	/**
	 * 私钥rID部分
	 * rID part of private ley, length is 20 bytes;
	 */
	protected byte[] rID;

	/**
	 * 私钥hID部分
	 * hID part ot private key, length is 128 bytes.
	 */
	protected byte[] hID;

	/**
	 * 椭圆函数参数
	 * pairing
	 */
	private byte[] pairing;

	/**
	 * 私钥所有者身份 即对应公钥
	 * the public key, e.g. email address
	 */
	private String userString;

	public IBEPrivateKey() {
	}

	public byte[] getrID() {
		return rID;
	}

	public void setrID(byte[] rID) {
		this.rID = rID;
	}

	public byte[] gethID() {
		return hID;
	}

	public void sethID(byte[] hID) {
		this.hID = hID;
	}

	public String getUserString() {
		return userString;
	}

	public void setUserString(String userString) {
		this.userString = userString;
	}

	public byte[] getPairing() {
		return pairing;
	}

	public void setPairing(byte[] pairing) {
		this.pairing = pairing;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(hID);
		result = prime * result + Arrays.hashCode(pairing);
		result = prime * result + Arrays.hashCode(rID);
		result = prime * result + ((userString == null) ? 0 : userString.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof IBEPrivateKey))
			return false;
		IBEPrivateKey other = (IBEPrivateKey) obj;
		if (!Arrays.equals(hID, other.hID))
			return false;
		if (!Arrays.equals(pairing, other.pairing))
			return false;
		if (!Arrays.equals(rID, other.rID))
			return false;
		if (userString == null) {
			if (other.userString != null)
				return false;
		} else if (!userString.equals(other.userString))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IBEPrivateKey [rID=" + Arrays.toString(rID) + ", hID="
				+ Arrays.toString(hID) + ", pairing="
				+ Arrays.toString(pairing) + ", userString=" + userString + "]";
	}

	/*
	 * (non-Javadoc)
	 * @see java.security.Key#getAlgorithm()
	 */
	@Override
	public String getAlgorithm() {
		return "IBE";
	}

	/*
	 * (non-Javadoc)
	 * @see java.security.Key#getFormat()
	 */
	@Override
	public String getFormat() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.security.Key#getEncoded()
	 */
	@Override
	public byte[] getEncoded() {
		return null;
	}

	/**
	 * 写入顺序：<br>
	 * rID 20字节<br>
	 * hID 128字节<br>
	 * 用户字符串长度4字节<br>
	 * 用户字符串不定长度<br>
	 * 椭圆参数长度4字节<br>
	 * 椭圆参数不定长度
	 * Serialize the private key, this implementation writes key content directly to a file, keep it private!!!
	 * Sequence: rID - 20 bytes, hID - 128 bytes, length of public key - 4 bytes, public key, length of pairing - 4 bytes, pairing
	 * @see hamaster.gradesgin.ibe.IBEConstraints#writeExternal(java.io.OutputStream)
	 */
	@Override
	public void writeExternal(OutputStream out) throws IOException {
		byte[] encoded = getEncoded();
		if (encoded != null) {
			out.write(encoded);
			out.flush();
			return;
		}
		// 不编码，直接将私钥写入输出流
		// TODO encrypt the private key with a password.
		byte[] rBuffer = new byte[IBE_ZR_SIZE];
		Arrays.fill(rBuffer, (byte) 0);
		if (rID != null)
			System.arraycopy(rID, 0, rBuffer, 0, IBE_ZR_SIZE > rID.length ? rID.length : IBE_ZR_SIZE);
		out.write(rBuffer);
		byte[] hBuffer = new byte[IBE_G_SIZE];
		Arrays.fill(hBuffer, (byte) 0);
		if (hID != null)
			System.arraycopy(hID, 0, hBuffer, 0, IBE_G_SIZE > hID.length ? hID.length : IBE_G_SIZE);
		out.write(hBuffer);

		byte[] userBuffer = null;
		if (userString != null) {
			userBuffer = userString.getBytes(USER_STRING_ENCODING);
			out.write(intToByte(userBuffer.length));
			out.write(userBuffer);
		}
		byte[] pBuffer = new byte[IBE_G_SIZE * 4];
		Arrays.fill(pBuffer, (byte) 0);
		if (pairing != null) {
			System.arraycopy(pairing, 0, pBuffer, 0, pairing.length);
			out.write(intToByte(pairing.length));
		} else {
			out.write(intToByte(IBE_G_SIZE * 4));
		}
		out.write(pBuffer, 0, pairing == null ? pBuffer.length : pairing.length);
		out.flush();
		MemoryUtil.fastSecureBuffers(userBuffer, pBuffer);
		MemoryUtil.immediateSecureBuffers(rBuffer, hBuffer);
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesgin.ibe.IBEConstraints#readExternal(java.io.InputStream)
	 */
	@Override
	public void readExternal(InputStream in) throws IOException, ClassNotFoundException {
		// TODO 判断私钥是否是编码过的
		byte[] buffer = new byte[IBE_ZR_SIZE + IBE_G_SIZE];

		int keySize = in.read(buffer);
		if (keySize != buffer.length)
			throw new IOException("Not enough bytes for a PrivateKey");
		this.rID = new byte[IBE_ZR_SIZE];
		this.hID = new byte[IBE_G_SIZE];
		System.arraycopy(buffer, 0, rID, 0, IBE_ZR_SIZE);
		System.arraycopy(buffer, IBE_ZR_SIZE, hID, 0, IBE_G_SIZE);

		byte[] upTmp = new byte[4];
		Arrays.fill(upTmp, (byte) 0);
		if (4 != in.read(upTmp)) {
			throw new IOException("Not enough bytes for a PrivateKey");
		}
		int uSize = bytesToInt(upTmp);
		byte[] uBuffer = new byte[uSize];
		uSize = in.read(uBuffer);
		if (uSize != uBuffer.length)
			throw new IOException("Not enough bytes for a PrivateKey");
		this.userString = new String(uBuffer, USER_STRING_ENCODING);

		Arrays.fill(upTmp, (byte) 0);
		if (4 != in.read(upTmp)) {
			throw new IOException("Not enough bytes for a PrivateKey");
		}
		int pSize = bytesToInt(upTmp);
		byte[] pBuffer = new byte[pSize];
		pSize = in.read(pBuffer);
		if (pSize != pBuffer.length)
			throw new IOException("Not enough bytes for a PrivateKey pairing");
		this.pairing = null;
		if (pSize != IBE_G_SIZE * 4) {
			this.pairing = new byte[pSize];
			System.arraycopy(pBuffer, 0, pairing, 0, pSize);
		}
		MemoryUtil.fastSecureBuffers(pBuffer);
		MemoryUtil.immediateSecureBuffers(buffer);
	}
}
