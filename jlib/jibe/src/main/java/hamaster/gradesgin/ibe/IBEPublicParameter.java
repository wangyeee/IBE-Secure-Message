package hamaster.gradesgin.ibe;

import static hamaster.gradesgin.util.Hex.bytesToInt;
import static hamaster.gradesgin.util.Hex.intToByte;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;

import hamaster.gradesgin.util.MemoryUtil;

/**
 * IBE系统公共参数
 * The public parameter of an IBE system
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
public class IBEPublicParameter implements Serializable, IBEConstraints {
    private static final long serialVersionUID = 712976807901527482L;

    /**
     * 参数g
     * parameter G
     */
    protected byte[] paramG;

    /**
     * 参数g1
     * parameter G1
     */
    protected byte[] paramG1;

    /**
     * 参数h
     * parameter H
     */
    protected byte[] paramH;

    /**
     * 椭圆函数参数
     * the pairing
     */
    protected byte[] pairing;

    public IBEPublicParameter() {
    }

    public byte[] getParamG() {
        return paramG;
    }

    public void setParamG(byte[] paramG) {
        this.paramG = paramG;
    }

    public byte[] getParamG1() {
        return paramG1;
    }

    public void setParamG1(byte[] paramG1) {
        this.paramG1 = paramG1;
    }

    public byte[] getParamH() {
        return paramH;
    }

    public void setParamH(byte[] paramH) {
        this.paramH = paramH;
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
        result = prime * result + Arrays.hashCode(pairing);
        result = prime * result + Arrays.hashCode(paramG);
        result = prime * result + Arrays.hashCode(paramG1);
        result = prime * result + Arrays.hashCode(paramH);
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
        if (!(obj instanceof IBEPublicParameter))
            return false;
        IBEPublicParameter other = (IBEPublicParameter) obj;
        if (!Arrays.equals(pairing, other.pairing))
            return false;
        if (!Arrays.equals(paramG, other.paramG))
            return false;
        if (!Arrays.equals(paramG1, other.paramG1))
            return false;
        if (!Arrays.equals(paramH, other.paramH))
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "IBEPublicParameter [paramG=" + Arrays.toString(paramG)
               + ", paramG1=" + Arrays.toString(paramG1) + ", paramH="
               + Arrays.toString(paramH) + ", pairing="
               + Arrays.toString(pairing) + "]";
    }

    /**
     * 序列化字段：<br>
     * g 128字节<br>
     * g1 128字节<br>
     * h 128字节<br>
     * 椭圆函数参数长度 4字节<br>
     * 椭圆函数参数
     * g 128 bytes, g1 128 bytes, h 128 bytes, length of pairing 4 bytes, pairing
     * @see hamaster.gradesgin.ibe.IBEConstraints#writeExternal(java.io.OutputStream)
     */
    @Override
    public void writeExternal(OutputStream out) throws IOException {
        byte[] buffer = new byte[IBE_G_SIZE];
        Arrays.fill(buffer, (byte) 0);
        if (paramG != null)
            System.arraycopy(paramG, 0, buffer, 0, IBE_G_SIZE > paramG.length ? paramG.length : IBE_G_SIZE);
        out.write(buffer);
        Arrays.fill(buffer, (byte) 0);
        if (paramG1 != null)
            System.arraycopy(paramG1, 0, buffer, 0, IBE_G_SIZE > paramG1.length ? paramG1.length : IBE_G_SIZE);
        out.write(buffer);
        Arrays.fill(buffer, (byte) 0);
        if (paramH != null)
            System.arraycopy(paramH, 0, buffer, 0, IBE_G_SIZE > paramH.length ? paramH.length : IBE_G_SIZE);
        out.write(buffer);
        int pSize = pairing == null ? IBE_G_SIZE * 4 : pairing.length;
        out.write(intToByte(pSize));
        byte[] pBuffer = new byte[IBE_G_SIZE * 4];
        Arrays.fill(pBuffer, (byte) 0);
        if (pairing != null)
            System.arraycopy(pairing, 0, pBuffer, 0, pairing.length);
        out.write(pBuffer, 0, pairing == null ? pBuffer.length : pairing.length);
        out.flush();
        MemoryUtil.fastSecureBuffers(buffer, pBuffer);
    }

    /*
     * (non-Javadoc)
     * @see hamaster.gradesgin.ibe.IBEConstraints#readExternal(java.io.InputStream)
     */
    @Override
    public void readExternal(InputStream in) throws IOException, ClassNotFoundException {
        byte[] buffer = new byte[IBE_G_SIZE * 3];
        int pSize = in.read(buffer);
        if (pSize < buffer.length) {
            // 字节数少于期望值，数据丢失
            throw new IOException("Not enough bytes for a PublicParameter");
        }
        this.paramG = new byte[IBE_G_SIZE];
        this.paramG1 = new byte[IBE_G_SIZE];
        this.paramH = new byte[IBE_G_SIZE];
        byte[] pTmp = new byte[4];
        Arrays.fill(pTmp, (byte) 0);
        if (4 != in.read(pTmp)) {
            throw new IOException("Not enough bytes for a PublicParameter");
        }
        pSize = bytesToInt(pTmp);
        this.pairing = new byte[pSize];
        pSize = in.read(pairing);
        if (pSize < pairing.length) {
            // 字节数少于期望值，数据丢失
            throw new IOException("Not enough bytes for a PublicParameter");
        }
        System.arraycopy(buffer, 0, paramG, 0, IBE_G_SIZE);
        System.arraycopy(buffer, IBE_G_SIZE, paramG1, 0, IBE_G_SIZE);
        System.arraycopy(buffer, IBE_G_SIZE * 2, paramH, 0, IBE_G_SIZE);
        MemoryUtil.fastSecureBuffers(buffer);
    }
}
