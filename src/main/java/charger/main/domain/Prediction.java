package charger.main.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Table(name = "prediction")
@AllArgsConstructor
@NoArgsConstructor
public class Prediction {
	@Id
	private Long id;
	@Column(name="store_time_stamp")
	private LocalDateTime storeTimeStamp;
	@Column(name="station_location")
	private String stationLocation;
	@Column(name = "evse_name")
	private String evseName;
	@Column(name = "y_true")
	private double yTrue;
	@Column(name = "y_pred")
	private double yPred;
	
}
