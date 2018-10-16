package hamaster.gradesign.jsf.bean;

import hamaster.gradesgin.util.Hash;
import hamaster.gradesign.daemon.EJBClient;
import hamaster.gradesign.dao.UserDAO;
import hamaster.gradesign.entity.User;
import hamaster.gradesign.ibe.util.Hex;
import hamaster.gradesign.jsf.validator.StringValidator;

import java.io.Serializable;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

@RequestScoped
@ManagedBean(name = "editUserBean")
public class EditUserBean extends User implements Serializable {
	private static final long serialVersionUID = -5479555579838299676L;

	@ManagedProperty(value = "#{userDAO}")
	private transient UserDAO userDAO;

	private String confirmPassword;

	public EditUserBean() {
	}

//	public UserDAO getUserDAO() {
//		return userDAO;
//	}

	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String register() {
		if (confirmPassword == null || confirmPassword.equals(getPassword()) == false) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "两次输入的密码不一致", "两次输入的密码不一致");
			FacesContext context = FacesContext.getCurrentInstance();
			context.addMessage("confirmpassword", message);
			return "failure";
		}
		setRegDate(new Date());
		setStatus(USER_REG);
		hashNewUser(this);
		System.out.println(this);
		try {
			userDAO.add(super.clone());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return "success";
	}

	public void validatePassword(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		if (value == null || String.valueOf(value).length() < 6) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "密码长度过短", "密码长度过短");
			throw new ValidatorException(message);
		}
	}

	public void validateUsername(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		if (value == null) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "用户名不能为空", "用户名不能为空");
			throw new ValidatorException(message);
		}
		String username = value.toString();
		if (!username.matches(StringValidator.USERNAME_REGEX)) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "用户名格式错误", "用户名格式错误");
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
		boolean exist = userDAO.isEmailExist(email);
		if (exist) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "电子邮件地址已被使用", "电子邮件地址已被使用");
			throw new ValidatorException(message);
		}
	}

	public static void hashNewUser(User newUser) {
		if (newUser == null || newUser.getPassword() == null || newUser.getRegDate() == null)
			return;
		String salt = EJBClient.util.format(newUser.getRegDate());
		byte[] hash = Hash.sha1(new StringBuilder(newUser.getPassword()).append(salt).toString());
		newUser.setPassword(Hex.hex(hash));
	}
}
