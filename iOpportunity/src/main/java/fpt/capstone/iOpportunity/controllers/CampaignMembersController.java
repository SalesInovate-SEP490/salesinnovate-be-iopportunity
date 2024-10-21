package fpt.capstone.iOpportunity.controllers;

import fpt.capstone.iOpportunity.dto.request.campaign.ContactMemberDTO;
import fpt.capstone.iOpportunity.dto.request.campaign.LeadMemberDTO;
import fpt.capstone.iOpportunity.dto.response.ResponseData;
import fpt.capstone.iOpportunity.dto.response.ResponseError;
import fpt.capstone.iOpportunity.services.MemberService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/campaign_members")
public class CampaignMembersController {
    @Autowired
    private final MemberService memberService;

    @GetMapping("/member-status")
    public ResponseData<?> getListCampaignMemberStatus() {
        try{
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    memberService.getListCampaignMemberStatus());
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), "Get list CampaignMemberStatus failed");
        }
    }

    @PostMapping("/member-status")
    public ResponseData<?> createMemberStatus(
            @RequestParam String status
    ) {
        try{
            long opportunity = memberService.createMemberStatus(status);
            return new ResponseData<>(HttpStatus.CREATED.value(), "Create Member Status success",opportunity, 1);
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }

    }

    @PatchMapping("/member-status")
    public ResponseData<?> patchMemberStatus(@RequestParam long id, @RequestParam String status) {
        return memberService.patchMemberStatus(id,status) ?
                new ResponseData<>(HttpStatus.OK.value(), "Update Campaign success", 1)
                : new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Update Campaign fail");
    }

    @DeleteMapping("/member-status")
    public ResponseData<?> deleteMemberStatus(
            @RequestParam Long[] id
    ) {
        try{
            memberService.deleteMemberStatus(id);
            return new ResponseData<>(HttpStatus.OK.value(), "Delete Member Status success", 1);
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/add-lead")
    public ResponseData<?> addLead(
            @RequestBody List<LeadMemberDTO> list
    ) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return memberService.addLead(userId,list)?
             new ResponseData<>(HttpStatus.CREATED.value(), "add lead success", 1):
                    new ResponseData<>(HttpStatus.NOT_FOUND.value(), "add lead fail", 0);
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }

    }

    @PostMapping("/add-contact")
    public ResponseData<?> addContact(
            @RequestBody List<ContactMemberDTO> list
    ) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return memberService.addContact(userId,list)?
                    new ResponseData<>(HttpStatus.CREATED.value(), "add contact success", 1):
                    new ResponseData<>(HttpStatus.NOT_FOUND.value(), "add contact fail", 0);
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @DeleteMapping("/delete-lead")
    public ResponseData<?> deleteLead(
            @RequestBody List<LeadMemberDTO> list
    ) {
        try{
            return memberService.deleteLead(list)?
                    new ResponseData<>(HttpStatus.CREATED.value(), "delete lead success", 1):
                    new ResponseData<>(HttpStatus.NOT_FOUND.value(), "delete lead fail", 0);
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @DeleteMapping("/delete-contact")
    public ResponseData<?> deleteContact(
            @RequestBody List<ContactMemberDTO> list
    ) {
        try{
            return memberService.deleteContact(list)?
                    new ResponseData<>(HttpStatus.CREATED.value(), "delete contact success", 1):
                    new ResponseData<>(HttpStatus.NOT_FOUND.value(), "delete contact fail", 0);
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PatchMapping("/patch-lead")
    public ResponseData<?> patchListLead(
            @RequestBody List<LeadMemberDTO> list
    ) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return memberService.patchListLead(userId,list)?
                    new ResponseData<>(HttpStatus.CREATED.value(), "update leads member  success", 1):
                    new ResponseData<>(HttpStatus.NOT_FOUND.value(), "update leads member fail", 0);
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PatchMapping("/patch-contact")
    public ResponseData<?> patchListContact(
            @RequestBody List<ContactMemberDTO> list
    ) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return memberService.patchListContact(userId,list)?
                    new ResponseData<>(HttpStatus.CREATED.value(), "update contacts member  success", 1):
                    new ResponseData<>(HttpStatus.NOT_FOUND.value(), "update contacts member fail", 0);
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/view-leads")
    public ResponseData<?> viewLeadMember(
            @RequestParam long campaignId,
            @RequestParam(value = "currentPage", defaultValue = "0") int pageNo,
            @RequestParam(value = "perPage", defaultValue = "10") int pageSize
    ) {
        try{
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    memberService.viewLeadMember( campaignId,pageNo, pageSize));
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), "Get list lead campaign failed");
        }
    }

    @GetMapping("/view-contacts")
    public ResponseData<?> viewContactMember(
            @RequestParam long campaignId,
            @RequestParam(value = "currentPage", defaultValue = "0") int pageNo,
            @RequestParam(value = "perPage", defaultValue = "10") int pageSize
    ) {
        try{
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    memberService.viewContactMember( campaignId,pageNo, pageSize));
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), "Get list contact campaign failed");
        }
    }

    @GetMapping("/influenced-opportunities")
    public ResponseData<?> getInfluenceOpportunities(
            @RequestParam(value = "currentPage", defaultValue = "0") int pageNo,
            @RequestParam(value = "perPage", defaultValue = "10") int pageSize,
            @RequestParam long campaignId
    ) {
        try{
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    memberService.getInfluenceOpportunities(campaignId, pageNo, pageSize));
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/by-leads")
    public ResponseData<?> getListCampaignsByLead(
            @RequestParam long leadId,
            @RequestParam(value = "currentPage", defaultValue = "0") int pageNo,
            @RequestParam(value = "perPage", defaultValue = "10") int pageSize
    ) {
        try{
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    memberService.getListCampaignsByLead( leadId,pageNo, pageSize));
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), "Get list lead campaign failed");
        }
    }

    @GetMapping("/by-contact")
    public ResponseData<?> getListCampaignsByContact(
            @RequestParam long contactId,
            @RequestParam(value = "currentPage", defaultValue = "0") int pageNo,
            @RequestParam(value = "perPage", defaultValue = "10") int pageSize
    ) {
        try{
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    memberService.getListCampaignsByContact(contactId,pageNo, pageSize));
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), "Get list lead campaign failed");
        }
    }
}
