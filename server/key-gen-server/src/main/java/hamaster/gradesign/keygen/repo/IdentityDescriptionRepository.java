package hamaster.gradesign.keygen.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hamaster.gradesign.keygen.entity.IdentityDescriptionEntity;

public interface IdentityDescriptionRepository extends JpaRepository<IdentityDescriptionEntity, Integer> {

    @Query("select i from IdentityDescriptionEntity i join i.system s where i.idOwner = :owner and s.systemOwner = :system")
    Optional<IdentityDescriptionEntity> findOneByOwnerAndSystem(@Param("owner") String owner, @Param("system") String system);
}
