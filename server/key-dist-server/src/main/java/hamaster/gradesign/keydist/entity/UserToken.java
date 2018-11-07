package hamaster.gradesign.keydist.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "IBE_USER_TOKEN")
public class UserToken implements Serializable {
    private static final long serialVersionUID = 3275371286477297953L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "TOKEN_ID")
    private Integer tokenId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "TOKEN_USER")
    private User user;

    @Column(nullable = false, name = "TOKEN_UUID")
    private String uuid;

    @Column(nullable = false, name = "GENERATION_DATE")
    private Date generationDate;

    @Column(nullable = true, name = "DESCRIPTION")
    private String description;

    public UserToken() {
    }

    public Integer getTokenId() {
        return tokenId;
    }

    public void setTokenId(Integer tokenId) {
        this.tokenId = tokenId;
    }

    public String getUsername() {
        return user == null ? null : user.getUsername();
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Date getGenerationDate() {
        return generationDate;
    }

    public void setGenerationDate(Date generationDate) {
        this.generationDate = generationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((generationDate == null) ? 0 : generationDate.hashCode());
        result = prime * result + ((tokenId == null) ? 0 : tokenId.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
        UserToken other = (UserToken) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (generationDate == null) {
            if (other.generationDate != null)
                return false;
        } else if (!generationDate.equals(other.generationDate))
            return false;
        if (tokenId == null) {
            if (other.tokenId != null)
                return false;
        } else if (!tokenId.equals(other.tokenId))
            return false;
        if (user == null) {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        if (uuid == null) {
            if (other.uuid != null)
                return false;
        } else if (!uuid.equals(other.uuid))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "UserToken [tokenId=" + tokenId + ", user=" + user + ", uuid=" + uuid + ", generationDate="
                + generationDate + ", description=" + description + "]";
    }
}
