package hamaster.gradesign.keydist.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hamaster.gradesign.keydist.entity.IDRequest;
import hamaster.gradesign.keydist.entity.User;

@Repository
public interface IDRequestDAO extends JpaRepository<IDRequest, Integer> {

    @Query("select i from IDRequest i where i.applicant = :o and i.identityString = :s")
    Optional<IDRequest> findByOwnerForID(@Param("o") User owner, @Param("s") String idString);

    List<IDRequest> findAllByStatus(int status, Pageable page);

    @Query("select i from IDRequest i where i.applicant = :o and i.status = :s")
    List<IDRequest> findAllByUserAndStatus(@Param("o") User owner, @Param("s") int status, Pageable page);

    @Query("select count(i) from IDRequest i where i.applicant = :o")
    Long countByOwner(@Param("o") User owner);
}
