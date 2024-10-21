package fpt.capstone.iOpportunity.repositories.quote;

import fpt.capstone.iOpportunity.model.quote.Quote;
import fpt.capstone.iOpportunity.model.quote.QuoteOppPro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QuoteOppProRepository extends JpaRepository<QuoteOppPro,Long>, JpaSpecificationExecutor<QuoteOppPro> {
}
