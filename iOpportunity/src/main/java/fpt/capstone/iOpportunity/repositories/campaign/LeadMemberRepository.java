package fpt.capstone.iOpportunity.repositories.campaign;

import fpt.capstone.iOpportunity.model.campaign.LeadMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LeadMemberRepository extends JpaRepository<LeadMember,Long>, JpaSpecificationExecutor<LeadMember> {
}
