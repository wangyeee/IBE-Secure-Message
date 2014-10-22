package hamaster.gradesgin.ibe;

import hamaster.gradesgin.util.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * IBE加密后的密文
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
public class IBECipherText implements Serializable, IBEConstraints {
	private static final long serialVersionUID = 5766799743332486673L;

	/**
	 * 密文uvw对象序列 共384字节
	 * 384 bytes of (u, v, w)
	 */
	private byte[] uvw;

	/**
	 * 对应明文有效长度 单位字节
	 * used length of plain text.
	 * should this length also be encrypted???
	 */
	private int length;

	public IBECipherText() {
	}

	public byte[] getUvw() {
		return uvw;
	}

	public void setUvw(byte[] uvw) {
		this.uvw = uvw;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + length;
		result = prime * result + Arrays.hashCode(uvw);
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
		if (!(obj instanceof IBECipherText))
			return false;
		IBECipherText other = (IBECipherText) obj;
		if (length != other.length)
			return false;
		if (!Arrays.equals(uvw, other.uvw))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IBECipherText [uvw=" + Arrays.toString(uvw) + ", length="
				+ length + "]";
	}

	/**
	 * 序列化字段：<br/>
	 * 密文内容 384字节<br/>
	 * 明文有效长度 1字节<br/>
	 * Serialize this objetc, first 384 bytes are (u,v,w) and last byte is the length of plain text(1 to 126)
	 * @see hamaster.gradesgin.ibe.IBEConstraints#writeExternal(java.io.OutputStream)
	 */
	@Override
	public void writeExternal(OutputStream out) throws IOException {
		byte[] uvwBuffer = new byte[IBE_G_SIZE * 3];
		Arrays.fill(uvwBuffer, (byte) 0);
		if (uvw != null)
			System.arraycopy(uvw, 0, uvwBuffer, 0, IBE_G_SIZE * 3 > uvw.length ? uvw.length : IBE_G_SIZE * 3);
		out.write(uvwBuffer);
		out.write((byte) length);
		out.flush();
		MemoryUtil.fastSecureBuffers(uvwBuffer);
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesgin.ibe.IBEConstraints#readExternal(java.io.InputStream)
	 */
	@Override
	public void readExternal(InputStream in) throws IOException,
			ClassNotFoundException {
		byte[] buffer = new byte[IBE_G_SIZE * 3];
		int size = in.read(buffer);
		if (size != buffer.length)
			throw new IOException("Not enough bytes for a CipherText");
		this.uvw = new byte[IBE_G_SIZE * 3];
		System.arraycopy(buffer, 0, uvw, 0, IBE_G_SIZE * 3);
		this.length = in.read();
		MemoryUtil.fastSecureBuffers(buffer);
	}
}
