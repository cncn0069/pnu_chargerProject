package charger.main.service.Reserve;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.management.AttributeNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import charger.main.domain.Member;
import charger.main.domain.ReserveTimeSlot;
import charger.main.domain.ReseverState;
import charger.main.domain.StoreReservation;
import charger.main.domain.TimeSlot;
import charger.main.domain.embeded.ReserveTimeId;
import charger.main.dto.ReserveDto;
import charger.main.persistence.MemberRepository;
import charger.main.persistence.ReserveRepository;
import charger.main.persistence.ReserverTimeSlotRepository;
import charger.main.persistence.TimeSlotRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReservationCreateService {
	
	@Autowired
	TimeSlotRepository timeSlotRepo;
	
	@Autowired
	MemberRepository memberRepo;
	
	@Autowired
	ReserverTimeSlotRepository reserverTimeSlotRepository;
	
	@Autowired
	ReserveRepository reserveRepo;
	
	@Transactional
	public void setTimeSlot(ReserveDto dto,String username) throws AttributeNotFoundException,IllegalStateException{
		List<Long> sortedSlotIds = dto.getSlotIds().stream().sorted().collect(Collectors.toList());
		
		Member member = memberRepo.findById(username).orElseThrow(()->new UsernameNotFoundException("존재하지 않는 유저입니다 : " +  username));
		
		//연속인지 체크
		for(int i = 1; i < sortedSlotIds.size();i++) {
			if(sortedSlotIds.get(i) != (sortedSlotIds.get(i-1) +1)) {
				throw new IllegalStateException("타임슬롯이 연속적이지 않음");
			}
		}
		
		//해당하는 슬롯을 모두 찾는다.
		List<TimeSlot> slots = sortedSlotIds.stream().map(n-> {
			try {
				return timeSlotRepo.findById(n).orElseThrow(()->new AttributeNotFoundException());
			} catch (AttributeNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}).collect(Collectors.toList());
		
		
		StoreReservation st = StoreReservation.builder()
				.member(member)
				.reserveDate(LocalDate.now())
				.reseverState(ReseverState.예약완료)
				.build();
		
		//예약정보 저장
		reserveRepo.save(st);
		
		//예약가능한지 체크
		//enabled가 true인지 확인
		//사용불가능하다면 오류 발생
		for(TimeSlot slot:slots) {			
			
			if(!slot.isEnabled()) {
				log.error("해당 타임 슬롯 사용중");
				throw new IllegalStateException("해당 타임 슬롯 사용중");
			}
			slot.setEnabled(false);
			//저장후 바로 수정
			ReserveTimeId reId = new ReserveTimeId();
			reId.setReserveId(st.getReserveId());
			reId.setTimeId(slot.getTimeId());
			
			ReserveTimeSlot tiSlot = new ReserveTimeSlot();
			tiSlot.setReserveTimeId(reId);
			tiSlot.setTimeSlot(slot);
			tiSlot.setStoreReservation(st);
			tiSlot.setEnabled(true);
			
			
						
			//예약 정보 따로 저장
			reserverTimeSlotRepository.save(tiSlot);
			
			
			//타임슬롯 사용중으로 변경
			timeSlotRepo.save(slot);
		}
	}
}
