package charger.main.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import charger.main.dto.CoorDinatesDto;
import charger.main.dto.EvStoreResultDto;
import charger.main.dto.MapInfoResultDto;
import charger.main.dto.StoreResultsDto;
import charger.main.service.MapService;

@RestController
public class MapController {
	
	@Autowired
	private MapService mapService;
	
	@PostMapping("/map/post/stations")
	public List<StoreResultsDto> getDefaultStations(@RequestBody MapInfoResultDto dto) {
		//위도 경도로 주위 충전소 찾기getMapInfo
		List<StoreResultsDto> result = mapService.getEVStores(dto); 
		
		//매번 조회시마다 해당하는 충전소가 없으면 충전소 등록
		
		return result;
	}
	
	@PostMapping("/map/post/setMap")
	public Map<String,List<EvStoreResultDto>> setMapInfo(@RequestBody CoorDinatesDto dto) {
		//위도 경도로 주위 충전소 찾기
		Map<String,List<EvStoreResultDto>> result = mapService.setEvStores(dto); 
		
		//매번 조회시마다 해당하는 충전소가 없으면 충전소 등록
		
		return result;
	}
	
//	@PostMapping("/map/post/stations/filter")
//	public List<StoreResultsDto> getFilterStations(@RequestBody CoorDinatesDto dto){
//		
//	}
}
