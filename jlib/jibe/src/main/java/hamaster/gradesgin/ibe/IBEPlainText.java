package hamaster.gradesgin.ibe;

import hamaster.gradesgin.util.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * 明文
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
public class IBEPlainText implements IBEConstraints, Serializable {
    private static final long serialVersionUID = -2705082103669151761L;

    /**
     * 明文内容 长度128字节
     * Length of plain text, max 128 bytes
     */
    private byte[] content;

    /**
     * 明文的有效长度 最大为126
     * Max length of mutable plain text, 126 bytes.
     * The first and 64th byte must be zero to avoid overflow.
     */
    private int length;

    public IBEPlainText() {
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(content);
        result = prime * result + length;
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
        if (!(obj instanceof IBEPlainText))
            return false;
        IBEPlainText other = (IBEPlainText) obj;
        if (!Arrays.equals(content, other.content))
            return false;
        if (length != other.length)
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "IBEPlainText [content=" + Arrays.toString(content)
               + ", length=" + getLength() + "]";
    }

    /**
     * 获取解密后明文中的有效内容
     * Get the 126 bytes content of decrypted plain text
     * @param plainText 要获取的明文
     * @return 包含有效内容的字节数组(Byte array containing the plain text)
     */
    public static byte[] getSignificantBytes(IBEPlainText plainText) {
        synchronized (plainText) {
            byte[] plain = new byte[plainText.getLength()];
            final int IBE_HALF = IBE_G_SIZE / 2 - 1;
            if (plainText.getLength() > IBE_HALF) {
                System.arraycopy(plainText.content, IBE_G_SIZE - plainText.getLength() - 1, plain, 0, plainText.getLength() - IBE_HALF);
                System.arraycopy(plainText.content, IBE_G_SIZE - IBE_HALF, plain, plainText.getLength() - IBE_HALF, IBE_HALF);
            } else {
                System.arraycopy(plainText.content, IBE_G_SIZE - plainText.getLength(), plain, 0, plainText.getLength());
            }
            return plain;
        }
    }

    /**
     * 通过特定长度的字节构建IBEPlainText对象
     * Construct an IBEPlainText object from certain length of bytes, the length must be from 1 to 126
     * @param significantBytes 有效字节 长度在1到126字节中间
     * @return IBEPlainText
     */
    public static IBEPlainText newIbePlainTextFormSignificantBytes(byte[] significantBytes) {
        IBEPlainText text = new IBEPlainText();
        if (significantBytes == null || significantBytes.length == 0 || significantBytes.length > IBE_G_SIZE - 2)
            throw new IllegalArgumentException("invalid plain text");
        int length = significantBytes.length;
        final int IBE_HALF = IBE_G_SIZE / 2 - 1;
        text.content = new byte[IBE_G_SIZE];
        Arrays.fill(text.content, (byte) 0);
        if (length > IBE_HALF) {
            System.arraycopy(significantBytes, 0, text.content, IBE_G_SIZE - length - 1, length - IBE_HALF);
            System.arraycopy(significantBytes, length - IBE_HALF, text.content, IBE_G_SIZE - IBE_HALF, IBE_HALF);
        } else {
            System.arraycopy(significantBytes, 0, text.content, IBE_G_SIZE - length, length);
        }
        text.setLength(length);
        return text;
    }

    /**
     * 序列化字段：<br>
     * 明文内容 128字节<br>
     * 明文有效长度 1字节<br>
     * Serialize this object, the first 128 bytes are padded plain text and the last byte is the length of byte used(1 to 126)
     * @see hamaster.gradesgin.ibe.IBEConstraints#writeExternal(java.io.OutputStream)
     */
    @Override
    public void writeExternal(OutputStream out) throws IOException {
        byte[] contentBuffer = new byte[IBE_G_SIZE];
        Arrays.fill(contentBuffer, (byte) 0);
        if (content != null)
            System.arraycopy(content, 0, contentBuffer, 0, IBE_G_SIZE > content.length ? content.length : IBE_G_SIZE);
        out.write(contentBuffer);
        out.write((byte) getLength());
        out.flush();
        MemoryUtil.immediateSecureBuffers(contentBuffer);
    }

    /*
     * (non-Javadoc)
     * @see hamaster.gradesgin.ibe.IBEConstraints#readExternal(java.io.InputStream)
     */
    @Override
    public void readExternal(InputStream in) throws IOException, ClassNotFoundException {
        byte[] buffer = new byte[IBE_G_SIZE];
        int size = in.read(buffer);
        if (size != buffer.length)
            throw new IOException("Not enough bytes for a PlainText");
        this.content = new byte[IBE_G_SIZE];
        System.arraycopy(buffer, 0, content, 0, IBE_G_SIZE);
        this.setLength(in.read());
        MemoryUtil.immediateSecureBuffers(buffer);
    }
}
