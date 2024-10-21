package fpt.capstone.iOpportunity.controllers;

import fpt.capstone.iOpportunity.dto.request.StageDTO;
import fpt.capstone.iOpportunity.dto.response.ResponseData;
import fpt.capstone.iOpportunity.dto.response.ResponseError;
import fpt.capstone.iOpportunity.services.ConfigService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/config-opportunity")
public class ConfigController {
    private final ConfigService configService;

    @GetMapping("/all")
    public ResponseData<?> getAllStage(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    configService.getAllOpportunityStage(page, size));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "System error! Please try again later.");
        }
    }

    @PostMapping("/create")
    public ResponseData<?> createStage(
            @RequestBody StageDTO stageDTO
    ) {
        try {
            return configService.createOpportunityStage(stageDTO) ?
                    new ResponseData<>(1, HttpStatus.OK.value(),
                    configService.createOpportunityStage(stageDTO))
                    : new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Create stage failed!");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "System error! Please try again later.");
        }
    }

    @PatchMapping("/update")
    public ResponseData<?> updateStage(
            @RequestBody StageDTO stageDTO
    ) {
        try {
            return configService.updateOpportunityStage(stageDTO) ?
                    new ResponseData<>(1, HttpStatus.OK.value(), "Update stage successfully")
                    : new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Update stage failed!");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "System error! Please try again later.");
        }
    }
}
