package hamaster.gradesign.entity;

import hamaster.gradesgin.ibe.io.SecureByteArrayInputStream;
import hamaster.gradesgin.ibe.io.SecureByteArrayOutputStream;
import hamaster.gradesgin.util.IBECapsule;
import hamaster.gradesgin.util.IBECapsuleAESImpl;
import hamaster.gradesgin.util.MemoryUtil;
import hamaster.gradesign.IdentityDescription;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * 保存用户身份描述信息的表
 * 用户私钥和签名证书加密后存储
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
@Entity
@Table(name = "IBE_IDENTITY_DESCRIPTION")
public class IdentityDescriptionEntity implements Serializable {
    private static final long serialVersionUID = 5131049791994128188L;

    /**
     * 身份描述ID主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "IBE_ID")
    private Integer ibeId;

    /**
     * 所有者名称 值与证书的ownerString字段一致
     */
    @Column(unique = true, nullable = false, name = "ID_OWNER")
    private String idOwner;

    /**
     * 用户所属于的IBE系统
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "SYSTEM")
    private IBESystemEntity system;

    /**
     * 以字节形式存储的加密后的身份描述信息
     */
    @Lob
    @Column(nullable = false, name = "ENCRYPTED_ID")
    private byte[] encryptedIdentityDescription;

    public IdentityDescriptionEntity() {
    }

    public Integer getIbeId() {
        return ibeId;
    }

    public void setIbeId(Integer ibeId) {
        this.ibeId = ibeId;
    }

    public String getIdOwner() {
        return idOwner;
    }

    public void setIdOwner(String idOwner) {
        this.idOwner = idOwner;
    }

    public IBESystemEntity getSystem() {
        return system;
    }

    public void setSystem(IBESystemEntity system) {
        this.system = system;
    }

    public byte[] getEncryptedIdentityDescription() {
        return encryptedIdentityDescription;
    }

    public void setEncryptedIdentityDescription(byte[] encryptedIdentityDescription) {
        this.encryptedIdentityDescription = encryptedIdentityDescription;
    }

    /**
     * 获取密码保护的IdentityDescription对象
     * 加密方式为AES256_CBC_PKCS5Padding
     * @param aesKey 加密用的密钥
     * @return IdentityDescription对象
     */
    public IdentityDescription getIdentityDescription(byte[] aesKey) {
        if (encryptedIdentityDescription == null)
            return null;
        synchronized (this) {
            IdentityDescription identityDescription;
            IBECapsule capsule = new IBECapsuleAESImpl();
            capsule.setKey(aesKey);
            try {
                ByteArrayInputStream bin = new SecureByteArrayInputStream(encryptedIdentityDescription);
                capsule.readExternal(bin);
                identityDescription = (IdentityDescription) capsule.getDataAsObject();
                bin.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
            return identityDescription;
        }
    }

    /**
     * 使用密码加密<code>IdentityDescription</code>
     * @param identityDescription 要加密的<code>IdentityDescription<code>
     * @param aesKey 加密用密码 可能会被处理后再加密
     */
    public void setIdentityDescription(IdentityDescription identityDescription, byte[] aesKey) {
        synchronized (this) {
            ByteArrayOutputStream bout = new SecureByteArrayOutputStream();
            IBECapsule capsule = new IBECapsuleAESImpl();
            capsule.setKey(aesKey);
            capsule.protect(identityDescription);
            try {
                capsule.writeExternal(bout);
                bout.flush();
                if (encryptedIdentityDescription != null)
                    MemoryUtil.fastSecureBuffers(encryptedIdentityDescription);
                this.encryptedIdentityDescription = bout.toByteArray();
                bout.close();
            } catch (IOException e) {
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(encryptedIdentityDescription);
        result = prime * result + ((ibeId == null) ? 0 : ibeId.hashCode());
        result = prime * result + ((idOwner == null) ? 0 : idOwner.hashCode());
        result = prime * result + ((system == null) ? 0 : system.hashCode());
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
        if (!(obj instanceof IdentityDescriptionEntity))
            return false;
        IdentityDescriptionEntity other = (IdentityDescriptionEntity) obj;
        if (!Arrays.equals(encryptedIdentityDescription,
                           other.encryptedIdentityDescription))
            return false;
        if (ibeId == null) {
            if (other.ibeId != null)
                return false;
        } else if (!ibeId.equals(other.ibeId))
            return false;
        if (idOwner == null) {
            if (other.idOwner != null)
                return false;
        } else if (!idOwner.equals(other.idOwner))
            return false;
        if (system == null) {
            if (other.system != null)
                return false;
        } else if (!system.equals(other.system))
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "IdentityDescriptionEntity [ibeId=" + ibeId + ", idOwner="
               + idOwner + ", system=" + system
               + ", encryptedIdentityDescription="
               + Arrays.toString(encryptedIdentityDescription) + "]";
    }
}
