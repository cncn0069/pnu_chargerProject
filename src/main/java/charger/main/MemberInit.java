package charger.main;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import charger.main.domain.Member;
import charger.main.domain.Role;
import charger.main.persistence.MemberRepository;

@Component
public class MemberInit implements ApplicationRunner{
	@Autowired
	private MemberRepository memberRepo;
	
	private PasswordEncoder encoder = new BCryptPasswordEncoder();
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		// TODO Auto-generated method stub
		
		memberRepo.save(Member.builder()
				.username("member1")
				.password(encoder.encode("a"))
				.nickname("nickname1")
				.phoneNumber("010-1234-5678")
				.email("hongildong@naver.com")
				.sex("male")
				.role(Arrays.asList(Role.ROLE_MEMBER))
				.enabled(true)
				.createAt(LocalDateTime.now())
				.build());
		memberRepo.save(Member.builder()
				.username("admin")
				.password(encoder.encode("a"))
				.nickname("nickname1")
				.phoneNumber("010-1234-5678")
				.email("hongildong@naver.com")
				.sex("male")
				.role(Arrays.asList(Role.ROLE_ADMIN,Role.ROLE_MANAGER))
				.enabled(true)
				.createAt(LocalDateTime.now())
				.build());
		memberRepo.save(Member.builder()
				.username("manager")
				.password(encoder.encode("a"))
				.nickname("nickname1")
				.phoneNumber("010-1234-5678")
				.email("hongildong@naver.com")
				.sex("male")
				.role(Arrays.asList(Role.ROLE_MANAGER))
				.enabled(true)
				.createAt(LocalDateTime.now())
				.build());
	}
}
