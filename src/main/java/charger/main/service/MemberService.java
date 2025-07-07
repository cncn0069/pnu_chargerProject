package charger.main.service;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import charger.main.domain.Member;
import charger.main.domain.Role;
import charger.main.dto.MemberDto;
import charger.main.persistence.MemberRepository;

@Service
public class MemberService {
	@Autowired
	MemberRepository memberRepo;
	//회원가입
	PasswordEncoder encode = new BCryptPasswordEncoder();
	
	public void setUser(MemberDto dto) {
		
		//해당하는 아이디가 있는지 확인
		if(!memberRepo.findById(dto.getUsername()).isEmpty()) {
			//이미 있으면 오류
			throw new IllegalStateException("이미 존재하는 아이디입니다.");
		}
		
		memberRepo.save(Member.builder()
				.username(dto.getUsername())
				.nickname(dto.getNickname())
				.password(encode.encode(dto.getPassword()))
				.phoneNumber(dto.getPhoneNumber())
				.email(dto.getEmail())
				.sex(dto.getSex())
				.role(Arrays.asList(Role.ROLE_MEMBER))
				.address(dto.getAddress())
				.enabled(true)
				.createAt(LocalDateTime.now())
				.build());
	}
	
	public void editUser(MemberDto dto,String username) {
		Member member = memberRepo.findById(dto.getUsername()).get();
		
		if(!dto.getUsername().equals(username)) {
			throw new IllegalStateException("잘못된 아이디 수정");
		}
		
		member.setNickname(dto.getNickname());
		member.setPassword(dto.getPassword());
		member.setEmail(dto.getEmail());
		member.setPhoneNumber(dto.getPhoneNumber());
		member.setEmail(dto.getEmail());
		member.setSex(dto.getSex());
		member.setAddress(dto.getAddress());
		
		
//		.(encode.encode(dto.getPassword()))
//		.phoneNumber(dto.getPhoneNumber())
//		.email(dto.getEmail())
//		.sex(dto.getSex())
//		.role(Arrays.asList(Role.ROLE_MEMBER))
//		.address(dto.getAddress());
		memberRepo.save(member);
	}
	
	public void validUser(String username) {
		//해당하는 아이디가 있는지 확인
		if(!memberRepo.findById(username).isEmpty()) {
			//이미 있으면 오류
			throw new IllegalStateException("이미 존재하는 아이디입니다.");
		}
	}
	
	public MemberDto getMemberInfo(String username) {
		Member member = memberRepo.findById(username).get();
		
		if(!member.isEnabled()) {
			throw new IllegalStateException("삭제된 아이디입니다.");
		}
		
		return MemberDto.builder()
				.username(member.getUsername())
				.nickname(member.getNickname())
				.phoneNumber(member.getPhoneNumber())
				.phoneNumber(member.getPhoneNumber())
				.email(member.getEmail())
				.sex(member.getSex())
				.address(member.getAddress())
				.createAt(member.getCreateAt())
				.build();
	}
	
	public void withDrawUser(String username) {
		Member member = memberRepo.findById(username).get();
		member.setEnabled(false);
		
		memberRepo.save(member);
	}
}
