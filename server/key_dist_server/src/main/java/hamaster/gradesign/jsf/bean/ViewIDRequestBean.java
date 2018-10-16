package hamaster.gradesign.jsf.bean;

import hamaster.gradesign.dao.IDRequestDAO;
import hamaster.gradesign.entity.IDRequest;
import hamaster.gradesign.idmgmt.IBESystemBean;
import hamaster.gradesign.jsf.page.DatabasePageDataModel;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;

@SessionScoped
@ManagedBean(name = "viewIDRequestBean")
public class ViewIDRequestBean implements Serializable {
	private static final long serialVersionUID = -1148906844021585398L;

	@ManagedProperty(value = "#{ibeSystemDAO}")
	private transient IBESystemBean ibeSystem;

	@ManagedProperty(value = "#{idRequestDAO}")
	private transient IDRequestDAO idRequestDAO;

    /**
     * 当前页面大小
     */
	protected int pageSize = 3;

	public ViewIDRequestBean() {
	}

	public DataModel<IDRequest> getUserIDRequests() {
		FacesContext context = FacesContext.getCurrentInstance();
		final ViewUserBean viewUserBean = (ViewUserBean) context.getApplication().getExpressionFactory().createValueExpression(context.getELContext(), "#{viewUserBean}", ViewUserBean.class).getValue(context.getELContext());
		DatabasePageDataModel<IDRequest> model = new DatabasePageDataModel<IDRequest>(pageSize) {
			@Override
			public List<IDRequest> list(int page, int pageSize) {
				return idRequestDAO.list(viewUserBean.getUser(), page, pageSize, -1);
			}
			@Override
			public int getCountImpl() {
				return idRequestDAO.count(viewUserBean.getUser());
			}
		};
		return model;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public IBESystemBean getIbeSystem() {
		return ibeSystem;
	}

	public void setIbeSystem(IBESystemBean ibeSystem) {
		this.ibeSystem = ibeSystem;
	}

	public IDRequestDAO getIdRequestDAO() {
		return idRequestDAO;
	}

	public void setIdRequestDAO(IDRequestDAO idRequestDAO) {
		this.idRequestDAO = idRequestDAO;
	}
}
