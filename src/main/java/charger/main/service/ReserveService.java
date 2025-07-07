package charger.main.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.management.AttributeNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import charger.main.domain.Member;
import charger.main.domain.ReseverState;
import charger.main.domain.StoreReservation;
import charger.main.domain.TimeSlot;
import charger.main.dto.ReserveDto;
import charger.main.persistence.ChargerRepository;
import charger.main.persistence.MemberRepository;
import charger.main.persistence.ReserveRepository;
import charger.main.persistence.StoreInfoRepository;
import charger.main.persistence.TimeSlotRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReserveService {
		
	@Autowired
	StoreInfoRepository infoRepo;
	
	@Autowired
	TimeSlotRepository timeSlotRepo;
	
	@Autowired
	ChargerRepository chargerRepo;
	
	@Autowired
	ReserveRepository reserveRepo;
	
	@Autowired
	MemberRepository memberRepo;
	
	
	public Map<LocalDate,List<TimeSlot>> getReserve(String username) {
		Member member = memberRepo.findById(username).orElseThrow(()->new UsernameNotFoundException("존재하지 않는 유저 입니다."));
		
		List<StoreReservation> stores = reserveRepo.findByMemberOrderByReserveId(member);
		
		return stores.stream().collect(
				Collectors.groupingBy(
						store->store.getSlot().getDate(),
						Collectors.mapping(StoreReservation::getSlot, Collectors.toList())));
	}
	
	//예약
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
		
		//예약가능한지 체크
		//enabled가 true인지 확인
		//사용불가능하다면 오류 발생
		for(TimeSlot slot:slots) {			
			
			if(!slot.isEnabled()) {
				log.error("해당 타임 슬롯 사용중");
				throw new IllegalStateException("해당 타임 슬롯 사용중");
			}
			slot.setEnabled(false);
			
			
			
			//예약정보 저장
			reserveRepo.save(StoreReservation.builder()
					.member(member)
					.slot(slot)
					.reserveDate(LocalDate.now())
					.reseverState(ReseverState.예약완료)
					.build());
			
			//타임슬롯 사용중으로 변경
			timeSlotRepo.save(slot);
		}
	}
		
	//예약 취소
	@Transactional
	public void setTimeSlotCancel(ReserveDto dto,String username) throws AttributeNotFoundException{
		List<Long> sortedSlotIds = dto.getSlotIds().stream().sorted().collect(Collectors.toList());
		
		Member member = memberRepo.findById(username).orElseThrow(()->new UsernameNotFoundException("존재하지 않는 유저네임입니다 : " +  username));
		
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
		
		//예약 되어있는지 체크
		//enabled가 false인지 확인
		//사용 가능하다면 오류 발생
		for(TimeSlot slot:slots) {			
			
			if(slot.isEnabled()) {
				log.error("해당 타임 슬롯 사용 중이지 않음");
				throw new IllegalStateException("해당 타임 슬롯 사용 중이지 않음");
			}
			slot.setEnabled(true);
			
			//타임슬롯 사용가능으로 변경
			timeSlotRepo.save(slot);
		}
		
		//예약한 당사지인지 확인
		List<StoreReservation> reservation = reserveRepo.findByMemberOrderByReserveId(member);
		
		for(StoreReservation resev : reservation) {
			if(!username.equals(resev.getMember().getUsername())) {
				throw new IllegalStateException("예약자 당사자가 아님.");
			}
			
			resev.setUpdateDate(LocalDate.now());
			resev.setReseverState(ReseverState.예약취소);
			
			//예약정보 저장
			reserveRepo.save(resev);
		}
		
		
	}
}
