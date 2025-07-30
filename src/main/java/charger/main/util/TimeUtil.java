package charger.main.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


public class TimeUtil {
	
	
	@Getter
	@Setter
	@AllArgsConstructor
	public class TimeSlotTem {
	    private LocalTime startTime;
	    private LocalTime endTime;
	}
	
	public List<TimeSlotTem> generateTimeSlots() {
	    int timeSet = 30;

	    List<TimeSlotTem> slots = new ArrayList<>();
	    LocalTime current = LocalTime.MIN;
	    //LocalTime end = LocalTime.MAX.minusNanos(LocalTime.MAX.getNano()); // 23:59:59

	    for (int i = 0; i < 48;i++) {
	        LocalTime next = current.plusMinutes(timeSet);
	        next = next.minusSeconds(1);
	        slots.add(new TimeSlotTem(current, next));
	        current = next.plusSeconds(1);
	    }

	    return slots;
	}
	
	public LocalDateTime changeToAsiaTime(LocalDateTime timeWithZ) {

		ZoneId utcZone = ZoneId.of("UTC");
	    ZoneId zoneKST = ZoneId.of("Asia/Seoul");

	    // 로컬타임을 UTC 기준 ZonedDateTime으로 해석
	    ZonedDateTime utcZoned = timeWithZ.atZone(utcZone);

	    // KST로 변환
	    ZonedDateTime kstZoned = utcZoned.withZoneSameInstant(zoneKST);

	    return kstZoned.toLocalDateTime();
	}
	

}
