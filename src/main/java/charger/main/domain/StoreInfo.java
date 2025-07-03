package charger.main.domain;

import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@Entity
@Getter
@Setter
@ToString
@Builder
@Table( name = "store_info",
		indexes = {
				@Index(name = "idx_lat_lng", columnList = "lat,lng")
		})
@AllArgsConstructor
@NoArgsConstructor
public class StoreInfo {
	
	@Id
	@Column(name = "stat_id")
	private String statId;
	@Column(name = "stat_nm")
	private String statNm;
	private String addr;
	private double lat;
	private double lng;
	@Column(name = "parking_free")
	private Boolean parkingFree;
	@Column(name = "limit_yn")
	private Boolean limitYn;
	@Enumerated(EnumType.STRING)
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name = "connector_types",
			joinColumns  = @JoinColumn(name  = "stat_id")
			)
	private Set<ConnectorTypes> enabledCharger;
	private String busiId;
	private String busiNm;
}
