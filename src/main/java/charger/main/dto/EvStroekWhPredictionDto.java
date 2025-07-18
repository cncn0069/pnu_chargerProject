package charger.main.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@ToString
@Builder
public class EvStroekWhPredictionDto {
	private String statId;
	private String chargerId;
	private LocalDate predDate;
	private LocalTime predTime; // 시간만 받을 필드
}
