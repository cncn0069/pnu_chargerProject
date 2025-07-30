package charger.main.service.Reserve;

import javax.management.AttributeNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import charger.main.dto.ReserveDto;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ReservationUpdateService {
	private final ReservationCancelService cancelService;
    private final ReservationCreateService createService;
    
	public void updateTimeSlot(ReserveDto dto, String username) throws AttributeNotFoundException,IllegalStateException {
		cancelService.setTimeSlotCancel(dto,username);
		createService.setTimeSlot(dto, username);
	}
}
