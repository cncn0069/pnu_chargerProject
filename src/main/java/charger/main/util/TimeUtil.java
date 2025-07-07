package charger.main.util;

import java.time.LocalDate;
import java.time.LocalTime;
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

}
