package fpt.capstone.iOpportunity.repositories;

import fpt.capstone.iOpportunity.dto.request.OpportunityDTO;
import fpt.capstone.iOpportunity.dto.response.OpportunityResponse;
import fpt.capstone.iOpportunity.model.Opportunity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long>, JpaSpecificationExecutor<Opportunity> {

}
