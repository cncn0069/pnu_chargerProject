package charger.main.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
	@NotNull(message = "lat이 비었습니다.")
	private Double lat;
	@NotNull(message = "lon이 비었습니다.")
	private Double lon;
	@Min(value = 100,message = "값이 100 미만입니다." )
	@Max(value = 100000,message = "반경이 100km 초과입니다." )
	private int radius;
}
