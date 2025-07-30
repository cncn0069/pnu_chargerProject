package charger.main.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import charger.main.domain.FavoriteStore;
import charger.main.domain.Member;
import charger.main.domain.Role;
import charger.main.domain.State;
import charger.main.domain.StoreInfo;
import charger.main.domain.UserCarInfo;
import charger.main.domain.embeded.FavoriteStoreId;
import charger.main.dto.EvCarDto;
import charger.main.dto.MemberDto;
import charger.main.persistence.EVCarModelRepository;
import charger.main.persistence.EvCarsRepository;
import charger.main.persistence.FavoriteRepository;
import charger.main.persistence.MemberRepository;
import charger.main.persistence.StoreInfoRepository;
import charger.main.persistence.UserCarInfoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class MemberService {
	@Autowired
	MemberRepository memberRepo;
	
	@Autowired
	StoreInfoRepository infoRepo;
	
	@Autowired
	FavoriteRepository favoriteRepo;
	
	@Autowired
	UserCarInfoRepository carInfoRepository;
	
	@Autowired
	EVCarModelRepository carModelRepository;
	
	@Autowired
	EvCarsRepository evCarsRepository;
	
	@Autowired
	UserCarInfoRepository userCarInfoRepository;
	@Autowired
	private PagedResourcesAssembler<MemberDto> pagedResourcesAssembler;
	
	PasswordEncoder encode = new BCryptPasswordEncoder();
	
	@PersistenceContext
	private EntityManager em;
	//회원가입
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
				.zipcode(dto.getZipcode())
				.roadAddr(dto.getRoadAddr())
				.detailAddr(dto.getDetailAddr())
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
		member.setEmail(dto.getEmail());
		member.setPhoneNumber(dto.getPhoneNumber());
		member.setEmail(dto.getEmail());
		member.setSex(dto.getSex());
		member.setZipcode(dto.getZipcode());
		member.setRoadAddr(dto.getRoadAddr());
		member.setDetailAddr(dto.getDetailAddr());
		
		
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
				.zipcode(member.getZipcode())
				.roadAddr(member.getRoadAddr())
				.detailAddr(member.getDetailAddr())
				.createAt(member.getCreateAt())
				.build();
	}
	
	public void withDrawUser(String username) {
		Member member = memberRepo.findById(username).get();
		member.setEnabled(false);
		
		memberRepo.save(member);
	}
	
	public void setFavorite(String statId,State state,String username) {
		Member member = memberRepo.findById(username).get();
		StoreInfo info = infoRepo.findById(statId).orElseThrow(()->new IllegalStateException("존재하지 않는 충전소 입니다."));
		
		
		FavoriteStoreId favoriteStoreId = new FavoriteStoreId();
		favoriteStoreId.setStoreId(info.getStatId());
		favoriteStoreId.setUsername(member.getUsername());
		
		Optional<FavoriteStore>  opt = favoriteRepo.findById(favoriteStoreId);
		//이미 존재하는 좋아하는 가게가 없다면
		if(opt.isEmpty()) {
			favoriteRepo.save(FavoriteStore.builder()
					.favoriteStoreId(favoriteStoreId)
					.member(member)
					.storeInfo(info)
					.createdAt(LocalDateTime.now())
					.enabled(true)
					.state(state)
					.build());
		}else {
			FavoriteStore store = opt.get();
			store.setEnabled(true);
			favoriteRepo.save(store);
		}
		
		
	}
	
	public void setUserCarInfo(EvCarDto dto,String username) {
		Member member = memberRepo.findById(username).get();
		
		carInfoRepository.save(UserCarInfo.builder()
				.carId(carModelRepository.findByEvCarModelName(dto.getModel()))
				.member(member)
				.createAt(LocalDateTime.now())
				.mainModel(dto.getMainModel())
				.enabled(true)
				.build());
	}
	
	public List<EvCarDto> getUserCarInfo(String username){
		Member member = memberRepo.findById(username).get();
		
		return userCarInfoRepository.findByMemberAndEnabledTrue(member)
				.stream()
				.map(n -> EvCarDto.builder()
				.brand((evCarsRepository.getByCarId(n.getCarId().getCarId())).getBrand())
				.model(n.getCarId().getEvCarModelName())
				.mainModel(n.getMainModel())
				.userCarId(n.getUserCarId())
				.build())
				.collect(Collectors.toList());
	}
	
	public void patchUserCarInfo(EvCarDto dto,Long userCarId,String username) {
		UserCarInfo carInfo = userCarInfoRepository.findById(userCarId).get();
		
		//모델이랑
		//메인 차량 변경만 가능
		carInfo.setMainModel(dto.getMainModel());
		carInfo.setCarId(carModelRepository.findByEvCarModelName(dto.getModel()));

		carInfoRepository.save(carInfo);
	}
	
	public void deleteUserCarInfo(Long userCarId) {
		UserCarInfo carInfo = userCarInfoRepository.findById(userCarId).get();
		carInfo.setEnabled(false);
		userCarInfoRepository.save(carInfo);
	}
	
	public List<StoreInfo> getFavorites(String username){
		
		Member member = memberRepo.findById(username).get();
		List<String> statIds = favoriteRepo.getByUsername(member.getUsername());
		
		
		return statIds.stream().map(n-> infoRepo.findById(n).get()).collect(Collectors.toList());
	}
	
	public void deleteFavorite(String statId,String username) {
	
		Member member = memberRepo.findById(username).get();
		StoreInfo info = infoRepo.findById(statId).orElseThrow(()->new IllegalStateException("존재하지 않는 충전소 입니다."));
		FavoriteStoreId favoriteStoreId = new FavoriteStoreId();
		favoriteStoreId.setStoreId(info.getStatId());
		favoriteStoreId.setUsername(member.getUsername());
		
		FavoriteStore favoriteStore = favoriteRepo.findById(favoriteStoreId).get();
		favoriteStore.setEnabled(false);
		favoriteRepo.save(favoriteStore);
	}
	
	public ResponseEntity<?> getUsers(Pageable pageable) {
		
		Page<Member> page = memberRepo.findAll(pageable);
		
		Page<MemberDto> memberDtoPage = page
				.map(n-> MemberDto.builder()
						.username(n.getUsername())
					    .nickname(n.getNickname())
					    .phoneNumber(n.getPhoneNumber())
					    .email(n.getEmail())
					    .sex(n.getSex())
					    .role(n.getRole())
					    .enabled(n.isEnabled())
					    .zipcode(n.getZipcode())
					    .roadAddr(n.getRoadAddr())
					    .detailAddr(n.getDetailAddr())
					    .createAt(n.getCreateAt())
					    .build());
		
		PagedModel<EntityModel<MemberDto>> pagedModel = pagedResourcesAssembler.toModel(memberDtoPage);
		return ResponseEntity.ok().body(pagedModel);
	}
}
