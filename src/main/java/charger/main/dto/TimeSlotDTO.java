package charger.main.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
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
public class TimeSlotDTO {
	@NotBlank
	private String statId;
	@NotBlank
	private String chgerId;
	private Long timeId;
	@NotBlank
	private LocalDate date;
	private LocalTime startTime;
	private LocalTime endTime;
	private Boolean enabled;
}
