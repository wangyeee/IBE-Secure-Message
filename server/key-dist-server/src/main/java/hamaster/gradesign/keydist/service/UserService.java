package hamaster.gradesign.keydist.service;

import hamaster.gradesign.keydist.entity.User;
import hamaster.gradesign.keydist.entity.UserToken;

public interface UserService {

    /**
     * 处理用户登录
     * @param email 用户电子邮件地址
     * @param password 明文密码
     * @return 只有电子邮件地址和密码匹配时返回User对象
     */
    User loginWithEmail(String email, String password);
    User loginWithUsername(String username, String password);
    User loginWithToken(String username, String token);

    UserToken appLogin(String username, String password);
    UserToken appLogin(String username, String password, String description);
    void appLogout(String username, String uuid);

    boolean isEmailExist(String email);

    boolean isUsernameExist(String username);

    void register(User user, String password);
}
