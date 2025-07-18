package charger.main.controller;

import java.util.Random;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import charger.main.dto.EvStroekWhPredictionDto;

@RestController
public class PredictionController {
	
	
	@PostMapping("/pred/kWh")
	public ResponseEntity<?> getkWhPredtion(@RequestParam EvStroekWhPredictionDto dto) {
	    Random random = new Random();
	    double result;

	    // 두 구간 중 하나를 랜덤하게 선택 (true면 10~20, false면 56~70)
	    if (random.nextBoolean()) {
	        // 10 ~ 20 사이의 double
	        result = 10 + (20 - 10) * random.nextDouble();
	    } else {
	        // 56 ~ 70 사이의 double
	        result = 56 + (70 - 56) * random.nextDouble();
	    }

	    return ResponseEntity.ok(result);
	}
}
