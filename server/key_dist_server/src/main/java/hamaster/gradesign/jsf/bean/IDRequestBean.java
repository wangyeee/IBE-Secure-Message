package hamaster.gradesign.jsf.bean;

import hamaster.gradesign.dao.IDRequestDAO;
import hamaster.gradesign.entity.IDRequest;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;

@RequestScoped
@ManagedBean(name = "idRequestBean")
public class IDRequestBean extends IDRequest implements Serializable {
	private static final long serialVersionUID = -7563997214966277617L;

	@ManagedProperty(value = "#{idRequestDAO}")
	private IDRequestDAO idRequestDAO;

	public IDRequestBean() {
	}

	public IDRequestDAO getIdRequestDAO() {
		return idRequestDAO;
	}

	public void setIdRequestDAO(IDRequestDAO idRequestDAO) {
		this.idRequestDAO = idRequestDAO;
	}
}
