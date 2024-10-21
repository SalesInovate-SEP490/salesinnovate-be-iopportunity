package fpt.capstone.iOpportunity.repositories.campaign;

import fpt.capstone.iOpportunity.model.campaign.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CampaignRepository extends JpaRepository<Campaign,Long>, JpaSpecificationExecutor<Campaign> {
}
