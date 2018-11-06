package hamaster.gradesign.keydist.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hamaster.gradesign.keydist.entity.User;
import hamaster.gradesign.keydist.entity.UserToken;

@Repository
public interface UserTokenDAO extends JpaRepository<UserToken, Integer> {

    List<UserToken> findAllByUser(User user);

    @Query("select t from UserToken t where t.uuid = :u")
    Optional<UserToken> findOneByUUID(@Param("u") String uuid);
}
