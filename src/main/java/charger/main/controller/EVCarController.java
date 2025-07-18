package charger.main.controller;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import charger.main.dto.EvStroekWhPredictionDto;
import charger.main.service.EvCarService;

@RestController
public class EVCarController {

	@Autowired
	EvCarService carService;
	
	@GetMapping("evcar/brand/info")
	public Set<String> getEvcars(){
		return carService.getEvCarBrandName().stream().collect(Collectors.toSet());
	}
	
	@GetMapping("evcar/brand/model/info")
	public Set<String> getEvCarModel(@RequestParam String brand){
		return carService.getEvCarModelName(brand).stream().collect(Collectors.toSet());
	}
}
