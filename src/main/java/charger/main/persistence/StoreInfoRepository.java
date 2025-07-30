package charger.main.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import charger.main.domain.StoreInfo;

public interface StoreInfoRepository extends JpaRepository<StoreInfo, String>,QuerydslPredicateExecutor<StoreInfo>{

	@Query(
			value ="SELECT si.stat_id FROM store_info si"
			          + " WHERE si.lng BETWEEN :minLng AND :maxLng"
			          + " AND si.lat BETWEEN :minLat AND :maxLat"
			          + " AND ST_Distance_Sphere(POINT(si.lng, si.lat), POINT(:lon, :lat)) <= :radius",
			nativeQuery = true)
	List<String> getStatIdByLatLng(double lat, double lon,int radius,double minLng, double maxLng, double minLat, double maxLat);

}
