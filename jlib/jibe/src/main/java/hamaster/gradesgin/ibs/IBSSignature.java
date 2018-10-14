package hamaster.gradesgin.ibs;

import static hamaster.gradesign.ibe.util.Hex.bytesToInt;
import static hamaster.gradesign.ibe.util.Hex.bytesToLong;
import static hamaster.gradesign.ibe.util.Hex.intToByte;
import static hamaster.gradesign.ibe.util.Hex.longToBytes;
import hamaster.gradesgin.ibe.IBEConstraints;
import hamaster.gradesgin.ibe.IBEPrivateKey;
import hamaster.gradesgin.ibe.IBEPublicParameter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

/**
 * IBS数字签名
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
public class IBSSignature extends IBEPrivateKey implements Serializable, IBEConstraints {
    private static final long serialVersionUID = -4862591033031464057L;

    /**
     * 签名者参数
     */
    protected IBEPublicParameter signatureParameter;

    /**
     * 被签名数据的摘要
     */
    protected byte[] digest;

    /**
     * 摘要算法
     */
    protected String hashAlgorithm;

    /**
     * 签名日期
     */
    protected Date signingDate;

    public IBSSignature() {
    }

    public IBEPublicParameter getSignatureParameter() {
        return signatureParameter;
    }

    public void setSignatureParameter(IBEPublicParameter signatureParameter) {
        this.signatureParameter = signatureParameter;
        setPairing(signatureParameter.getPairing());
    }

    public byte[] getDigest() {
        return digest;
    }

    public void setDigest(byte[] digest) {
        this.digest = digest;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public Date getSigningDate() {
        return signingDate;
    }

    public void setSigningDate(Date signingDate) {
        this.signingDate = signingDate;
    }

    /**
     * 序列化字段：<br>
     * rID 20字节<br>
     * hID 128字节<br>
     * 摘要长度 4字节<br>
     * 摘要内容<br>
     * 签名参数<br>
     * 签名日期 8字节<br>
     * 哈希算法长度 1字节<br>
     * 哈希算法字符串
     * @see hamaster.gradesgin.ibe.IBEConstraints#writeExternal(java.io.OutputStream)
     */
    @Override
    public void writeExternal(OutputStream out) throws IOException {
        byte[] rBuffer = new byte[IBE_ZR_SIZE];
        byte[] hBuffer = new byte[IBE_G_SIZE];
        Arrays.fill(rBuffer, (byte) 0);
        Arrays.fill(hBuffer, (byte) 0);
        if (rID != null)
            System.arraycopy(rID, 0, rBuffer, 0, rID.length > IBE_ZR_SIZE ? IBE_ZR_SIZE : rID.length);
        out.write(rBuffer);
        if (hID != null)
            System.arraycopy(hID, 0, hBuffer, 0, hID.length > IBE_G_SIZE ? IBE_G_SIZE : hID.length);
        out.write(hBuffer);
        if (digest == null) {
            byte[] zero = new byte[4];
            Arrays.fill(zero, (byte) 0);
            out.write(zero);
        } else {
            out.write(intToByte(digest.length));
            out.write(digest);
        }
        signatureParameter.writeExternal(out);
        if (signingDate == null) {
            byte[] zero = new byte[8];
            Arrays.fill(zero, (byte) 0);
            out.write(zero);
        } else {
            out.write(longToBytes(signingDate.getTime()));
        }
        int aSize = hashAlgorithm == null ? 0 : hashAlgorithm.length();
        out.write((byte) aSize);
        out.write(hashAlgorithm.getBytes(USER_STRING_ENCODING));
        out.flush();
    }

    /*
     * (non-Javadoc)
     * @see hamaster.gradesgin.ibe.IBEConstraints#readExternal(java.io.InputStream)
     */
    @Override
    public void readExternal(InputStream in) throws IOException, ClassNotFoundException {
        byte[] buffer = new byte[IBE_G_SIZE + IBE_ZR_SIZE];
        int size = in.read(buffer);
        if (size != buffer.length)
            throw new IOException("Not enough bytes for a Signature");
        this.rID = new byte[IBE_ZR_SIZE];
        this.hID = new byte[IBE_G_SIZE];
        System.arraycopy(buffer, 0, rID, 0, IBE_ZR_SIZE);
        System.arraycopy(buffer, IBE_ZR_SIZE, hID, 0, IBE_G_SIZE);
        byte[] dTmp = new byte[4];
        Arrays.fill(dTmp, (byte) 0);
        if (4 != in.read(dTmp))
            throw new IOException("Not enough bytes for a Signature");
        int dSize = bytesToInt(dTmp);
        this.digest = new byte[dSize];
        dSize = in.read(digest);
        if (dSize != digest.length)
            throw new IOException("Not enough bytes for a Signature");
        setUserString(new String(digest, USER_STRING_ENCODING));
        this.signatureParameter = new IBEPublicParameter();
        signatureParameter.readExternal(in);
        byte[] sTmp = new byte[8];
        Arrays.fill(sTmp, (byte) 0);
        if (8 != in.read(sTmp))
            throw new IOException("Not enough bytes for a Signature");
        this.signingDate = new Date(bytesToLong(sTmp));
        size = in.read();
        byte[] hash = new byte[size];
        size = in.read(hash);
        if (size != hash.length)
            throw new IOException("Not enough bytes for a Signature");
        this.hashAlgorithm = new String(hash, 0, size, USER_STRING_ENCODING);
        setPairing(signatureParameter.getPairing());
    }
}
