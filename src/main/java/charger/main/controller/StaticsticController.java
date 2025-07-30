package charger.main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.querydsl.core.Tuple;

import charger.main.dto.IdleStaticDto;
import charger.main.dto.StaticsticDto;
import charger.main.service.StatisticService;
import jakarta.validation.Valid;

@RestController
public class StaticsticController {
	
	@Autowired
	StatisticService service;
	
	@GetMapping("/static/weekdays")
	public List<StaticsticDto> getStatisticWeekdayByStatId(@RequestParam String statId) {
		return service.getStatisticWeekdayByStatId(statId);
	}
	
	@GetMapping("/static/idle")
	public IdleStaticDto getIdleInfo(@RequestParam@Valid String local) {
		return service.getIdleInfo(local);
	}
	
	@GetMapping("/static/carTotal")
	public Long getCarTotal() {
		return service.getCarTotal();
	}
	
	@GetMapping("/static/userTotal")
	public Long getUserTotal() {
		return service.getUserTotal();
	}
	
	@GetMapping("/static/userDisableTotal")
	public Long getUserDisableTotal() {
		return service.getUserDisableTotal();
	}
	
	@GetMapping("/static/reserveTotal")
	public Long getReserveTotal() {
		return service.getUserTotal();
	}
}
