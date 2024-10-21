package fpt.capstone.iOpportunity.repositories.quote;

import fpt.capstone.iOpportunity.model.quote.Quote;
import fpt.capstone.iOpportunity.model.quote.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QuoteStatusRepository extends JpaRepository<QuoteStatus,Integer>, JpaSpecificationExecutor<QuoteStatus> {
}
