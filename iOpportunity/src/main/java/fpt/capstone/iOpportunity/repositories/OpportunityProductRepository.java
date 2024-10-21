package fpt.capstone.iOpportunity.repositories;

import fpt.capstone.iOpportunity.model.Opportunity;
import fpt.capstone.iOpportunity.model.OpportunityProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OpportunityProductRepository extends JpaRepository<OpportunityProduct,Long>, JpaSpecificationExecutor<OpportunityProduct> {
}
