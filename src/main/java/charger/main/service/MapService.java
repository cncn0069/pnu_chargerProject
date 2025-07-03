package charger.main.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.resource.NoResourceFoundException;

import charger.main.domain.ConnectorTypes;
import charger.main.domain.StoreInfo;
import charger.main.dto.CoorDinatesDto;
import charger.main.dto.EvStoreResultDto;
import charger.main.dto.KaKaoResultDto;
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
	
	public List<StoreResultsDto> getEVStores(CoorDinatesDto dto) {
		List<String> codes = util.getMapInfo(dto);
		Mono<List<List<EvStoreResultDto>>> kepcoResults = util.getKepco(codes.stream().collect(Collectors.toSet()));
		
		List<List<EvStoreResultDto>> items = kepcoResults.block();
		
		List<StoreResultsDto> results = new ArrayList<>();
		
		for(List<EvStoreResultDto> item:items) {
			StoreResultsDto stDto = new StoreResultsDto();
			stDto.setStatNm(item.get(0).getStatNm());
			stDto.setStatId(item.get(0).getStatId());
			stDto.setAddr(item.get(0).getAddr());
			stDto.setLat(item.get(0).getLat());
			stDto.setLng(item.get(0).getLng());
			stDto.setParkingFree(item.get(0).getParkingFree().equals("Y")?true:false);
			stDto.setLimitYn(item.get(0).getLimitYn().equals("Y") ?true:false);
			stDto.setBusiId(item.get(0).getBusiId());
			stDto.setBusiNm(item.get(0).getBusiNm());
			int totalChargeNum = 0;
			int chargeNum = 0;
			Set<String> enabledCharger = new HashSet<>();
			for(EvStoreResultDto evDto: item) {
				totalChargeNum++;
				if(evDto.getStat().equals("2")){
					chargeNum++;
				}
				enabledCharger.add(evDto.getChgerType());
			}
			stDto.setTotalChargeNum(totalChargeNum);
			stDto.setChargeNum(chargeNum);
			stDto.setEnabledCharger(enabledCharger);
			results.add(stDto);
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
