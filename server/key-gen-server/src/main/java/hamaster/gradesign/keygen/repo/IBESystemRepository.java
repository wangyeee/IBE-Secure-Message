package hamaster.gradesign.keygen.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hamaster.gradesign.keygen.entity.IBESystemEntity;

public interface IBESystemRepository extends JpaRepository<IBESystemEntity, Integer> {

    @Query("select s from IBESystemEntity s where s.systemOwner = :owner")
    Optional<IBESystemEntity> findByOwner(@Param("owner") String owner);
}
