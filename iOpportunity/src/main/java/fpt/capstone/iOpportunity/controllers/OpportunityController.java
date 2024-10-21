package fpt.capstone.iOpportunity.controllers;

import fpt.capstone.iOpportunity.dto.request.*;
import fpt.capstone.iOpportunity.dto.response.ResponseData;
import fpt.capstone.iOpportunity.dto.response.ResponseError;
import fpt.capstone.iOpportunity.services.OpportunityService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/opportunity")
public class OpportunityController {

    @Autowired
    private final OpportunityService opportunityService;


    @PostMapping("/create-opportunity")
    public ResponseData<?> createOpportunity(
            @RequestBody OpportunityDTO opportunityDTO
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            long opportunity = opportunityService.createOpportunity(userId,opportunityDTO);
            return new ResponseData<>(HttpStatus.CREATED.value(), "Create Opportunity Success", opportunity, 1);
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }

    }


    @DeleteMapping("/delete/{id}")
    public ResponseData<?> deleteOpportunity(
            @PathVariable(name = "id") Long id
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return opportunityService.deleteOpportunity(userId,id)?
             new ResponseData<>(HttpStatus.OK.value(), "Delete opportunity success", 1):
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Delete opportunity fail");
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public ResponseData<?> detailOpportunity(
            @PathVariable(name = "id") Long id
    ) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(), opportunityService.getDetailOpportunity(id));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/get-list-forecast-category")
    public ResponseData<?> getListForecastCategory() {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(), opportunityService.getListForecastCategory());
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Get list forecast error");
        }
    }

    @GetMapping("/get-list-stage")
    public ResponseData<?> getListStage() {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(), opportunityService.getListStage());
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Get list stage error");
        }
    }

    @GetMapping("/get-list-type")
    public ResponseData<?> getListType() {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(), opportunityService.getListType());
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Get list type error");
        }
    }

    @GetMapping("/get-list-leadsource")
    public ResponseData<?> getListLeadSource() {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(), opportunityService.getLeadSource());
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Get list lead source error");
        }
    }

    @GetMapping("/get-list-opportunity")
    public ResponseData<?> getListOpportunity(
            @RequestParam(value = "currentPage", defaultValue = "0") int pageNo,
            @RequestParam(value = "perPage", defaultValue = "10") int pageSize
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    opportunityService.getListOpportunity(userId,pageNo, pageSize));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Get list opportunity failed");
        }
    }

    @GetMapping("/list-opportunity-by-account")
    public ResponseData<?> getListOpportunityByAccount(@RequestParam(value = "currentPage", defaultValue = "0") int pageNo,
                                                       @RequestParam(value = "perPage", defaultValue = "10") int pageSize,
                                                       @RequestParam long accountId) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    opportunityService.getListOpportunityByAccount(pageNo, pageSize, accountId));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Get list opportunity failed");
        }
    }

    @GetMapping("/list-opportunity-by-contact")
    public ResponseData<?> getListOpportunityByContact(@RequestParam(value = "currentPage", defaultValue = "0") int pageNo,
                                                       @RequestParam(value = "perPage", defaultValue = "10") int pageSize,
                                                       @RequestParam long contactId) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    opportunityService.getListOpportunityByContact(pageNo, pageSize, contactId));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Get list opportunity failed");
        }
    }

    @PostMapping("/convert-new-from-lead")
    public ResponseData<?> convertNewFromLead(@RequestBody ConvertFromLeadDTO convertDTO ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    opportunityService.convertNewOpportunity(userId,convertDTO));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "convert new opportunity failed");
        }
    }

    @PostMapping("/convert-exist-from-lead/{leadId}/{contactId}/{opportunityId}")
    public ResponseData<?> convertExistFromLead(@PathVariable long leadId,@PathVariable long contactId,
                                                @PathVariable long opportunityId,@RequestBody List<OpportunityUserDTO> userDTOS) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    opportunityService.convertExistOpportunity(userId,leadId,contactId, opportunityId,userDTOS));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Convert opportunity failed");
        }
    }

    @PatchMapping("/patch-opportunity/{id}")
    public ResponseData<?> patchOpportunity(@RequestBody OpportunityDTO opportunityDTO, @PathVariable(name = "id") long id) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return opportunityService.patchOpportunity(userId,opportunityDTO, id) ?
                    new ResponseData<>(HttpStatus.OK.value(), "Update Opportunity success", 1)
                    : new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Update Opportunity fail");
        }catch (Exception e){
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }

    }

    @PatchMapping("/patch-list-opportunity")
    public ResponseData<?> patchListOpportunity(@RequestParam Long[] id, @RequestBody OpportunityDTO opportunityDTO) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return opportunityService.patchListOpportunity(userId,id, opportunityDTO) ?
                    new ResponseData<>(1, HttpStatus.OK.value(), "Update Opportunity success") :
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Update Opportunity fail");
        }catch (Exception e){
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }

    }

    @GetMapping("/opportunity-filter")
    public ResponseData<?> filterOpportunity(Pageable pageable,
                                             @RequestParam(required = false) String[] search) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    opportunityService.filterOpportunity(userId,pageable, search));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "list filter Opportunity fail");
        }
    }

    @PostMapping("/add-pricebook")
    public ResponseData<?> addPricebook(@RequestParam long opportunityId, @RequestParam long pricebookId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return opportunityService.addPricebook(userId,opportunityId, pricebookId) ?
                    new ResponseData<>(1, HttpStatus.OK.value(), "add Pricebook success") :
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "add Pricebook fail");
        }catch (Exception e){
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }

    }

    @GetMapping("/count-product")
    public ResponseData<?> countProduct(@RequestParam long opportunityId) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    opportunityService.countProduct(opportunityId));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "can not count products");
        }
    }

    @PostMapping("/add-product")
    public ResponseData<?> addProductToOpportunity(@RequestBody OpportunityPriceBookProductDTO dto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return opportunityService.addProductToOpportunity(userId,dto) ?
                    new ResponseData<>(1, HttpStatus.OK.value(), "add Product to Opportunity success") :
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "add Product to Opportunity fail");
        }catch (Exception e){
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "can not count products");
        }
    }

    @GetMapping("/get-product")
    public ResponseData<?> getProduct(@RequestParam long opportunityId) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    opportunityService.getProduct(opportunityId));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "can not get products");
        }
    }

    @GetMapping("/search-pricebook")
    public ResponseData<?> searchPriceBookToAdd(@RequestParam long opportunityId, @RequestParam(defaultValue = "", required = false) String search) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    opportunityService.searchPriceBookToAdd(opportunityId, search));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "can search pricebook");
        }
    }

    @GetMapping("/search-product")
    public ResponseData<?> searchProductToAdd(@RequestParam long opportunityId,
                                              @RequestParam(defaultValue = "", required = false) String search) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    opportunityService.searchProductToAdd(opportunityId, search));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "can search pricebook");
        }
    }

    @PatchMapping("/{opportunityId}/patch-product")
    public ResponseData<?> patchProduct(@PathVariable long opportunityId, @RequestBody List<OpportunityProductDTO> listProducts) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return opportunityService.patchProduct(userId,opportunityId, listProducts) ?
                new ResponseData<>(1, HttpStatus.OK.value(), "Update Opportunity product success") :
                new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Update Opportunity product fail");
    }

    @DeleteMapping("/{opportunityId}/delete-product/{productId}")
    public ResponseData<?> deleteProduct(@PathVariable long opportunityId, @PathVariable long productId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return opportunityService.deleteProduct(userId,opportunityId, productId) ?
                    new ResponseData<>(HttpStatus.OK.value(), "Delete opportunity product success", 1) :
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Delete opportunity product success");
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/contact-role")
    public ResponseData<?> addContactRole(@RequestBody CoOppRelationDTO dto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return opportunityService.addContactRole(userId,dto) ?
                    new ResponseData<>(1, HttpStatus.OK.value(), "add Contact role success") :
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "add Contact role fail");
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @DeleteMapping("/contact-role")
    public ResponseData<?> deleteContactRole(@RequestParam long contactId, @RequestParam long opportunityId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return opportunityService.deleteContactRoleByUser(userId,contactId, opportunityId) ?
                    new ResponseData<>(1, HttpStatus.OK.value(), "delete Contact role success") :
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "delete Contact role fail");
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PatchMapping("/contact-role")
    public ResponseData<?> editContactRole(@RequestBody List<CoOppRelationDTO> list) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return opportunityService.editContactRole(userId,list) ?
                    new ResponseData<>(1, HttpStatus.OK.value(), "update Contact role success") :
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "update Contact role fail");
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PatchMapping("/contact-role/primary")
    public ResponseData<?> setPrimary(@RequestBody CoOppRelationDTO dto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return opportunityService.setPrimary(userId,dto) ?
                    new ResponseData<>(1, HttpStatus.OK.value(), "set primary Contact role success") :
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "set primary Contact role fail");
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/contact-role")
    public ResponseData<?> getListContactRole(@RequestParam int pageNo, @RequestParam int pageSize,@RequestParam Long opportunityId) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    opportunityService.getListContactRole(pageNo, pageSize, opportunityId));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "can not get contactRole");
        }
    }

    @GetMapping("/get-role")
    public ResponseData<?> getListRole() {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    opportunityService.getListRole());
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "can not get ListRole");
        }
    }

    @GetMapping("/campaign-influence")
    public ResponseData<?> getCampaignInfluence(@RequestParam int pageNo, @RequestParam int pageSize,@RequestParam Long opportunityId) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    opportunityService.getCampaignInfluence( opportunityId,pageNo, pageSize));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "can not get Campaign Influence");
        }
    }

    @PatchMapping("/campaign-influence")
    public ResponseData<?> patchCampaignInfluence(@RequestBody List<CampaignInfluenceDTO> list) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return opportunityService.patchCampaignInfluence(userId,list) ?
                    new ResponseData<>(1, HttpStatus.OK.value(), "update CampaignInfluence success") :
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "update CampaignInfluence fail");
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/add-users/{opportunityId}")
    public ResponseData<?> addUserToOpportunity(@PathVariable Long opportunityId,@RequestBody List<OpportunityUserDTO> userDTOS) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return opportunityService.addUserToOpportunity(userId,opportunityId,userDTOS) ?
                    new ResponseData<>(1, HttpStatus.OK.value(), "add users success") :
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "add users fail");
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/get-user/{opportunityId}")
    public ResponseData<?> getListUserInOpportunity(@PathVariable Long opportunityId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    opportunityService.getListUserInOpportunity(opportunityId));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

}
