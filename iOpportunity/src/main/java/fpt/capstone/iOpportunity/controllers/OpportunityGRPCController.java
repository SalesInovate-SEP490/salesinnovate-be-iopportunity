package fpt.capstone.iOpportunity.controllers;

import fpt.capstone.iOpportunity.dto.response.OpportunityResponse;
import fpt.capstone.iOpportunity.services.MemberService;
import fpt.capstone.iOpportunity.services.OpportunityService;
import fpt.capstone.proto.opportunity.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GrpcService
public class OpportunityGRPCController extends OpportunityServiceGrpc.OpportunityServiceImplBase {
    @Autowired
    OpportunityService opportunityService;
    @Autowired
    MemberService memberService;

    @Override
    public void getOpportunity(GetOpportunityRequest request, StreamObserver<GetOpportunityResponse> responseObserver) {
        long opportunityId = request.getOpportunityId();
        OpportunityResponse opportunityResponse = opportunityService.getDetailOpportunity(opportunityId);
        try {
            GetOpportunityResponse getOpportunityResponse;
            if (opportunityResponse != null) {
                OpportunityDtoProto proto = OpportunityDtoProto.newBuilder()
                        .setOpportunityId(opportunityResponse.getOpportunityId())
                        .setOpportunityName(opportunityResponse.getOpportunityName())
                        .setAccountId(opportunityResponse.getAccountId())
                        .build();
                getOpportunityResponse = GetOpportunityResponse.newBuilder()
                        .setResponse(proto)
                        .build();
            } else {
                getOpportunityResponse = GetOpportunityResponse.getDefaultInstance();
            }
            responseObserver.onNext(getOpportunityResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.asRuntimeException());
        }
    }

    @Override
    public void convertCampaignFromLeadToContact(ConvertCampaignFromLeadToContactRequest request,
                                                 StreamObserver<ConvertCampaignFromLeadToContactResponse> responseObserver) {
        long leadId = request.getLeadId();
        long contactId = request.getContactId();
        String userId = request.getUserId();
        boolean convertMember = memberService.convertCampaignFromLeadToContact(userId,leadId, contactId);
        try {
            ConvertCampaignFromLeadToContactResponse response = ConvertCampaignFromLeadToContactResponse.newBuilder()
                    .setResponse(convertMember)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.asRuntimeException());
        }
    }

    @Override
    public void deleteContactRole(DeleteContactRoleRequest request,
                                                 StreamObserver<DeleteContactRoleResponse> responseObserver) {
        long contactId = request.getContactId();
        long opportunityId = request.getOpportunityId();
        boolean convertMember = opportunityService.deleteContactRole(contactId, opportunityId);
        try {
            DeleteContactRoleResponse response = DeleteContactRoleResponse.newBuilder()
                    .setResponse(convertMember)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.asRuntimeException());
        }
    }

    @Override
    public void deleteRelationLeadCampaign(DeleteRelationLeadCampaignRequest request,
                                                 StreamObserver<DeleteRelationLeadCampaignResponse> responseObserver) {
        long leadId = request.getLeadId();
        boolean deleteRelations = memberService.deleteRelationLeadCampaign(leadId);
        try {
            DeleteRelationLeadCampaignResponse response = DeleteRelationLeadCampaignResponse.newBuilder()
                    .setResponse(deleteRelations)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.asRuntimeException());
        }
    }

    @Override
    public void assignUserFollowingAccount(AssignUserFollowingAccountRequest request,
                                           StreamObserver<AssignUserFollowingAccountResponse> responseObserver){
        long accountId = request.getAccountId();
        List<String> listUser = request.getListUserList();
        boolean checkAssign = opportunityService.assignUserFollowingAccount(accountId,listUser);
        try {
            AssignUserFollowingAccountResponse response = AssignUserFollowingAccountResponse.newBuilder()
                    .setResponse(checkAssign)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.asRuntimeException());
        }
    }
}
