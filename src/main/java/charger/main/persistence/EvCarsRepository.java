package charger.main.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import charger.main.domain.EvCars;

public interface EvCarsRepository extends JpaRepository<EvCars, Integer>{
	@Query("select ec from EvCars ec where ec.carId = :carId")
	EvCars getByCarId(Integer carId);
	
	List<EvCars> findByBrand(String brand);
}
