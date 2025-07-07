package charger.main.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import charger.main.domain.Member;
import charger.main.persistence.MemberRepository;

@Service
public class ServiceUserDetailsService implements UserDetailsService{
	@Autowired
	private MemberRepository memberRepo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		Member member = memberRepo.findById(username).orElseThrow(()->new UsernameNotFoundException("사용하지 않는 사용자입니다."));
		
		//삭제된 사용자라면
		if(!member.isEnabled()) {
			throw new UsernameNotFoundException("탈퇴된 사용자 입니다.");
		}
		
		return User.builder().username(member.getUsername())
				.password(member.getPassword())
				.authorities(AuthorityUtils.createAuthorityList(member.getRole().toString()))
				.disabled(!member.isEnabled())
				.build();
	}

}
