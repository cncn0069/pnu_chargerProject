package charger.main.service.Reserve;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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
import charger.main.dto.StoreReservationDto;
import charger.main.persistence.ChargerRepository;
import charger.main.persistence.MemberRepository;
import charger.main.persistence.ReserveRepository;
import charger.main.persistence.ReserverTimeSlotRepository;
import charger.main.persistence.StoreInfoRepository;
import charger.main.persistence.TimeSlotRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReserveGetService {

	@Autowired
	ChargerRepository chargerRepo;
	
	@Autowired
	ReserveRepository reserveRepo;
	
	@Autowired
	MemberRepository memberRepo;
	
	@Autowired
	ReserverTimeSlotRepository reserverTimeSlotRepository;
	
	
	public List<StoreReservationDto> getReserve(String username) {
		Member member = memberRepo.findById(username).orElseThrow(()->new UsernameNotFoundException("존재하지 않는 유저 입니다."));
		
		//사용자로 예약된 예약 목록을 모두 가져옴
		List<StoreReservation> stores = reserveRepo.findByMemberOrderByReserveId(member);
		
		List<StoreReservationDto> result = new ArrayList<>();
		
		for(StoreReservation st: stores) {
			
			StoreReservationDto srtd = new StoreReservationDto();
			srtd.setReserveDate(st.getReserveDate());
			srtd.setReserveId(st.getReserveId());
			srtd.setUsername(st.getMember().getUsername());
			srtd.setReserveDate(st.getReserveDate());
			srtd.setUpdateDate(st.getUpdateDate());
			srtd.setReseverState(st.getReseverState());
			List<ReserveTimeSlot>  rtsls = reserverTimeSlotRepository.findByReserveTimeId_ReserveId(st.getReserveId());
			List<TimeSlot> ts = new ArrayList<>();
			for(ReserveTimeSlot rtsl: rtsls) {
				ts.add(rtsl.getTimeSlot());
			}
			srtd.setSlot(ts);
			
			result.add(srtd);
		}
		result.sort(Comparator.comparing(StoreReservationDto::getReserveDate));
		//날짜로 모으기
		return result;
	}
	
	//예약
	
		
	
	
	
}
