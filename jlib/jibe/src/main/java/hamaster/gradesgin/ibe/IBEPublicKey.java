package hamaster.gradesgin.ibe;

import static hamaster.gradesign.ibe.util.Hex.bytesToInt;
import static hamaster.gradesign.ibe.util.Hex.intToByte;
import hamaster.gradesgin.util.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.PublicKey;
import java.util.Arrays;

/**
 * IBE用户公钥
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 * @deprecated 由于身份即公钥 使用时用字符串代替这个类
 */
@Deprecated
public class IBEPublicKey implements IBEConstraints, Serializable, PublicKey {
    private static final long serialVersionUID = -4861524673840672351L;

    /**
     * 用户ID的摘要值（PBC hash）
     */
    private byte[] user;

    /**
     * 用户ID 即实际使用的公钥
     */
    private String userString;

    public IBEPublicKey() {
    }

    public byte[] getUser() {
        return user;
    }

    public void setUser(byte[] user) {
        this.user = user;
    }

    public String getUserString() {
        return userString;
    }

    public void setUserString(String userString) {
        this.userString = userString;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(user);
        result = prime * result
                 + ((userString == null) ? 0 : userString.hashCode());
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
        if (!(obj instanceof IBEPublicKey))
            return false;
        IBEPublicKey other = (IBEPublicKey) obj;
        if (!Arrays.equals(user, other.user))
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
        return "IBEPublicKey [user=" + Arrays.toString(user) + ", userString="
               + userString + "]";
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
     * 序列化字段：<br>
     * 用户ID摘要 20字节<br>
     * 用户ID长度 4字节<br>
     * 用户ID字符串
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
        // 不编码，直接将公钥写入输出流
        byte[] userBuffer = new byte[IBE_ZR_SIZE];
        Arrays.fill(userBuffer, (byte) 0);
        if (user != null)
            System.arraycopy(user, 0, userBuffer, 0, IBE_ZR_SIZE > user.length ? user.length : IBE_ZR_SIZE);
        out.write(userBuffer);
        int keySize = userString == null ? 0 : userString.getBytes(USER_STRING_ENCODING).length;
        out.write(intToByte(keySize));
        byte[] strBuffer = null;
        if (userString != null) {
            strBuffer = userString.getBytes(USER_STRING_ENCODING);
            out.write(strBuffer);
        }
        out.flush();
        if (strBuffer != null)
            MemoryUtil.fastSecureBuffers(userBuffer, strBuffer);
        else
            MemoryUtil.fastSecureBuffers(userBuffer);
    }

    /*
     * (non-Javadoc)
     * @see hamaster.gradesgin.ibe.IBEConstraints#readExternal(java.io.InputStream)
     */
    @Override
    public void readExternal(InputStream in) throws IOException, ClassNotFoundException {
        byte[] buffer = new byte[IBE_ZR_SIZE];
        int keySize = in.read(buffer);
        if (keySize != buffer.length)
            throw new IOException("Not enough bytes for a PublicKey");
        this.user = new byte[IBE_ZR_SIZE];
        System.arraycopy(buffer, 0, user, 0, IBE_ZR_SIZE);
        byte[] kTmp = new byte[4];
        Arrays.fill(kTmp, (byte) 0);
        if (4 != in.read(kTmp)) {
            throw new IOException("Not enough bytes for a PublicKey");
        }
        keySize = bytesToInt(kTmp);
        byte[] kBuffer = new byte[keySize];
        keySize = in.read(kBuffer);
        if (keySize != kBuffer.length)
            throw new IOException("Not enough bytes for a PublicKey");
        this.userString = new String(kBuffer, USER_STRING_ENCODING);
        MemoryUtil.fastSecureBuffers(buffer);
    }
}
