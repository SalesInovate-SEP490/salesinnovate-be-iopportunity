package fpt.capstone.iOpportunity.services.impl;

import fpt.capstone.iOpportunity.dto.Converter;
import fpt.capstone.iOpportunity.dto.request.StageDTO;
import fpt.capstone.iOpportunity.dto.response.PageResponse;
import fpt.capstone.iOpportunity.model.Stage;
import fpt.capstone.iOpportunity.repositories.StageRepository;
import fpt.capstone.iOpportunity.services.ConfigService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ConfigServiceImpl implements ConfigService {
    private final StageRepository stageRepository;
    private final Converter converter;

    @Override
    public boolean createOpportunityStage(StageDTO stageDTO) {
        try {
            Stage stage = converter.DTOToStage(stageDTO);
            stageRepository.save(stage);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public PageResponse<?> getAllOpportunityStage(int page, int size) {
        try {
            List<Sort.Order> sorts = new ArrayList<>();
            sorts.add(new Sort.Order(Sort.Direction.ASC, "index"));
            Pageable pageable = PageRequest.of(page, size, Sort.by(sorts));
            Page<Stage> stagePage = stageRepository.findAll(pageable);
            return converter.convertToPageResponseStage(stagePage, pageable);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateOpportunityStage(StageDTO stageDTO) {
        try {
            Stage stage = stageRepository.findById(stageDTO.getId()).orElse(null);
            if (stage == null) {
                return false;
            }
            if (stageDTO.getIndex() != null)
                stage.setIndex(stageDTO.getIndex());
            if (stageDTO.getStageName() != null && !stageDTO.getStageName().isEmpty())
                stage.setStageName(stageDTO.getStageName());
            stageRepository.save(stage);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteOpportunityStage() {
        return false;
    }
}
