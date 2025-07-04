package charger.main.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import charger.main.domain.ConnectorTypes;
import charger.main.domain.StoreInfo;
import charger.main.dto.CoorDinatesDto;
import charger.main.dto.EvStoreResultDto;
import charger.main.dto.MapInfoResultDto;
import charger.main.dto.StoreResultsDto;
import charger.main.persistence.StoreInfoRepository;
import charger.main.util.StoreUtil;
import reactor.core.publisher.Mono;

@Service
public class MapService {
	
	@Autowired
	private WebClient.Builder webClientBuilder;
	
	@Autowired
	private StoreInfoRepository infoRepo;
	
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
		Mono<List<List<EvStoreResultDto>>> kepcoResults = util.getKepco(codes.stream().collect(Collectors.toSet()));
		
		List<List<EvStoreResultDto>> items = kepcoResults.block();
		List<StoreResultsDto> results = new ArrayList<>();
		
		for(List<EvStoreResultDto> item:items) {
			StoreResultsDto resultDto = util.getStoreResultsDto(item);
			//canuse
			// 사용가능한 조건이 걸려있고 사용가능한 충전기 개수도 0일 때
			if(dto.getMapQueryDto().getCanUse() && resultDto.getTotalChargeNum() < 0) {
				continue;
			}
			
			boolean flag = false;
			//chgerType
			//충전기 타입 조건 사이즈가 0 이 아닐때
			if(dto.getMapQueryDto().getChgerType().size() != 0) {
				for(String chgerType:dto.getMapQueryDto().getChgerType()) {
					//chgerType이 존재하지 않는다면
					if(!resultDto.getEnabledCharger().contains(chgerType)) {
						flag = true;
						break;
					}
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
}
