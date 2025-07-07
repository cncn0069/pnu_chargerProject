package charger.main.domain.embeded;

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
public class FavoriteStoreId {
	private String storeId;
	private String username;
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FavoriteStoreId)) return false;
        FavoriteStoreId that = (FavoriteStoreId) o;
        return Objects.equals(username, that.username) &&
               Objects.equals(storeId, that.storeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, storeId);
    }
}
