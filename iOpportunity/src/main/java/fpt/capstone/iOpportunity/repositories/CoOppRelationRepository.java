package fpt.capstone.iOpportunity.repositories;

import fpt.capstone.iOpportunity.model.CoOppRelation;
import fpt.capstone.iOpportunity.model.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoOppRelationRepository extends JpaRepository<CoOppRelation,Long>, JpaSpecificationExecutor<CoOppRelation> {
    @Query(value = "select c from CoOppRelation c where c.opportunityId = :opportunity_id and c.contactId = :contact_id")
    List<CoOppRelation> countNumRelation (long opportunity_id, long contact_id);

    @Query(value = "select c from CoOppRelation c where c.contactId = :contact_id")
    List<CoOppRelation> getListOpportunityByContact (long contact_id);
}
