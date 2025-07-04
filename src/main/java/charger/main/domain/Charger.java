package charger.main.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import charger.main.domain.embeded.ChargerId;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
		name = "charger")
public class Charger {
	@EmbeddedId
	private ChargerId chargerId;
	private String chgerType;
	private Integer output;
	@MapsId("statId")
	@ManyToOne
	@JoinColumn(name = "stat_id")
	private StoreInfo storeInfo;
}
