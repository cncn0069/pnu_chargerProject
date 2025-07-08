package charger.main.dto;

import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class MapQueryDto {
	//개방이냐 아니냐
	@NotNull(message = "useMap 옵션이 비었습니다.")
	private Boolean useMap;
	private Boolean limitYn;
	private Boolean parkingFree;
	private Boolean canUse;
	@NotNull(message = "outputMin 옵션이 비었습니다.")
	@Min(value = 0, message = "outputMin값이 현재 0미만 입니다.")
	private Integer outputMin;
	@NotNull(message = "outputMax 옵션이 비었습니다.")
	@Max(value = 2001, message = "outputMax이 현재 2000초과 입니다.")
	private Integer outputMax;
	private Set<String> busiId;
	private Set<String> chgerType;
	private String keyWord;
}
