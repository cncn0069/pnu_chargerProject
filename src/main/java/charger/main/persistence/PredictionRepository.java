package charger.main.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import charger.main.domain.Prediction;
import io.lettuce.core.dynamic.annotation.Param;

public interface PredictionRepository extends JpaRepository<Prediction, Long>{
}
