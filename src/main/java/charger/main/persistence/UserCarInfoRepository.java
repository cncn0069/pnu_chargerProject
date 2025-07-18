package charger.main.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import charger.main.domain.Member;
import charger.main.domain.UserCarInfo;

public interface UserCarInfoRepository extends JpaRepository<UserCarInfo, Long>{
	List<UserCarInfo> findByMemberAndEnabledTrue(Member member);
}
