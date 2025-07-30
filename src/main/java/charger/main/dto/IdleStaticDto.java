package charger.main.dto;

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
public class IdleStaticDto {
	private int totalCharger;
	private int totalUseableCharger;
	private int totalDisableCharger;
	private int[] stat;
}
