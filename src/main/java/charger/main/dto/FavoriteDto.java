package charger.main.dto;

import java.time.LocalDateTime;
import java.util.List;

import charger.main.domain.State;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Data
public class FavoriteDto {
	private StoreResultsDto storeResults;
	private State state;
	private LocalDateTime createAt;
}
