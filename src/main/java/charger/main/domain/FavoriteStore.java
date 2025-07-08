package charger.main.domain;

import java.time.LocalDateTime;

import charger.main.domain.embeded.ChargerId;
import charger.main.domain.embeded.FavoriteStoreId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
		name = "favorite_store")
public class FavoriteStore {
	@EmbeddedId
	private FavoriteStoreId favoriteStoreId;
	
	@MapsId("storeId")
	@ManyToOne
	@JoinColumn(name = "stat_id")
	private StoreInfo storeInfo;
	
	@MapsId("username")
	@ManyToOne
	@JoinColumn(name = "username")
	private Member member;
	
	@Column(name = "create_at")
	private LocalDateTime createdAt;
	
	@Enumerated(EnumType.STRING)
	private State state;
	
	private boolean enabled;
}
