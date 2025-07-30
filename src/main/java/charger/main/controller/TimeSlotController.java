package charger.main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import charger.main.config.SecurityConfig;
import charger.main.dto.ReserveDto;
import charger.main.dto.TimeSlotDTO;
import charger.main.service.TimeSlotService;
import charger.main.service.Reserve.ReserveGetService;
import jakarta.validation.Valid;

@RestController
public class TimeSlotController {

    private final SecurityConfig securityConfig;
	
	@Autowired
	TimeSlotService service;
	


    TimeSlotController(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }
	
	@PostMapping("/time/timeslots")
	public List<TimeSlotDTO> getTimeSlots(@RequestBody@Valid TimeSlotDTO dto){
		return service.getTimeSlot(dto.getStatId(), dto.getChgerId(), dto.getDate());
	}

}
