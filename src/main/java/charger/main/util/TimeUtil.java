package charger.main.util;

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
	public class TimeSlot {
	    private LocalTime startTime;
	    private LocalTime endTime;
	}
	
	public List<TimeSlot> generateTimeSlots(LocalTime start, LocalTime end, int slotMinutes){
		
		
		List<TimeSlot> slots = new ArrayList<>();
		LocalTime current = start;
		while(current.isBefore(end)) {
			LocalTime next= current.plusMinutes(30);
			if(next.isAfter(end)) break;
			slots.add(new TimeSlot(current, next));
			current = next;
		}
		
		return slots;
	}
}
