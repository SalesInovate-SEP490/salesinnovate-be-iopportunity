package fpt.capstone.iOpportunity.repositories.campaign;

import fpt.capstone.iOpportunity.model.campaign.Campaign;
import fpt.capstone.iOpportunity.model.campaign.CampaignInfluence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CampaignInfluenceRepository extends JpaRepository<CampaignInfluence,Long>,JpaSpecificationExecutor<CampaignInfluence> {
}
