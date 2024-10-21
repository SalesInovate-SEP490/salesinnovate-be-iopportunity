package fpt.capstone.iOpportunity.repositories;

import fpt.capstone.iOpportunity.model.ProductPriceBookCurrency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<ProductPriceBookCurrency,Long> {
}
