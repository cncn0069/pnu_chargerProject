package charger.main.domain.embeded;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReserveTimeId  implements Serializable{
	private Long reserveId;
	private Long timeId;
	 @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (o == null || getClass() != o.getClass()) return false;
	        ReserveTimeId that = (ReserveTimeId) o;
	        return Objects.equals(reserveId, that.reserveId) &&
	               Objects.equals(timeId, that.timeId);
	    }

	    @Override
	    public int hashCode() {
	        return Objects.hash(reserveId, timeId);
	    }
}
