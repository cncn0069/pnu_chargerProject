package charger.main.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import charger.main.domain.Statistics;

public interface StatisticsRepository extends JpaRepository<Statistics, Long>{

}
