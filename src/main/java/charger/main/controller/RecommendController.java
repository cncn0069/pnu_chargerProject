package charger.main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import charger.main.dto.CoorDinatesDto;
import charger.main.dto.StoreResultsDto;
import charger.main.service.RecommendService;

@RestController
public class RecommendController {
	
	@Autowired
	RecommendService recommendService;
	
	@PostMapping("/recommend/car")
	public List<StoreResultsDto> getRecommendStore(@RequestBody CoorDinatesDto dto) {
		
		return recommendService.getRecommendStore(dto);
	}
	
}
