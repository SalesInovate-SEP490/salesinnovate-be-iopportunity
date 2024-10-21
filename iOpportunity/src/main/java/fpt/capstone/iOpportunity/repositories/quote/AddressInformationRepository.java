package fpt.capstone.iOpportunity.repositories.quote;

import fpt.capstone.iOpportunity.model.quote.AddressInformation;
import fpt.capstone.iOpportunity.model.quote.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AddressInformationRepository extends JpaRepository<AddressInformation,Long>, JpaSpecificationExecutor<AddressInformation> {
}
