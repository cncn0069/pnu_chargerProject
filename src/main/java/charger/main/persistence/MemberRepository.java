package charger.main.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import charger.main.domain.Member;

public interface MemberRepository extends JpaRepository<Member, String>{

}
