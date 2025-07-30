package charger.main.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import charger.main.dto.InquiryBoardDto;
import charger.main.service.InquiryBoardService;

@RestController
public class InquiryBoardController {
	@Autowired
	InquiryBoardService boardService;
	
	@PostMapping("inquiry/insert")
	public ResponseEntity<?> createInquiry(Authentication authentication,@RequestBody InquiryBoardDto dto) {
		return boardService.createInquiry(authentication.getName(), dto);
	}
	
	@GetMapping("inquiry/get")
	public ResponseEntity<?> getInquiryBoard(Authentication authentication,Pageable pageable) {
		return boardService.getInquiryBoard(authentication.getName(), pageable);
	}
	
	@PatchMapping("inquiry/update")
	public ResponseEntity<?> updateInquiryBoard(Authentication authentication,@RequestBody InquiryBoardDto dto) {
		return boardService.updateInquiryBoard(authentication.getName(), dto);
	}
	
	@DeleteMapping("inquiry/delete")
	public ResponseEntity<?> deleteInquiryBoard(Authentication authentication,@RequestParam Long id) {
		return boardService.deleteInquiryBoard(authentication.getName(), id);
	}
}
