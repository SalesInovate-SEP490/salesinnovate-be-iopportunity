package fpt.capstone.iOpportunity.controllers;

import fpt.capstone.iOpportunity.dto.request.OpportunityDTO;
import fpt.capstone.iOpportunity.dto.request.campaign.CampaignDTO;
import fpt.capstone.iOpportunity.dto.response.ResponseData;
import fpt.capstone.iOpportunity.dto.response.ResponseError;
import fpt.capstone.iOpportunity.services.CampaignService;
import fpt.capstone.iOpportunity.services.MemberService;
import fpt.capstone.iOpportunity.services.OpportunityService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/campaign")
public class CampaignController {
    @Autowired
    private final CampaignService campaignService;


    @GetMapping("/get-list-campaign")
    public ResponseData<?> getListCampaigns(
            @RequestParam(value = "currentPage", defaultValue = "0") int pageNo,
            @RequestParam(value = "perPage", defaultValue = "10") int pageSize
    ) {
        try{
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    campaignService.getListCampaigns( pageNo, pageSize));
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), "Get list campaign failed");
        }
    }

    @GetMapping("/{id}")
    public ResponseData<?> getDetailCampaign(
            @PathVariable(name = "id") Long id
    ) {
        try{
            return new ResponseData<>(1, HttpStatus.OK.value(), campaignService.getDetailCampaign(id));
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/create-campaign")
    public ResponseData<?> createCampaign(
            @RequestBody CampaignDTO campaignDTO
    ) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            long opportunity = campaignService.createCampaign(userId,campaignDTO);
            return new ResponseData<>(HttpStatus.CREATED.value(), "Create campaign Success",opportunity, 1);
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public ResponseData<?> patchCampaign(@RequestBody CampaignDTO campaignDTO, @PathVariable(name = "id") long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return campaignService.patchCampaign(userId,id,campaignDTO) ?
                new ResponseData<>(HttpStatus.OK.value(), "Update Campaign success", 1)
                : new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Update Campaign fail");
    }

    @PatchMapping("/patch-list-campaign")
    public ResponseData<?> patchListCampaign(@RequestBody CampaignDTO campaignDTO, @RequestParam Long[] id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return campaignService.patchListCampaign(userId,id,campaignDTO) ?
                new ResponseData<>(HttpStatus.OK.value(), "Update Campaign success", 1)
                : new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Update Campaign fail");
    }

    @DeleteMapping("/delete")
    public ResponseData<?> deleteCampaign(
            @RequestParam Long[] id
    ) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            campaignService.deleteCampaign(userId,id);
            return new ResponseData<>(HttpStatus.OK.value(), "Delete Campaign success", 1);
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/filter")
    public ResponseData<?> filterCampaigns(Pageable pageable,
                                             @RequestParam(required = false) String[] search) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    campaignService.filterCampaigns(pageable, search));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "list filter Campaigns fail");
        }
    }

    @GetMapping("/status")
    public ResponseData<?> getListStatus() {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    campaignService.getListStatus());
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "list filter Campaigns Status fail");
        }
    }

    @GetMapping("/type")
    public ResponseData<?> getCampaignTypeList() {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    campaignService.getCampaignTypeList());
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "list filter Campaigns Type fail");
        }
    }



}
