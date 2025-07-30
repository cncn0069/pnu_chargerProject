package charger.main.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import charger.main.dto.CoorDinatesDto;
import charger.main.dto.StoreResultsDto;

@RestController
public class RecommendController {
	
	@PostMapping("/recommend/car")
	public StoreResultsDto getRecommendStore(CoorDinatesDto dto) {
		
		return null;
	}
	
}
