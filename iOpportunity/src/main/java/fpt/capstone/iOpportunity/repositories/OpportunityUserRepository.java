package fpt.capstone.iOpportunity.repositories;

import fpt.capstone.iOpportunity.model.Opportunity;
import fpt.capstone.iOpportunity.model.OpportunityUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OpportunityUserRepository extends JpaRepository<OpportunityUser, Long>, JpaSpecificationExecutor<OpportunityUser> {
}
