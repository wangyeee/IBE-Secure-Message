package hamaster.gradesign.jsf.bean;

import hamaster.gradesign.idmgmt.IBESystemBean;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;

@RequestScoped
@ManagedBean(name = "ibeSystemBean")
public class SystemBean implements Serializable {
	private static final long serialVersionUID = 3960486267134726615L;

	@ManagedProperty(value = "#{ibeSystemDAO}")
	private transient IBESystemBean ibeSystem;

	public SystemBean() {
	}

	public IBESystemBean getIbeSystem() {
		return ibeSystem;
	}

	public void setIbeSystem(IBESystemBean ibeSystem) {
		this.ibeSystem = ibeSystem;
	}
}
