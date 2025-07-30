package charger.main.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import charger.main.dto.CoorDinatesDto;
import charger.main.dto.EvStoreResultDto;
import charger.main.dto.FavoriteDto;
import charger.main.dto.MapInfoResultDto;
import charger.main.dto.MapSetIdsDto;
import charger.main.dto.StoreResultsDto;
import charger.main.service.MapService;
import charger.main.util.StoreUtil;
import jakarta.validation.Valid;

@RestController
public class MapController {
	
	@Autowired
	private MapService mapService;
	
	@PostMapping("/map/post/stations")
	public List<StoreResultsDto> getDefaultStations(
			@Valid
			@RequestBody MapInfoResultDto dto, Authentication authentication) {
		//위도 경도로 주위 충전소 찾기getMapInfo
		List<StoreResultsDto> result = mapService.getEVStores(dto); 
			
		return result;
	}
	
	@PostMapping("/map/stations/size")
	public int getStationsSize(@Valid
			@RequestBody MapInfoResultDto dto) {
		
		
		return mapService.getStoresize(dto);
	}
	
	@GetMapping("/map/post/stations/favorite")
	public List<FavoriteDto> getFavoriteStations(Authentication authentication) {
		
		List<FavoriteDto> result = null;
		//즐겨찾기 정보 불러오기
		//로그인 되어있으면
		if(authentication.isAuthenticated()) {
			result = mapService.getFavorites(authentication.getName());
		}
		
		return result;
	}
	
	@PostMapping("/map/post/setMap")
	public Map<String,List<EvStoreResultDto>> setMapInfo(
			@Valid
			@RequestBody CoorDinatesDto dto) {
		//위도 경도로 주위 충전소 찾기
		Map<String,List<EvStoreResultDto>> result = mapService.setEvStores(dto); 
		
		//매번 조회시마다 해당하는 충전소가 없으면 충전소 등록
		
		return result;
	}
	
	@PostMapping("/map/post/setById")
	public void setById(@RequestBody MapSetIdsDto ids) {
		mapService.setById(new HashSet<>(ids.getIds()));
	}
	
	@GetMapping("/map/get/getbystatId")
	public StoreResultsDto getOneStoreByStatid(@RequestParam String statId) {
		return mapService.getOneStoreByStatid(statId);
	}
	
}
