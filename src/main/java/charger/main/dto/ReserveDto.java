package charger.main.dto;

import java.time.LocalDate;
import java.util.Set;

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
public class ReserveDto {
	@NotNull
	private Set<Long> slotIds;
	private Set<Long> reseIds;
	private LocalDate date;
}
