package hamaster.gradesign.mail;

import hamaster.gradesign.entity.IDRequest;
import hamaster.gradesign.entity.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

public class ActivationContent implements Serializable {
	private static final long serialVersionUID = -8810376430415900692L;

	public final static byte ACTIVE_USER = 0x00;
	public final static byte ACTIVE_ID = 0x01;

	private int userId;
	private String email;
	private Date activeDate;
	private byte type;

	private ActivationContent(int userId, String email, Date activeDate) {
		this.userId = userId;
		this.email = email;
		this.activeDate = activeDate;
	}

	public ActivationContent(User user) {
		this(user.getUserId().intValue(), user.getEmail(), user.getRegDate());
		type = ACTIVE_USER;
	}

	public ActivationContent(IDRequest request) {
		this(request.getApplicant().getUserId().intValue(), request.getIdentityString(), new Date());
		type = ACTIVE_ID;
	}

	public final static ActivationContent fromBytes(byte[] bytes) {
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		ActivationContent content = null;
		ObjectInputStream oin = null;
		try {
			oin = new ObjectInputStream(in);
			content = (ActivationContent) oin.readObject();
		} catch (IOException e) {
			// TODO debug
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// 不可能发生
		} finally {
			try {
				oin.close();
			} catch (IOException e) {
			}
		}
		return content;
	}

	public final static byte[] toBytes(ActivationContent activationContent) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream oout = null;
		try {
			oout = new ObjectOutputStream(out);
			oout.writeObject(activationContent);
			oout.flush();
		} catch (IOException e) {
			// TODO debug
			e.printStackTrace();
		} finally {
			try {
				oout.close();
			} catch (IOException e) {
			}
		}
		return out.toByteArray();
	}

	public String getEmail() {
		return email;
	}

	public Date getActiveDate() {
		return activeDate;
	}

	public byte getType() {
		return type;
	}

	public int getUserId() {
		return userId;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activeDate == null) ? 0 : activeDate.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + type;
		result = prime * result + userId;
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
		if (getClass() != obj.getClass())
			return false;
		ActivationContent other = (ActivationContent) obj;
		if (activeDate == null) {
			if (other.activeDate != null)
				return false;
		} else if (!activeDate.equals(other.activeDate))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (type != other.type)
			return false;
		if (userId != other.userId)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ActivationContent [userId=" + userId + ", email=" + email
				+ ", activeDate=" + activeDate + ", type=" + type + "]";
	}
}
