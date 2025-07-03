package charger.main.dto;

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
public class CoorDinatesDto {
	private double lat;
	private double lon;
	private int radius;
}
