package charger.main.config;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import charger.main.domain.Member;
import charger.main.domain.Role;

public class SecurityUser extends User{
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SecurityUser(Member member) {
		 super(member.getUsername(), member.getPassword(),
		 AuthorityUtils.createAuthorityList(member
									 .getRole()
									 .stream()
									 .map(Role::name)
									 .toArray(String[]::new)));
	}
}
