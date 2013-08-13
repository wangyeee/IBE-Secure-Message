package hamaster.gradesign.jsf.bean;

import hamaster.gradesign.dao.UserDAO;
import hamaster.gradesign.entity.User;
import hamaster.gradesign.jsf.validator.StringValidator;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

@SessionScoped
@ManagedBean(name = "viewUserBean")
public class ViewUserBean implements Serializable {
	private static final long serialVersionUID = -5974556005970370000L;

	@ManagedProperty("#{userDAO}")
	private transient UserDAO userDAO;

	private String password;

	private String email;

	private User user;

	public ViewUserBean() {
	}

	public String login() {
		user = userDAO.login(getEmail(), getPassword());
		if (user == null) {
			return "failure";
		}
		return "success";
	}

	public void validatePassword(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		if (value == null || String.valueOf(value).length() < 4) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "密码长度过短", "密码长度过短");
			throw new ValidatorException(message);
		}
	}

	public void validateEmail(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		if (value == null) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "电子邮件地址不能为空", "电子邮件地址不能为空");
			throw new ValidatorException(message);
		}
		String email = value.toString();
		if (!email.matches(StringValidator.EMAIL_REGEX)) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "电子邮件地址格式错误", "电子邮件地址格式错误");
			throw new ValidatorException(message);
		}
	}

	public UserDAO getUserDAO() {
		return userDAO;
	}

	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
