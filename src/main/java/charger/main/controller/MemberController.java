package charger.main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import charger.main.domain.State;
import charger.main.domain.StoreInfo;
import charger.main.dto.LoginDto;
import charger.main.dto.MemberDto;
import charger.main.service.MemberService;
import jakarta.validation.Valid;

@RestController
public class MemberController {
	
	@Autowired
	MemberService memberService;
	
	@GetMapping({"/", "/index"})
	 public String index() {
		return "index";
	 }
	
	@PostMapping("/user/join")
	public void setUser(@RequestBody MemberDto dto) {
		memberService.setUser(dto);
	}
	
	@PostMapping("/user/join/valid")
	public ResponseEntity<?> validUser(@RequestBody LoginDto dto) {
		memberService.validUser(dto.getUsername());
		return ResponseEntity.ok("가입 가능한 아이디입니다.");
	}
	
	@GetMapping("user/info")
	public MemberDto getUserInfo(Authentication authentication) {
		return memberService.getMemberInfo(authentication.getName());
	}
	
	@PatchMapping("/user/edit")
	public void patchUserInfo(@RequestBody@Valid MemberDto dto,Authentication authentication) {
		memberService.editUser(dto, authentication.getName());
	}
	
	@DeleteMapping("/user/withdraw")
	public void withdrawUser(Authentication authentication) {
		memberService.withDrawUser(authentication.getName());
	}
	
	@GetMapping("user/favorite")
	public void setFavorite(@RequestParam(name="statid")String statId,State state,Authentication authentication) {
		memberService.setFavorite(statId, state,authentication.getName());
	}
	
	@GetMapping("user/favorite/info")
	public List<StoreInfo> getFaboriteInfo(Authentication authentication) {
		return memberService.getFavorites(authentication.getName());
	}
	
	@DeleteMapping("user/favorite/delete")
	public void deleteFavorite(@RequestParam(name="statid")String statId,Authentication authentication) {
		memberService.deleteFavorite(statId, authentication.getName());
	}
}
