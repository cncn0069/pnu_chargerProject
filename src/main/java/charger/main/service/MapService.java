package charger.main.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.management.AttributeNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.querydsl.core.group.GroupBy;

import charger.main.domain.Charger;
import charger.main.domain.ConnectorTypes;
import charger.main.domain.FavoriteStore;
import charger.main.domain.StoreInfo;
import charger.main.domain.TimeSlot;
import charger.main.domain.embeded.ChargerId;
import charger.main.dto.CoorDinatesDto;
import charger.main.dto.EvStoreResultDto;
import charger.main.dto.FavoriteDto;
import charger.main.dto.MapInfoResultDto;
import charger.main.dto.StoreResultsDto;
import charger.main.dto.TimeSlotDTO;
import charger.main.persistence.ChargerRepository;
import charger.main.persistence.FavoriteRepository;
import charger.main.persistence.StoreInfoRepository;
import charger.main.persistence.TimeSlotRepository;
import charger.main.util.StoreUtil;
import charger.main.util.TimeUtil;
import charger.main.util.TimeUtil.TimeSlotTem;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class MapService {
	
	@Autowired
	private WebClient.Builder webClientBuilder;
	
	@Autowired
	private StoreInfoRepository infoRepo;
	
	@Autowired
	private ChargerRepository chargerRepo;
	
	@Autowired
	private TimeSlotRepository timeSlotRepo;
	
	@Autowired
	private FavoriteRepository favoriteRepo;
	
	@Value("${kakao.api.key}")
	private String KAKAO_API_KEY;
	
	@Value("${dd.api.key}")
	private String DD_API_KEY;
	
	@Value("${dd.api.key.decode}")
	private String DD_API_KEY_DECODE;
	
	@Autowired
	private StoreUtil util;
	
	public List<StoreResultsDto> getEVStores(MapInfoResultDto dto) {
		List<String> codes = util.getMapInfo(dto);
//		Mono<List<List<EvStoreResultDto>>> kepcoResults = util.getKepco(codes.stream().collect(Collectors.toSet()));
//		List<List<EvStoreResultDto>> items = kepcoResults.block();
		
		//더미
		List<List<EvStoreResultDto>> items = util.getDummy(codes.stream().collect(Collectors.toSet()));
		
		List<StoreResultsDto> results = new ArrayList<>();
		
		for(List<EvStoreResultDto> item:items) {
			StoreResultsDto resultDto = util.getStoreResultsDto(item);
			//canuse
			// 사용가능한 조건이 걸려있고 사용가능한 충전기 개수도 0일 때
			if(dto.getMapQueryDto().getCanUse() && resultDto.getTotalChargeNum() <= 0) {
				continue;
			}
			
			boolean flag = false;
			//chgerType
			//충전기 타입 조건 사이즈가 0 이 아닐때
			if(dto.getMapQueryDto().getChgerType().size() != 0) {
				Set<String> types = new HashSet<>();
				for(String chgerType:dto.getMapQueryDto().getChgerType()) {
					//chgerType이 존재하지 않는다면
					switch(chgerType) {
						case "DC차데모":
							types.add("01");
							types.add("03");
							types.add("05");
							types.add("06");
							//급속 충전 가능 수 0 can use가 활성화
							if(dto.getMapQueryDto().getCanUse() && resultDto.getChargeFastNum() <= 0) {
								flag = true;
							}
							break;
						case "AC완속":
							types.add("02");
							//완속 충전 가능 수 0 can use가 활성화 되어있고
							if(dto.getMapQueryDto().getCanUse() && resultDto.getChargeSlowNum() <= 0) {
								flag = true;
							}
							break;
						case "DC콤보":
							types.add("04");
							types.add("05");
							types.add("06");
							types.add("10");
							//급속 충전 가능 수 0 can use가 활성화
							if(dto.getMapQueryDto().getCanUse() && resultDto.getChargeFastNum() <= 0) {
								flag = true;
							}
							break;
						case "AC3상":
							types.add("03");
							types.add("06");
							types.add("07");
							//급속 충전 가능 수 0 can use가 활성화
							if(dto.getMapQueryDto().getCanUse() && resultDto.getChargeFastNum() <= 0) {
								flag = true;
							}
						case "콤보(완속)":
							types.add("08");
							if(dto.getMapQueryDto().getCanUse() && resultDto.getChargeSlowNum() <= 0) {
								flag = true;
							}
						case "NACS":
							types.add("09");
							types.add("10");
							//급속 충전 가능 수 0 can use가 활성화
							if(dto.getMapQueryDto().getCanUse() && resultDto.getTotalNacsNum() <= 0) {
								flag = true;
							}
					}
				}
				
				if(flag) {
					continue;
				}
				
				for(String type : types) {
					flag = true;		
					if(resultDto.getEnabledCharger().contains(type)) {
						flag = false;
						break;
					}
				}
				
				if(flag) {
					continue;
				}
			}
			//output
			Map<String, EvStoreResultDto> chargerInfo = resultDto.getChargerInfo();
			for(String keySet: chargerInfo.keySet()) {
				if(!chargerInfo.get(keySet).getOutput().equals("")) {
					int output = Integer.parseInt(chargerInfo.get(keySet).getOutput());
					//output이 범위 밖을 벗어난다면
					if(dto.getMapQueryDto().getOutputMin() > output || dto.getMapQueryDto().getOutputMax() < output) {
						flag = true;
					}
					try {
						StoreInfo storeInfo = infoRepo.findById(chargerInfo.get(keySet).getStatId()).orElseThrow(()->new AttributeNotFoundException());
						ChargerId chargerId = new ChargerId();
						chargerId.setStatId(storeInfo.getStatId());
						chargerId.setChgerId(chargerInfo.get(keySet).getChgerId());
						
						
						//충전기 정보가 등록안되어있으면 등록하기
						if(chargerRepo.findById(chargerId).isEmpty()){
							Charger charger = new Charger();
							charger.setChgerType(chargerInfo.get(keySet).getChgerType());
							charger.setOutput(Integer.parseInt(chargerInfo.get(keySet).getOutput()));
							charger.setChargerId(chargerId);
							charger.setStoreInfo(storeInfo);
							
							chargerRepo.save(charger);
						}
						
						//24시간 타임 슬롯 만들기
						//이건 예약 과정에 따로 만드는게 좋을듯?
						//새로운 예약이 만들어지는과정
						//여기서 하면 date등의 자료를 또 따로 받아야하는데 말이 안됨
					
					} catch (AttributeNotFoundException e) {
						// TODO Auto-generated catch block
						log.info("존재하지 않는 storeInfo");
						e.printStackTrace();
					}
				}
			}
			//chgerType이 존재하지 않는다면
			if(flag) {
				continue;
			}
			
			results.add(resultDto);
		}
		return results;
	}
	
	//부산시도 코드로 부산시의 모든 전기차 충전소 위치 저장
	public Map<String,List<EvStoreResultDto>> setEvStores(CoorDinatesDto dto) {
		Set<String> kakao = util.getKakao(dto);
		Set<String> setIds = util.getSetIds(kakao.stream().collect(Collectors.toList()));
		Map<String,List<EvStoreResultDto>> search= util.setMapInfo(setIds);
		
		for(String keySet:search.keySet()) {
			Set<ConnectorTypes> copTy = new HashSet<>();

			for(EvStoreResultDto evDto: search.get(keySet)) {
				
				switch (evDto.getChgerType()) {
					case "1":
						copTy.add(ConnectorTypes.DC차데모);
						break;
					case "2":
						copTy.add(ConnectorTypes.AC완속);
						break;
					case "3":
						copTy.add(ConnectorTypes.DC차데모);
						copTy.add(ConnectorTypes.AC3상);
						break;
					case "4":
						copTy.add(ConnectorTypes.DC콤보);
						break;
					case "5":
						copTy.add(ConnectorTypes.DC차데모);
						copTy.add(ConnectorTypes.DC콤보);
						break;
					case "6":
						copTy.add(ConnectorTypes.DC차데모);
						copTy.add(ConnectorTypes.AC3상);
						copTy.add(ConnectorTypes.DC콤보);
						break;
					case "7":
						copTy.add(ConnectorTypes.AC3상);
						break;
					case "8":
						copTy.add(ConnectorTypes.DC콤보_완속);
						break;
					case "9":
						copTy.add(ConnectorTypes.NACS);
						break;
					case "10":
						copTy.add(ConnectorTypes.DC콤보);
						copTy.add(ConnectorTypes.NACS);
						break;
				}
			
			EvStoreResultDto result = search.get(keySet).get(0);
			
			infoRepo.save(StoreInfo.builder()
		            .statId(result.getStatId())
		            .statNm(result.getStatNm())
		            .addr(result.getAddr())
		            .lat(result.getLat())
		            .lng(result.getLng())
		            .parkingFree(result.getParkingFree().equals("Y")?true:false)
		            .limitYn(result.getLimitYn().equals("Y") ?true:false)
		            .enabledCharger(copTy) // Set<ConnectorTypes>
		            .busiId(result.getBusiId())
		            .busiNm(result.getBusiNm())
		            .build());
			}
		}
		return search;
	}
	
	public int getStoresize(MapInfoResultDto dto) {
		return util.getMapInfo(dto).size();
	}
	
	//즐겨찾기한 저장소 추가
	public List<FavoriteDto> getFavorites(String username){
		List<String> codes = favoriteRepo.getByUsername(username);
		Mono<List<List<EvStoreResultDto>>> kepcoResults = util.getKepco(codes.stream().collect(Collectors.toSet()));
		
		Map<String,FavoriteStore> favorites = favoriteRepo.getAllByUsername(username)
				.stream()
				.collect(Collectors.toMap(f -> (String)(f.getStoreInfo().getStatId()),
						Function.identity(),
						(existing, replacement) -> existing
						));
		//동기로 바꾸는건 가장 마지막에
		List<List<EvStoreResultDto>> items = kepcoResults.block();
		List<FavoriteDto> results = new ArrayList<>();
		for(List<EvStoreResultDto> item:items) {
			StoreResultsDto storeDto = util.getStoreResultsDto(item);
			
			results.add(FavoriteDto.builder()
					.storeResults(storeDto)
					.state(favorites.get(storeDto.getStatId()).getState())
					.createAt(favorites.get(storeDto.getStatId()).getCreatedAt())
					.build());
		}
		
		//내가 즐겨찾기한 가게 리턴
		return results;
	}
}
