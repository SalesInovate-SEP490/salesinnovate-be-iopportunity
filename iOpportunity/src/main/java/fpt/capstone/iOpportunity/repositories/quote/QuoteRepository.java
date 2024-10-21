package fpt.capstone.iOpportunity.repositories.quote;

import fpt.capstone.iOpportunity.model.PriceBook;
import fpt.capstone.iOpportunity.model.quote.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QuoteRepository extends JpaRepository<Quote,Long>, JpaSpecificationExecutor<Quote> {
}
