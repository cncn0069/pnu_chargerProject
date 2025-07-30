package charger.main.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import charger.main.domain.InquiryBoard;
import charger.main.domain.Member;
import charger.main.domain.QInquiryBoard;
import charger.main.dto.InquiryBoardDto;
import charger.main.dto.MemberDto;
import charger.main.persistence.InquiryBoardRepository;
import charger.main.persistence.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class InquiryBoardService {
	
	@Autowired
	private InquiryBoardRepository inquiryBoardRepository;
	
	@Autowired
	private MemberRepository memberRepository;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private PagedResourcesAssembler<InquiryBoardDto> pagedResourcesAssembler;
	
	public ResponseEntity<?> createInquiry(String username,InquiryBoardDto dto) {
		
		Member member= memberRepository.findById(username).get();
		
		if(member == null)
			return ResponseEntity.badRequest().body("회원이 존재하지 않습니다.");
		
		inquiryBoardRepository.save(InquiryBoard.builder()
								.title(dto.getTitle())
								.content(dto.getContent())
								.member(member)
								.createdAt(LocalDateTime.now())
								.enabled(true)
								.build());
		return ResponseEntity.ok().body("게시판 입력 완료.");
	}
	
	public ResponseEntity<?> getInquiryBoard(String username,Pageable pageable) {
		Member member= memberRepository.findById(username).get();
		
		if(member == null)
			return ResponseEntity.badRequest().body("회원이 존재하지 않습니다.");
		
		Page<InquiryBoard> inquiryBoards = inquiryBoardRepository.findAll(pageable);
		
		Page<InquiryBoardDto> inquiryBoardsDtoPage = inquiryBoards
				.map(n-> InquiryBoardDto
				.builder()
				.id(n.getId())
				.title(n.getTitle())
				.content(n.getContent())
				.memberUsername(n.getMember().getUsername())
				.createdAt(n.getCreatedAt())
				.updatedAt(n.getUpdatedAt())
				.build());
		
		PagedModel<EntityModel<InquiryBoardDto>> pagedModel = pagedResourcesAssembler.toModel(inquiryBoardsDtoPage);
		
		return ResponseEntity.ok().body(pagedModel);
	}
	
	public ResponseEntity<?> updateInquiryBoard(String username, InquiryBoardDto dto) {
		Member member= memberRepository.findById(username).get();
		
		if(member == null)
			return ResponseEntity.badRequest().body("회원이 존재하지 않습니다.");
		
		InquiryBoard iBoard = inquiryBoardRepository.findById(dto.getId()).get();
		
		if(!iBoard.getMember().getUsername().equals(username)) {
			return ResponseEntity.badRequest().body("작성자가 아닙니다.");
		}
		iBoard.setTitle(dto.getTitle());
		iBoard.setContent(dto.getContent());
		iBoard.setMember(member);
		iBoard.setUpdatedAt(LocalDateTime.now());
		iBoard.setEnabled(true);
		inquiryBoardRepository.save(iBoard);
		
		return ResponseEntity.ok("수정 완료");
	}
	
	public ResponseEntity<?> deleteInquiryBoard(String username, Long id) {
		Member member= memberRepository.findById(username).get();
		
		if(member == null)
			return ResponseEntity.badRequest().body("회원이 존재하지 않습니다.");
		
		InquiryBoard iBoard = inquiryBoardRepository.findById(id).get();
		if(!iBoard.getMember().getUsername().equals(username)) {
			return ResponseEntity.badRequest().body("작성자가 아닙니다.");
		}
		
		if(!iBoard.isEnabled()) {
			return ResponseEntity.badRequest().body("이미 삭제된 게시글 입니다.");
		}
		iBoard.setEnabled(false);
		
		inquiryBoardRepository.save(iBoard);
		
		return ResponseEntity.ok("삭제 완료");
	}
}
