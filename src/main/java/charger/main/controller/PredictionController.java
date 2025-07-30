package charger.main.controller;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import charger.main.dto.CoorDinatesDto;
import charger.main.dto.EvStroekWhPredictionDto;
import charger.main.dto.MapInfoResultDto;
import charger.main.dto.PredictionByLocalDto;
import charger.main.dto.StoreResultsDto;
import charger.main.service.MapService;
import charger.main.service.PredictionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class PredictionController {
	
	@Autowired
	MapService mapService;
	
	@Autowired
	PredictionService predictionService;
	
	@PostMapping("/pred/kWh")
	public ResponseEntity<?> getkWhPredtion() {
	    Random random = new Random();
	    double result;

	    // 두 구간 중 하나를 랜덤하게 선택 (true면 10~20, false면 56~70)
	    if (random.nextBoolean()) {
	        // 10 ~ 20 사이의s double
	        result = 10 + (20 - 10) * random.nextDouble();
	    } else {
	        // 56 ~ 70 사이의 double
	        result = 56 + (70 - 56) * random.nextDouble();
	    }

	    return ResponseEntity.ok(result);
	}
	
	@PostMapping("/pred/location")
	public List<StoreResultsDto> getkWhByLocation(@RequestBody@Valid PredictionByLocalDto dto) {
		log.info("예측요청 들어옴");
		List<StoreResultsDto> results = predictionService.getPredctionEVStoreByLocal(dto);
		log.info("예측요청 처리완료");
	    return results;
	}
	
	
}
