package charger.main.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import charger.main.domain.Charger;
import charger.main.domain.embeded.ChargerId;

public interface ChargerRepository extends JpaRepository<Charger, ChargerId> {

}
