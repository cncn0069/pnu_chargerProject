package charger.main.domain.embeded;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class ChargerId implements Serializable{
	
	
	private String statId;
	private String chgerId;
	
	 @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (o == null || getClass() != o.getClass()) return false;
	        ChargerId that = (ChargerId) o;
	        return Objects.equals(statId, that.statId) &&
	               Objects.equals(chgerId, that.chgerId);
	    }

	    @Override
	    public int hashCode() {
	        return Objects.hash(statId, chgerId);
	    }
}