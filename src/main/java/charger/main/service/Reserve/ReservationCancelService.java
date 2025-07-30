package charger.main.service.Reserve;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.management.AttributeNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import charger.main.domain.ReserveTimeSlot;
import charger.main.domain.ReseverState;
import charger.main.domain.StoreReservation;
import charger.main.dto.ReserveDto;
import charger.main.persistence.ReserveRepository;
import charger.main.persistence.ReserverTimeSlotRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReservationCancelService {
	@Autowired
	ReserveRepository reserveRepo;
	@Autowired
	ReserverTimeSlotRepository reserverTimeSlotRepository;
	
	//예약 취소
	@Transactional
	public void setTimeSlotCancel(ReserveDto dto,String username) throws AttributeNotFoundException,IllegalStateException{
		List<Long> sortedReseIds = dto.getReseIds().stream().sorted().collect(Collectors.toList());		

		//해당하는 슬롯을 모두 찾는다.
		List<StoreReservation> slots = sortedReseIds.stream().map(n->
			reserveRepo.findById(n).get()
		).collect(Collectors.toList());
		
		//예약한 당사지인지 확인
		for(StoreReservation slot:slots) {			
			if(!slot.getMember().getUsername().equals(username)) {
				throw new IllegalStateException("예약한 유저와 요청한 유저가 다름");
			}
			if(slot.getReseverState().equals(ReseverState.예약취소)) {
				throw new IllegalStateException("이미 취소된 예약요청");
			}
			
			//예약정보
			for(ReserveTimeSlot rts : reserverTimeSlotRepository.findByReserveTimeId_ReserveId(slot.getReserveId())) {
				StoreReservation st =  reserveRepo.findById(rts.getReserveTimeId().getReserveId()).get();
				//rts reseve_id 가 같은 유저인지
				if(!username.equals(st.getMember().getUsername())) {
					throw new IllegalStateException("이미 예약된 다른 사람의 시간에 접근");
				}
				
				rts.getTimeSlot().setEnabled(true);
				rts.setEnabled(false);
				
				//예약 타임슬롯 저장
				reserverTimeSlotRepository.save(rts);
			}
			
			slot.setReseverState(ReseverState.예약취소);
			slot.setUpdateDate(LocalDate.now());
			//예약저장
			reserveRepo.save(slot);
		}
	}
}
