package charger.main.domain;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table( name = "ev_car_model")
@AllArgsConstructor
@NoArgsConstructor
public class EVCarModel {
	@Id
	@Column(name = "car_id")
	private Long carId;
	@Column(name = "ev_car_model_name")
	private String evCarModelName;
}
