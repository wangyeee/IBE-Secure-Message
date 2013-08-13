package hamaster.gradesign.jsf.bean;

import hamaster.gradesign.IBECSR;
import hamaster.gradesign.dao.IDRequestDAO;
import hamaster.gradesign.entity.IDRequest;

import java.io.Serializable;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

@RequestScoped
@ManagedBean(name = "editIDRequestBean")
public class EditIDRequestBean extends IDRequest implements Serializable {
	private static final long serialVersionUID = -1148906844021585398L;

	private int type;

	@ManagedProperty(value = "#{idRequestDAO}")
	private transient IDRequestDAO idRequestDAO;

	public EditIDRequestBean() {
	}

	public String addIDRequest() {
		FacesContext context = FacesContext.getCurrentInstance();
		ViewUserBean viewUserBean = (ViewUserBean) context.getApplication().getExpressionFactory().createValueExpression(context.getELContext(), "#{viewUserBean}", ViewUserBean.class).getValue(context.getELContext());
		setApplicant(viewUserBean.getUser());
		setApplicationDate(new Date());
		setStatus(IBECSR.APPLICATION_STARTED);
		FacesMessage message;
		try {
			idRequestDAO.add(super.clone());
			message = new FacesMessage(FacesMessage.SEVERITY_INFO, "申请已经受理", "申请已经受理，您将在稍后获取身份描述信息");
		} catch (Exception e) {
			e.printStackTrace();
			message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "申请失败", "申请失败");
		}
		context.addMessage("addresult", message);
		return "success";
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setIdRequestDAO(IDRequestDAO idRequestDAO) {
		this.idRequestDAO = idRequestDAO;
	}
}
