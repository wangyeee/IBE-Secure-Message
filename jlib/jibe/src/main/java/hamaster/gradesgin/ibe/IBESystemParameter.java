package hamaster.gradesgin.ibe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * IBE系统参数 包含系统公共参数和系统主密钥
 * The system parameter of an IBE system, containing the public parameter and system master key.
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
public class IBESystemParameter implements Serializable, IBEConstraints {
    private static final long serialVersionUID = 784755162095627372L;

    /**
     * 公共参数
     * The public key
     */
    protected IBEPublicParameter publicParameter;

    /**
     * 主密钥
     * The system master key, all user private keys are generated from this key.
     */
    protected byte[] masterKey;

    public IBESystemParameter() {
    }

    public IBEPublicParameter getPublicParameter() {
        return publicParameter;
    }

    public void setPublicParameter(IBEPublicParameter publicParameter) {
        this.publicParameter = publicParameter;
    }

    public byte[] getMasterKey() {
        return masterKey;
    }

    public void setMasterKey(byte[] masterKey) {
        this.masterKey = masterKey;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(masterKey);
        result = prime * result + ((publicParameter == null) ? 0 : publicParameter.hashCode());
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
        if (!(obj instanceof IBESystemParameter))
            return false;
        IBESystemParameter other = (IBESystemParameter) obj;
        if (!Arrays.equals(masterKey, other.masterKey))
            return false;
        if (publicParameter == null) {
            if (other.publicParameter != null)
                return false;
        } else if (!publicParameter.equals(other.publicParameter))
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "IBESystemParameter [publicParameter=" + publicParameter
               + ", masterKey=" + Arrays.toString(masterKey) + "]";
    }

    /*
     * (non-Javadoc)
     * @see hamaster.gradesgin.ibe.IBEConstraints#writeExternal(java.io.OutputStream)
     */
    @Override
    public void writeExternal(OutputStream out) throws IOException {
        if (masterKey == null) {
            byte[] zero = new byte[IBE_ZR_SIZE];
            Arrays.fill(zero, (byte) 0);
            out.write(zero);
        } else {
            // TODO encryption is also needed to protect the master key with a password.
            out.write(masterKey);
        }
        if (publicParameter != null)
            publicParameter.writeExternal(out);
        out.flush();
    }

    /*
     * (non-Javadoc)
     * @see hamaster.gradesgin.ibe.IBEConstraints#readExternal(java.io.InputStream)
     */
    @Override
    public void readExternal(InputStream in) throws IOException, ClassNotFoundException {
        this.masterKey = new byte[IBE_ZR_SIZE];
        if (IBE_ZR_SIZE != in.read(masterKey))
            throw new IOException("Not enough bytes for a SystemParameter");
        this.publicParameter = new IBEPublicParameter();
        publicParameter.readExternal(in);
    }
}
