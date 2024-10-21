package fpt.capstone.iOpportunity.services;

import fpt.capstone.iOpportunity.dto.request.*;
import fpt.capstone.iOpportunity.dto.response.*;
import fpt.capstone.iOpportunity.model.ContactRole;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OpportunityService {
    Long createOpportunity(String userId, OpportunityDTO opportunityDTO);

    boolean deleteOpportunity(String userId, Long id);

    OpportunityResponse getDetailOpportunity(Long id);

    PageResponse<?> getListOpportunity(String userId, int pageNo, int pageSize);

    List<ForecastDTO> getListForecastCategory();

    List<StageDTO> getListStage();

    List<TypeDTO> getListType();

    List<LeadSourceDTO> getLeadSource();

    PageResponse<?> getListOpportunityByAccount(int pageNo, int pageSize, long accountId);

    PageResponse<?> getListOpportunityByContact(int pageNo, int pageSize, long contactId);

    Long convertNewOpportunity(String userId, ConvertFromLeadDTO convert);

    Long convertExistOpportunity(String userId, long leadId, long contactId, long opportunityId, List<OpportunityUserDTO> userDTOS);

    boolean patchOpportunity(String userId, OpportunityDTO opportunityDTO, long id);

    boolean patchListOpportunity(String userId, Long[] id, OpportunityDTO opportunityDTO);

    PageResponse<?> filterOpportunity(String userId, Pageable pageable, String[] search);

    Boolean addPricebook(String userId, long opportunityId, long pricebookId);

    Integer countProduct(long opportunityId);

    Boolean addProductToOpportunity(String userId, OpportunityPriceBookProductDTO dto);

    OpportunityPriceBookProductResponse getProduct(long opportunityId);

    List<PriceBookResponse> searchPriceBookToAdd(long opportunityId, String search);

    List<ProductPriceBookResponse> searchProductToAdd(long opportunityId, String search);

    boolean patchProduct(String userId, long opportunityId, List<OpportunityProductDTO> listProducts);

    boolean deleteProduct(String userId, long opportunityId, long productId);

    PageResponse<?> getCampaignInfluence(long opportunityId, int pageNo, int pageSize);

    boolean patchCampaignInfluence(String userId, List<CampaignInfluenceDTO> list);

    PageResponse<?> getListContactRole(int pageNo, int pageSize, Long opportunityId);

    boolean addContactRole(String userId, CoOppRelationDTO dto);

    boolean deleteContactRole(Long contactId, Long opportunityId);

    boolean deleteContactRoleByUser(String userId, Long contactId, Long opportunityId);

    boolean editContactRole(String userId, List<CoOppRelationDTO> list);

    boolean setPrimary(String userId, CoOppRelationDTO dto);

    List<ContactRole> getListRole();

    boolean addUserToOpportunity(String userId, Long opportunityId, List<OpportunityUserDTO> userDTOS);

    List<UserResponse> getListUserInOpportunity(Long opportunityId);

    boolean assignUserFollowingAccount(Long accountId, List<String> listUsers);
}
