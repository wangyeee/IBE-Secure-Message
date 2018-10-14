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
}
