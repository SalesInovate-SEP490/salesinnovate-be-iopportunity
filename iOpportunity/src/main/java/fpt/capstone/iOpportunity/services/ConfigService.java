package fpt.capstone.iOpportunity.services;

import fpt.capstone.iOpportunity.dto.request.StageDTO;
import fpt.capstone.iOpportunity.dto.response.PageResponse;

public interface ConfigService {
    boolean createOpportunityStage(StageDTO stageDTO);
    PageResponse<?> getAllOpportunityStage(int page, int size);
    boolean updateOpportunityStage(StageDTO stageDTO);
    boolean deleteOpportunityStage();
}
