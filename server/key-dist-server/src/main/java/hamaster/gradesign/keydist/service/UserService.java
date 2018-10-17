package hamaster.gradesign.keydist.service;

import hamaster.gradesign.keydist.entity.User;

public interface UserService {

    /**
     * 处理用户登录
     * @param email 用户电子邮件地址
     * @param password 明文密码
     * @return 只有电子邮件地址和密码匹配时返回User对象
     */
    User login(String email, String password);

    boolean isEmailExist(String email);

    void save(User user);
}
