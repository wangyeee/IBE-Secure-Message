package hamaster.gradesign.keygen;

import java.io.Serializable;

public class SimpleRESTResponse implements Serializable {
    private static final long serialVersionUID = -8931817682359207120L;

    private int resultCode;

    private String message;

    private Object payload;

    public SimpleRESTResponse() {
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((payload == null) ? 0 : payload.hashCode());
        result = prime * result + resultCode;
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
        SimpleRESTResponse other = (SimpleRESTResponse) obj;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (payload == null) {
            if (other.payload != null)
                return false;
        } else if (!payload.equals(other.payload))
            return false;
        if (resultCode != other.resultCode)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SimpleRESTResponse [resultCode=" + resultCode + ", message=" + message + "]";
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
