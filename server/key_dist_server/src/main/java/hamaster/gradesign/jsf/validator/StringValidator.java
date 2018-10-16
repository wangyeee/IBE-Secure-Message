package hamaster.gradesign.jsf.validator;

public interface StringValidator {

	/**
	 * 电子邮件
	 */
	String EMAIL_REGEX = "\\w+(\\.\\w+)*@\\w+(\\.\\w+)+";

	/**
	 * 用户名：1到14个中文字符、英文字母、数字或者下划线，中文字符算一个字符
	 */
	String USERNAME_REGEX = "^[a-zA-Z0-9_\u4e00-\u9fa5]{1,14}";
}
