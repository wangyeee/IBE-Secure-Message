package hamaster.gradesign.keydist.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import hamaster.gradesign.keydist.entity.User;

@Repository
public interface UserDAO extends JpaRepository<User, Integer> {

    /**
     * 获取注册完成但还未验证邮箱的用户
     * @param amount 用户数量
     * @return 新用户列表
     */
    default List<User> listNewRegisteredUsers(int amount) {
        return listNewRegisteredUsers(PageRequest.of(0, amount));
    }

    @Query("select u from User u where u.status = 0")
    List<User> listNewRegisteredUsers(Pageable page);

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
}
