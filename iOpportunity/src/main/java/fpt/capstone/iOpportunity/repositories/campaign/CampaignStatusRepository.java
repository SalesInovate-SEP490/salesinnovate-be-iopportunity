package fpt.capstone.iOpportunity.repositories.campaign;

import fpt.capstone.iOpportunity.model.campaign.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignStatusRepository extends JpaRepository<CampaignStatus,Long> {
}
