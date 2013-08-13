package hamaster.gradesign.entity;

import hamaster.gradesgin.util.IBECapsule;
import hamaster.gradesgin.util.IBECapsuleAESImpl;
import hamaster.gradesgin.util.MemoryUtil;
import hamaster.gradesign.IBESystem;
import hamaster.gradesign.ibe.util.Hex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * 储存一个IBE系统参数的表
 * 系统主密钥和签名证书加密后存储
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
@Entity
@Table(name = "IBE_SYSTEM")
public class IBESystemEntity implements Serializable {
	private static final long serialVersionUID = -7701742629570999997L;

	/**
	 * 系统主键
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "SYSTEM_ID")
	private Integer systemId;

	/**
	 * 系统所有者名称 值与证书的ownerString字段一致
	 */
	@Column(unique = true, nullable = false, name = "SYSTEM_OWNER")
	private String systemOwner;

	/**
	 * 以加密后字节形式存储的IBE系统参数
	 */
	@Lob
	@Column(nullable = false, name = "ENCRYPTED_SYSTEM")
	private byte[] encryptedIBESystem;

	/**
	 * 系统访问密码的SHA-1摘要
	 */
	@Column(nullable = false, name = "SYSTEM_KEY", length = 40)
	private String systemKeyHash;

	public IBESystemEntity() {
	}

	public Integer getSystemId() {
		return systemId;
	}

	public void setSystemId(Integer systemId) {
		this.systemId = systemId;
	}

	public String getSystemOwner() {
		return systemOwner;
	}

	public void setSystemOwner(String systemOwner) {
		this.systemOwner = systemOwner;
	}

	public byte[] getEncryptedIBESystem() {
		return encryptedIBESystem;
	}

	public void setEncryptedIBESystem(byte[] encryptedIBESystem) {
		this.encryptedIBESystem = encryptedIBESystem;
	}

	public String getSystemKeyHash() {
		return systemKeyHash;
	}

	public void setSystemKeyHash(String systemKeyHash) {
		this.systemKeyHash = systemKeyHash;
	}

	/**
	 * 获取密码保护的IBESystem对象
	 * 加密方式为AES256_CBC_PKCS5Padding
	 * @param cryptionKeyAndIV 加密用的密钥和初始向量
	 * @return IBESystem对象
	 */
	public IBESystem getSystem(byte[] cryptionKeyAndIV) {
		synchronized (this) {
			IBESystem system;// = new IBESystem();
//			system.setCryptionKeyAndIV(cryptionKeyAndIV);
			if (encryptedIBESystem == null)
				return null;
			IBECapsule capsule = new IBECapsuleAESImpl();
			capsule.setKey(cryptionKeyAndIV);
			try {
				ByteArrayInputStream bin = new ByteArrayInputStream(encryptedIBESystem);
//				ObjectInputStream in = new ObjectInputStream(bin);
				capsule.readExternal(bin);
				system = (IBESystem) capsule.getDataAsObject();
				system.setCryptionKeyAndIV(cryptionKeyAndIV);
//				in.close();
				bin.close();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}
			return system;
		}
	}

	public void setSystem(IBESystem system) {
		synchronized (this) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			IBECapsule capsule = new IBECapsuleAESImpl();
			capsule.setKey(system.getCryptionKeyAndIV());
			capsule.protect(system);
			try {
//				ObjectOutputStream out = new ObjectOutputStream(bout);
				capsule.writeExternal(bout);
//				out.flush();
				bout.flush();
				if (encryptedIBESystem != null)
					MemoryUtil.fastSecureBuffers(encryptedIBESystem);
				this.encryptedIBESystem = bout.toByteArray();
				System.out.println("IBE System:");
				System.out.println(Hex.hex(bout.toByteArray()));
//				out.close();
				bout.close();
			} catch (IOException e) {
				e.printStackTrace();// TODO debug
				return;
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
		result = prime * result + Arrays.hashCode(encryptedIBESystem);
		result = prime * result + ((systemId == null) ? 0 : systemId.hashCode());
		result = prime * result + ((systemKeyHash == null) ? 0 : systemKeyHash.hashCode());
		result = prime * result + ((systemOwner == null) ? 0 : systemOwner.hashCode());
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
		if (!(obj instanceof IBESystemEntity))
			return false;
		IBESystemEntity other = (IBESystemEntity) obj;
		if (!Arrays.equals(encryptedIBESystem, other.encryptedIBESystem))
			return false;
		if (systemId == null) {
			if (other.systemId != null)
				return false;
		} else if (!systemId.equals(other.systemId))
			return false;
		if (systemKeyHash == null) {
			if (other.systemKeyHash != null)
				return false;
		} else if (!systemKeyHash.equals(other.systemKeyHash))
			return false;
		if (systemOwner == null) {
			if (other.systemOwner != null)
				return false;
		} else if (!systemOwner.equals(other.systemOwner))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IBESystemEntity [systemId=" + systemId + ", systemOwner="
				+ systemOwner + ", encryptedIBESystem="
				+ Arrays.toString(encryptedIBESystem) + ", systemKeyHash="
				+ systemKeyHash + "]";
	}
}
