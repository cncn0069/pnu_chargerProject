package charger.main.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import charger.main.domain.EVCarModel;

public interface EVCarModelRepository extends JpaRepository<EVCarModel, Integer>{
}
