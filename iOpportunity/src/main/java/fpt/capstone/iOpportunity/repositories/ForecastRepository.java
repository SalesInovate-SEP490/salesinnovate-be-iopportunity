package fpt.capstone.iOpportunity.repositories;

import fpt.capstone.iOpportunity.model.Forecast;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForecastRepository extends JpaRepository<Forecast, Long> {
}
