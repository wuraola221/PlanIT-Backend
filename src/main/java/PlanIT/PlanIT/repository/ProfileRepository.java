package PlanIT.PlanIT.repository;

import PlanIT.PlanIT.entity.ERole;
import PlanIT.PlanIT.entity.ProfileEntity;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository <ProfileEntity, Long>{
    Optional <ProfileEntity> findByEmail(String email);
    Optional<ProfileEntity> findByActivationToken(String activationToken);
    Optional<ProfileEntity> findByPasswordResetToken(String passwordResetToken);
    Optional<ProfileEntity> findById(long id);
//    List<ProfileEntity> findAllDevelopers(ERole role);
//
//    @Query("SELECT p.id, p.fullName FROM ProfileEntity p WHERE p.role = :role ORDER BY p.fullName")
//    List<Object[]> findDevelopersForDropdown(@Param("role") ERole role);



}
