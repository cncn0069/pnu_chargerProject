package charger.main.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

import charger.main.domain.StoreInfo;
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
public class StoreResultsDto {
	private String statNm;
	private String statId;
	private String addr;
	private double lat;
	private double lng;
	private Boolean parkingFree;
	private Boolean limitYn;
	private int totalChargeNum;
	int totalFastNum;
	int totalSlowNum;
	int chargeFastNum;
	int chargeSlowNum;
	int totalMidNum;
	int chargeMidNum;
	int totalNacsNum;
	private int chargeNum;
	private Set<String> enabledCharger;
	private String busiId;
	private String busiNm;
	Map<String, EvStoreResultDto> chargerInfo;
}
