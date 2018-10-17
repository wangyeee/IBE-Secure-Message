package hamaster.gradesgin.ibs;

import static hamaster.gradesgin.util.Hex.bytesToInt;
import static hamaster.gradesgin.util.Hex.bytesToLong;
import static hamaster.gradesgin.util.Hex.intToByte;
import static hamaster.gradesgin.util.Hex.longToBytes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import hamaster.gradesgin.ibe.IBEPublicParameter;
import hamaster.gradesgin.ibe.IBESystemParameter;
import hamaster.gradesgin.util.MemoryUtil;

/**
 * IBS数字证书对象
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
public class IBSCertificate extends IBESystemParameter implements Serializable {
    private static final long serialVersionUID = 1203770296311146389L;

    /**
     * 所有者
     */
    protected String ownerString;

    /**
     * 证书签名
     */
    protected IBSSignature signature;

    /**
     * 有效期开始日期
     */
    protected Date noEarlyThan;

    /**
     * 有效期结束日期
     */
    protected Date noLateThan;

    public IBSCertificate() {
    }

    public String getOwnerString() {
        return ownerString;
    }

    public void setOwnerString(String ownerString) {
        this.ownerString = ownerString;
    }

    public IBSSignature getSignature() {
        return signature;
    }

    public void setSignature(IBSSignature signature) {
        this.signature = signature;
    }

    public Date getNoEarlyThan() {
        return noEarlyThan;
    }

    public void setNoEarlyThan(Date noEarlyThan) {
        this.noEarlyThan = noEarlyThan;
    }

    public Date getNoLateThan() {
        return noLateThan;
    }

    public void setNoLateThan(Date noLateThan) {
        this.noLateThan = noLateThan;
    }

    /*
     * (non-Javadoc)
     * @see hamaster.gradesgin.ibe.IBESystemParameter#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                 + ((noEarlyThan == null) ? 0 : noEarlyThan.hashCode());
        result = prime * result
                 + ((noLateThan == null) ? 0 : noLateThan.hashCode());
        result = prime * result
                 + ((ownerString == null) ? 0 : ownerString.hashCode());
        result = prime * result
                 + ((signature == null) ? 0 : signature.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * @see hamaster.gradesgin.ibe.IBESystemParameter#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof IBSCertificate))
            return false;
        IBSCertificate other = (IBSCertificate) obj;
        if (noEarlyThan == null) {
            if (other.noEarlyThan != null)
                return false;
        } else if (!noEarlyThan.equals(other.noEarlyThan))
            return false;
        if (noLateThan == null) {
            if (other.noLateThan != null)
                return false;
        } else if (!noLateThan.equals(other.noLateThan))
            return false;
        if (ownerString == null) {
            if (other.ownerString != null)
                return false;
        } else if (!ownerString.equals(other.ownerString))
            return false;
        if (signature == null) {
            if (other.signature != null)
                return false;
        } else if (!signature.equals(other.signature))
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * @see hamaster.gradesgin.ibe.IBESystemParameter#toString()
     */
    @Override
    public String toString() {
        return "IBSCertificate [ownerString=" + ownerString + ", signature="
               + signature + ", noEarlyThan=" + noEarlyThan + ", noLateThan="
               + noLateThan + ", publicParameter=" + publicParameter
               + ", masterKey=" + Arrays.toString(masterKey) + "]";
    }

    /**
     * 序列化字段：<br>
     * 公共参数<br>
     * 签名主密钥 20字节<br>
     * 证书有效期开始 8字节<br>
     * 证书有效期 8字节<br>
     * 所有者字符串长度 4字节<br>
     * 所有者字符串 长度由前4字节定义<br>
     * 根证书签名
     * @see hamaster.gradesgin.ibe.IBEConstraints#writeExternal(java.io.OutputStream)
     */
    @Override
    public void writeExternal(OutputStream out) throws IOException {
        publicParameter.writeExternal(out);
        byte[] mBuffer = new byte[IBE_ZR_SIZE];
        Arrays.fill(mBuffer, (byte) 0);
        if (masterKey != null)
            System.arraycopy(masterKey, 0, mBuffer, 0, IBE_ZR_SIZE);
        out.write(mBuffer);
        long start = noEarlyThan == null ? 0L : noEarlyThan.getTime();
        long end = noLateThan == null ? 0L : noLateThan.getTime();
        out.write(longToBytes(start));
        out.write(longToBytes(end - start));
        int oSize = ownerString != null ? ownerString.length() : 10;
        out.write(intToByte(oSize));
        out.write(ownerString.getBytes(USER_STRING_ENCODING));
        if (signature != null) {
            out.write((byte) 1);
            signature.writeExternal(out);
        } else {
            out.write((byte) 0);
        }
        out.flush();
        MemoryUtil.immediateSecureBuffers(mBuffer);
    }

    /*
     * (non-Javadoc)
     * @see hamaster.gradesgin.ibe.IBEConstraints#readExternal(java.io.InputStream)
     */
    @Override
    public void readExternal(InputStream in) throws IOException, ClassNotFoundException {
        this.publicParameter = new IBEPublicParameter();
        publicParameter.readExternal(in);
        this.masterKey = new byte[IBE_ZR_SIZE];
        int size = in.read(masterKey);
        if (size != masterKey.length)
            throw new IOException("Not enough bytes for a Certificate");
        byte[] spTmp = new byte[8];
        Arrays.fill(spTmp, (byte) 0);
        if (8 != in.read(spTmp))
            throw new IOException("Not enough bytes for a Certificate");
        long start = bytesToLong(spTmp);
        Arrays.fill(spTmp, (byte) 0);
        if (8 != in.read(spTmp))
            throw new IOException("Not enough bytes for a Certificate");
        long period = bytesToLong(spTmp);
        this.noEarlyThan = new Date(start);
        this.noLateThan = new Date(start + period);
        byte[] sTmp = new byte[4];
        Arrays.fill(sTmp, (byte) 0);
        if (4 != in.read(sTmp))
            throw new IOException("Not enough bytes for a Certificate");
        size = bytesToInt(sTmp);
        byte[] buffer = new byte[size];
        size = in.read(buffer);
        if (size != buffer.length)
            throw new IOException("Not enough bytes for a Certificate");
        this.ownerString = new String(buffer, 0, size, USER_STRING_ENCODING);
        size = in.read();
        if (size == 1) {
            this.signature = new IBSSignature();
            signature.readExternal(in);
        }
    }
}
