package charger.main.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import charger.main.domain.ConnectorTypes;
import charger.main.domain.ReseverState;
import charger.main.domain.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@Data
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreReservationDto {
	private Long reserveId; 
	private String username;
	private List<TimeSlot> slot;
	private LocalDate reserveDate;
	private LocalDate updateDate;
	private ReseverState reseverState;
}
