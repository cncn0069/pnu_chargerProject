package charger.main.dto;

import java.util.Set;

import jakarta.validation.Valid;
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
public class MapInfoResultDto {
	@Valid
	MapQueryDto mapQueryDto;
	@Valid
	CoorDinatesDto coorDinatesDto;
}
