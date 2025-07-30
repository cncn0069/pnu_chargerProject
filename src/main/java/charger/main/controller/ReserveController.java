package charger.main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import charger.main.dto.ReserveDto;
import charger.main.dto.StoreReservationDto;
import charger.main.service.ReserveService;
import jakarta.validation.Valid;

@RestController
public class ReserveController {
	@Autowired
	ReserveService reserveService;
	
	@PostMapping("/reserve/setSlots")
	public ResponseEntity<?> setTimeSlots(@RequestBody@Valid ReserveDto dto,Authentication authentication) throws Exception{

		reserveService.setTimeSlot(dto,authentication.getName());
		
		return ResponseEntity.ok(HttpStatus.OK);
	}
	
	@GetMapping("/reserve/getSlots")
	public ResponseEntity<?> getTimeSlots(Authentication authentication){
		return ResponseEntity.ok().body(reserveService.getReserve(authentication.getName()));
	}
	
	@PostMapping("/reserve/setslotsCancel")
	public ResponseEntity<?> setTimeSlotsCancel(@RequestBody@Valid ReserveDto dto,Authentication authentication) throws Exception{
		reserveService.setTimeSlotCancel(dto,authentication.getName());
		
		return ResponseEntity.ok("예약 취소완료.");
	}
	
	@GetMapping("/admin/reserve/user")
	public List<StoreReservationDto> getReservations(@RequestParam String username){
		
		return reserveService.getReserve(username);
	}
}
