package hamaster.gradesign.keygen;

import java.io.Serializable;
import java.util.Date;

public class IBECSR implements Serializable {
    private static final long serialVersionUID = -7382866034793567072L;

    final public static int APPLICATION_STATUS_ALL = 0;
    final public static int APPLICATION_STARTED = 1;
    final public static int APPLICATION_APPROVED = 2;
    final public static int APPLICATION_DENIED = 3;
    final public static int APPLICATION_ERROR = 4;
    final public static int APPLICATION_NOT_VERIFIED = 5;
    final public static int APPLICATION_PROCESSING = 6;

    /**
     * 身份描述申请序号
     */
    private Integer requestId;

    /**
     * 身份字符串
     */
    private String identityString;

    /**
     * 申请密码的摘要
     */
    private String password;

    /**
     * 所使用的系统
     */
    private Integer ibeSystemId;

    /**
     * 申请日期
     */
    private Date applicationDate;

    /**
     * 有效期 单位毫秒
     */
    private long period;

    public IBECSR() {
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public String getIdentityString() {
        return identityString;
    }

    public void setIdentityString(String identityString) {
        this.identityString = identityString;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getIbeSystemId() {
        return ibeSystemId;
    }

    public void setIbeSystemId(Integer ibeSystemId) {
        this.ibeSystemId = ibeSystemId;
    }

    public Date getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(Date applicationDate) {
        this.applicationDate = applicationDate;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((applicationDate == null) ? 0 : applicationDate.hashCode());
        result = prime * result + ((ibeSystemId == null) ? 0 : ibeSystemId.hashCode());
        result = prime * result + ((identityString == null) ? 0 : identityString.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + (int) (period ^ (period >>> 32));
        result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IBECSR other = (IBECSR) obj;
        if (applicationDate == null) {
            if (other.applicationDate != null)
                return false;
        } else if (!applicationDate.equals(other.applicationDate))
            return false;
        if (ibeSystemId == null) {
            if (other.ibeSystemId != null)
                return false;
        } else if (!ibeSystemId.equals(other.ibeSystemId))
            return false;
        if (identityString == null) {
            if (other.identityString != null)
                return false;
        } else if (!identityString.equals(other.identityString))
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (period != other.period)
            return false;
        if (requestId == null) {
            if (other.requestId != null)
                return false;
        } else if (!requestId.equals(other.requestId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "IBECSR [requestId=" + requestId + ", identityString=" + identityString + ", password=" + password
                + ", ibeSystemId=" + ibeSystemId + ", applicationDate=" + applicationDate + ", period=" + period + "]";
    }
}
