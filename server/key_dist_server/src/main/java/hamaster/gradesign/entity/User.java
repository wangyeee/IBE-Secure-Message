package hamaster.gradesign.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * 保存用户信息的实体类
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
@Entity
@Table(name = "IBE_USER")
public class User implements Serializable, Cloneable {
    private static final long serialVersionUID = -1101813432750693173L;

    /**
     * 用户刚刚注册
     */
    public final static int USER_REG = 0;

    /**
     * 已经发出激活信
     */
    public final static int USER_ACTIVE_LETTER_SENT = 1;

    /**
     * 用户已经被激活
     */
    public final static int USER_ACTIVE = 2;

    /**
     * 用户编号
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "USER_ID")
    private Integer userId;

    /**
     * 用户名
     */
    @Column(nullable = false, name = "USERNAME")
    private String username;

    /**
     * 用户电子邮件地址
     */
    @Column(nullable = false, name = "EMAIL")
    private String email;

    /**
     * 用户密码的SHA-1摘要
     */
    @Column(nullable = false, name = "PASSWORD", length = 40)
    private String password;

    /**
     * 用户注册时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, name = "REG_DATE")
    private Date regDate;

    /**
     * 用户当前状态
     */
    @Column(name = "STATUS")
    private Integer status;

    public User() {
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getRegDate() {
        return regDate;
    }

    public void setRegDate(Date regDate) {
        this.regDate = regDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        User copy = new User();
        copy.userId = this.userId;
        copy.username = this.username;
        copy.email = this.email;
        copy.password = this.password;
        copy.regDate = this.regDate;
        copy.status = this.status;
        return copy;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result
                 + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((regDate == null) ? 0 : regDate.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result
                 + ((username == null) ? 0 : username.hashCode());
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
        User other = (User) obj;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (regDate == null) {
            if (other.regDate != null)
                return false;
        } else if (!regDate.equals(other.regDate))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        if (userId == null) {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "User [userId=" + userId + ", username=" + username + ", email="
               + email + ", password=" + password + ", regDate=" + regDate
               + ", status=" + status + "]";
    }
}
