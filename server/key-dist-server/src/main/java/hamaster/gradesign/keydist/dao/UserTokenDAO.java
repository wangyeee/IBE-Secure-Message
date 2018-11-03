package hamaster.gradesign.keydist.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hamaster.gradesign.keydist.entity.User;
import hamaster.gradesign.keydist.entity.UserToken;

@Repository
public interface UserTokenDAO extends JpaRepository<UserToken, Integer> {

    List<UserToken> findAllByUser(User user);
}
