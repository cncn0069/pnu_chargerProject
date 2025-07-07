package charger.main.service;

import java.time.LocalDate;
import java.util.List;

import javax.management.AttributeNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import charger.main.domain.Charger;
import charger.main.domain.StoreInfo;
import charger.main.domain.TimeSlot;
import charger.main.domain.embeded.ChargerId;
import charger.main.dto.TimeSlotDTO;
import charger.main.persistence.ChargerRepository;
import charger.main.persistence.StoreInfoRepository;
import charger.main.persistence.TimeSlotRepository;
import charger.main.util.TimeUtil;
import charger.main.util.TimeUtil.TimeSlotTem;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TimeSlotService {

	@Autowired
	StoreInfoRepository infoRepo;
	
	@Autowired
	TimeSlotRepository timeSlotRepo;
	
	@Autowired
	ChargerRepository chargerRepo;
	
	
public List<TimeSlotDTO> getTimeSlot(String stat_id, String chger_id,LocalDate date){
		
		try {
			StoreInfo storeInfo = infoRepo.findById(stat_id).orElseThrow(()->new AttributeNotFoundException());
			ChargerId chargerId = new ChargerId();
			chargerId.setChgerId(chger_id);
			chargerId.setStatId(storeInfo.getStatId());
			
			Charger charger = chargerRepo.findById(chargerId).orElseThrow(()->new AttributeNotFoundException());
			
			TimeUtil timeUtil = new TimeUtil();
			
			//해당하는 날에 타임슬롯이 있는지 없는지 확인
			//없으면 24시간 타임 슬롯 만들기
			if(timeSlotRepo.findByChargerAndDate(charger, date).isEmpty()){
				List<TimeSlotTem> timeSlots = timeUtil.generateTimeSlots();
				
				
				//이후에 트랜스액션 적용해줘야함
				for(TimeSlotTem ts: timeSlots) {
					timeSlotRepo.save(TimeSlot.builder()
							.startTime(ts.getStartTime())
							.endTime(ts.getEndTime())
							.date(date)
							.charger(charger)
							.enabled(true)
							.build()
							);
				}
			}
			return timeSlotRepo.findByChargerAndDate(charger, date).stream()
					.map(n -> TimeSlotDTO.builder()
							.timeId(n.getTimeId())
							.date(n.getDate())
							.startTime(n.getStartTime())
							.endTime(n.getEndTime())
							.enabled(n.isEnabled())
							.build())
					.toList();
			
		} catch (Exception e) {
			// TODO: handle exception
			log.info("타입 슬롯 만들기 중 오류");
		}
		
		return null;
	}
}
