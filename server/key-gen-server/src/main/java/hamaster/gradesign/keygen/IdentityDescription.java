package hamaster.gradesign.keygen;

import hamaster.gradesgin.ibe.IBEPrivateKey;
import hamaster.gradesgin.ibe.IBEPublicParameter;
import hamaster.gradesgin.ibe.io.SecureByteArrayOutputStream;
import hamaster.gradesgin.ibs.IBSCertificate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * 身份描述信息 包含身份字符串和私钥
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
public class IdentityDescription implements Serializable, SecureConstraints {
    private static final long serialVersionUID = -9068795518061465393L;

    /**
     * 用户私钥
     */
    private IBEPrivateKey privateKey;

    /**
     * 用户签名证书
     */
    private IBSCertificate certificate;

    /**
     * 用户所属于的IBE系统的公共参数
     */
    private IBEPublicParameter systemPublicParameter;

    public IdentityDescription() {
    }

    /**
     * <pre>将身份描述信息转换为字节表示形式：
     * 系统公共参数
     * 用户私钥（不包含所有者身份和椭圆函数参数）
     * 签名证书</pre>
     * @return 字节形式表示的身份描述信息
     */
    public byte[] toByteArray() {
        ByteArrayOutputStream out = new SecureByteArrayOutputStream();
        byte[] id = null;
        try {
            systemPublicParameter.writeExternal(out);
            out.write(privateKey.getrID());
            out.write(privateKey.gethID());
            certificate.writeExternal(out);
            id = out.toByteArray();
        } catch (IOException e) {
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
        }
        return id;
    }

    public IBEPrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(IBEPrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public IBSCertificate getCertificate() {
        return certificate;
    }

    public void setCertificate(IBSCertificate certificate) {
        this.certificate = certificate;
    }

    public IBEPublicParameter getSystemPublicParameter() {
        return systemPublicParameter;
    }

    public void setSystemPublicParameter(IBEPublicParameter systemPublicParameter) {
        this.systemPublicParameter = systemPublicParameter;
    }
}
