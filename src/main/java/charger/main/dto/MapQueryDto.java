package charger.main.dto;

import java.util.Map;
import java.util.Set;

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
	private boolean useMap;
	private boolean limitYn;
	private boolean parkingFree;
	private boolean canUse;
	private int outputMin;
	private int outputMax;
	private Set<String> busiId;
	private Set<String> chgerType;
}
