package fpt.capstone.iOpportunity.services;

import fpt.capstone.proto.account.AccountDtoProto;
import fpt.capstone.proto.account.AccountServiceGrpc;
import fpt.capstone.proto.account.GetAccountRequest;
import fpt.capstone.proto.account.GetAccountResponse;
import fpt.capstone.proto.contact.ContactDtoProto;
import fpt.capstone.proto.contact.ContactServiceGrpc;
import fpt.capstone.proto.contact.GetContactRequest;
import fpt.capstone.proto.contact.GetContactResponse;
import fpt.capstone.proto.lead.GetLeadRequest;
import fpt.capstone.proto.lead.GetLeadResponse;
import fpt.capstone.proto.lead.LeadDtoProto;
import fpt.capstone.proto.lead.LeadServiceGrpc;
import fpt.capstone.proto.opportunity.ConvertCampaignFromLeadToContactRequest;
import fpt.capstone.proto.opportunity.ConvertCampaignFromLeadToContactResponse;
import fpt.capstone.proto.user.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpportunityClientService {

    @GrpcClient("iLead")
    LeadServiceGrpc.LeadServiceBlockingStub stub ;

    @GrpcClient("iContact")
    ContactServiceGrpc.ContactServiceBlockingStub stubContact ;

    @GrpcClient("iAccount")
    AccountServiceGrpc.AccountServiceBlockingStub stubAccount ;

    @GrpcClient("iUser")
    UserServiceGrpc.UserServiceBlockingStub stubUser ;


    public LeadDtoProto getLead (Long leadId){
        GetLeadRequest request = GetLeadRequest.newBuilder()
                .setLeadId(leadId)
                .build();
        GetLeadResponse response = stub.getLead(request);
        return response.getResponse();
    }

    public ContactDtoProto getContact (Long contactId){
        GetContactRequest request = GetContactRequest.newBuilder()
                .setContactId(contactId)
                .build();
        GetContactResponse response = stubContact.getContact(request);
        return response.getResponse();
    }

    public AccountDtoProto getAccount (Long accountId){
        GetAccountRequest request = GetAccountRequest.newBuilder()
                .setAccountId(accountId)
                .build();
        GetAccountResponse response = stubAccount.getAccount(request);
        return response.getResponse();
    }

    public UserDtoProto getUser (String userId){
        GetUserRequest request = GetUserRequest.newBuilder()
                .setUserId(userId)
                .build();
        GetUserResponse response = stubUser.getUser(request);
        return response.getResponse();
    }

    public boolean convertLogCallToOpp(Long leadId, Long opportunityId) {
        ConvertLogCallToOppRequest request = ConvertLogCallToOppRequest.newBuilder()
                .setLeadId(leadId)
                .setOpportunityId(opportunityId)
                .build();
        ConvertLogCallToOppResponse response = stubUser.convertLogCallToOpp(request);
        return response.getResponse();
    }
    public boolean convertLogEmailToOpp(Long leadId, Long opportunityId) {
        ConvertLogEmailToOppRequest request = ConvertLogEmailToOppRequest.newBuilder()
                .setLeadId(leadId)
                .setOpportunityId(opportunityId)
                .build();
        ConvertLogEmailToOppResponse response = stubUser.convertLogEmailToOpp(request);
        return response.getResponse();
    }

    public boolean createNotification (String userId, String content, Long linkedId,
                                       Long notificationType, List<String> listUser){
        CreateNotificationRequest.Builder requestBuilder = CreateNotificationRequest.newBuilder()
                .setUserId(userId)
                .setContent(content)
                .setLinkId(linkedId)
                .setNotificationType(notificationType);

        // Thêm từng phần tử từ listUser vào yêu cầu
        for (String user : listUser) {
            requestBuilder.addListUser(user);  // Sử dụng addListUser cho trường `repeated`
        }

        CreateNotificationResponse response = stubUser.createNotification(requestBuilder.build());
        return response.getResponse();
    }

    public List<String> getUserRoles (String userId){
        GetUserRolesRequest request = GetUserRolesRequest.newBuilder()
                .setUserId(userId)
                .build();
        GetUserRolesResponse response = stubUser.getUserRoles(request);
        return response.getListRolesList();
    }
}
