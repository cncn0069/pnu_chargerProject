package charger.main.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import charger.main.domain.Member;
import charger.main.domain.StoreReservation;

public interface ReserveRepository extends JpaRepository<StoreReservation, Long>{
	List<StoreReservation> findByMemberOrderByReserveId(Member member);
}
