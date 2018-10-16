package hamaster.gradesign.dao;

import java.util.List;

import hamaster.gradesign.entity.User;

public interface UserDAO extends DAO {

	/**
	 * 获取注册完成但还未验证邮箱的用户
	 * @param amount 用户数量
	 * @return 新用户列表
	 */
	List<User> listNewRegisteredUsers(int amount);

	/**
	 * 判断电子邮件地址是否被使用
	 * @param email 待判断的电子邮件地址
	 * @return 只有被使用时返回true
	 */
	boolean isEmailExist(String email);

	/**
	 * 处理用户登录
	 * @param email 用户电子邮件地址
	 * @param password 明文密码
	 * @return 只有电子邮件地址和密码匹配时返回User对象
	 */
	User login(String email, String password);
}
