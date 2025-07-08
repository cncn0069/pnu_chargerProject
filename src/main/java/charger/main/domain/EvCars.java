package charger.main.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
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
@Table(name = "ev_cars")
@AllArgsConstructor
@NoArgsConstructor
public class EvCars {
	@Id
	@Column(name="모델명")
	private Integer carId;
	
	@Column(name = "제조사")
	private String brand;
	
	@OneToOne
	@MapsId
	@JoinColumn(name = "모델명",referencedColumnName = "car_id")
	private EVCarModel ModelID;
	
	@Column(name="배터리용량")
	private double batteryW;
	
	@Column(name="최대거리")
	private double maxDis;
	@Column(name="충전시간_급속")
	private Integer fastIdleChargeTime;
	@Column(name="충전시간_완속")
	private Integer slowIdelChargerTime;
	@Column(name="최대속도")
	private Integer maxSpeed;
	@Column(name="급속충전방식")
	private String fastIdelChargerType;
	@Column(name="완속충전방식")
	private String slowIdelChargerType;
	
}
