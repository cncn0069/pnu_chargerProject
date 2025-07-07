package charger.main.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import charger.main.domain.FavoriteStore;
import charger.main.domain.Member;
import charger.main.domain.StoreInfo;
import charger.main.domain.embeded.FavoriteStoreId;

public interface FavoriteRepository extends JpaRepository<FavoriteStore, FavoriteStoreId>{
	
	
	@Query("select fs.favoriteStoreId.storeId from FavoriteStore fs where fs.favoriteStoreId.username = :username")
	List<String> getByUsername(String username);
}
