package charger.main.dto;

import java.util.Map;
import java.util.Set;

import charger.main.domain.ConnectorTypes;
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
	private String useTime;
	private double lat;
	private double lng;
	private int minute;
	private String predTag;
	private Boolean parkingFree;
	private Boolean limitYn;
	private int totalChargeNum;
	private Boolean bestChoice;
	int totalFastNum;
	int totalSlowNum;
	int chargeFastNum;
	int chargeSlowNum;
	int totalMidNum;
	int chargeMidNum;
	int totalNacsNum;
	double chargingDemand;
	private int chargeNum;
	private Set<ConnectorTypes> enabledCharger;
	private String busiId;
	private String busiNm;
	Map<String, EvStoreResultDto> chargerInfo;
}
