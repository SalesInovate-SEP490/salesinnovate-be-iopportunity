package fpt.capstone.iOpportunity.repositories.campaign;

import fpt.capstone.iOpportunity.model.campaign.ContactMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ContactMemberRepository extends JpaRepository<ContactMember,Long> , JpaSpecificationExecutor<ContactMember> {
}
