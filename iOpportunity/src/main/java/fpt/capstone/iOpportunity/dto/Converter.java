package fpt.capstone.iOpportunity.dto;

import fpt.capstone.iOpportunity.dto.request.*;
import fpt.capstone.iOpportunity.dto.request.campaign.CampaignDTO;
import fpt.capstone.iOpportunity.dto.request.quote.QuoteDTO;
import fpt.capstone.iOpportunity.dto.response.*;
import fpt.capstone.iOpportunity.dto.response.campaign.CampaignResponse;
import fpt.capstone.iOpportunity.dto.response.quote.QuoteOppProResponse;
import fpt.capstone.iOpportunity.dto.response.quote.QuoteReport;
import fpt.capstone.iOpportunity.model.*;
import fpt.capstone.iOpportunity.model.campaign.Campaign;
import fpt.capstone.iOpportunity.model.quote.AddressInformation;
import fpt.capstone.iOpportunity.model.quote.Quote;
import fpt.capstone.iOpportunity.repositories.*;
import fpt.capstone.iOpportunity.repositories.campaign.CampaignStatusRepository;
import fpt.capstone.iOpportunity.repositories.campaign.CampaignTypeRepository;
import fpt.capstone.iOpportunity.repositories.quote.QuoteRepository;
import fpt.capstone.iOpportunity.repositories.quote.QuoteStatusRepository;
import fpt.capstone.iOpportunity.services.OpportunityClientService;
import fpt.capstone.iOpportunity.services.QuoteService;
import fpt.capstone.proto.account.AccountDtoProto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@AllArgsConstructor
@Component
public class Converter {
    private final ForecastRepository forecastRepository;
    private final StageRepository stageRepository;
    private final TypeRepository typeRepository;
    private final LeadSourceRepository leadSourceRepository;
    private final ProductRepository productRepository;
    private final PriceBookRepository priceBookRepository;
    private final ProductFamilyRepository productFamilyRepository;
    private final CampaignStatusRepository campaignStatusRepository;
    private final CampaignTypeRepository campaignTypeRepository;
    private final CurrencyRepository currencyRepository;
    private final QuoteStatusRepository quoteStatusRepository;
    private final OpportunityClientService opportunityClientService;

    public Opportunity DTOToOpportunity(OpportunityDTO opportunityDTO){
        if (opportunityDTO == null) return null ;
        return Opportunity.builder()
                .userId(opportunityDTO.getUserId())
                .accountId(opportunityDTO.getAccountId())
                .opportunityName(opportunityDTO.getOpportunityName())
                .probability(opportunityDTO.getProbability())
                .forecast(opportunityDTO.getForecast()==null?null:
                        forecastRepository.findById(opportunityDTO.getForecast()).orElse(null))
                .nextStep(opportunityDTO.getNextStep())
                .amount(opportunityDTO.getAmount())
                .closeDate(opportunityDTO.getCloseDate())
                .stage(opportunityDTO.getStage()==null?null:
                        stageRepository.findById(opportunityDTO.getStage()).orElse(null))
                .type(opportunityDTO.getType()==null?null:
                        typeRepository.findById(opportunityDTO.getType()).orElse(null))
                .leadSource(opportunityDTO.getLeadSource()==null?null:
                        leadSourceRepository.findById(opportunityDTO.getLeadSource()).orElse(null))
                .primaryCampaignSourceId(opportunityDTO.getPrimaryCampaignSourceId())
                .description(opportunityDTO.getDescription())
                .lastModifiedBy(opportunityDTO.getLastModifiedBy())
                .editDate(opportunityDTO.getEditDate())
                .createBy(opportunityDTO.getCreateBy())
                .createDate(opportunityDTO.getCreateDate())
                .isDeleted(opportunityDTO.getIsDeleted())
                .build();
    }


    public ForecastDTO entityToForecastDTO(Forecast forecast){
        return ForecastDTO.builder()
                .id(forecast.getForecastCategoryId())
                .forecastName(forecast.getForecastName())
                .build();
    }

    public StageDTO entityToStageDTO(Stage stage){
        return StageDTO.builder()
                .id(stage.getStageId())
                .stageName(stage.getStageName())
                .probability(stage.getProbability())
                .index(stage.getIndex())
                .isClose(stage.getIsClose())
                .build();
    }

    public TypeDTO entityToTypeDTO(Type type){
        return TypeDTO.builder()
                .id(type.getTypeId())
                .typeName(type.getTypeName())
                .build();
    }

    public LeadSourceDTO entityToLeadSourceDTO(LeadSource leadSource){
        return LeadSourceDTO.builder()
                .leadSourceId(leadSource.getLeadSourceId())
                .leadSourceName(leadSource.getLeadSourceName())
                .build();
    }

    public PageResponse<?> convertToPageResponse(Page<?> pageResult, Pageable pageable) {
        return PageResponse.builder()
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .total(pageResult.getTotalElements())
                .items(pageResult.getContent())
                .build();
    }


    public OpportunityResponse entityToOpportunityResponse(Opportunity existedOpportunity) {
        AccountDtoProto proto = opportunityClientService.getAccount(existedOpportunity.getAccountId()==null?0
                :existedOpportunity.getAccountId());
        return OpportunityResponse.builder()
                .opportunityId(existedOpportunity.getOpportunityId())
                .opportunityName(existedOpportunity.getOpportunityName())
                .accountId(existedOpportunity.getAccountId())
                .accountName(proto.getAccountName())
                .amount(existedOpportunity.getAmount())
                .closeDate(existedOpportunity.getCloseDate())
                .opportunityName(existedOpportunity.getOpportunityName())
                .forecast(existedOpportunity.getForecast())
                .stage(existedOpportunity.getStage())
                .type(existedOpportunity.getType())
                .primaryCampaignSourceId(existedOpportunity.getPrimaryCampaignSourceId())
                .nextStep(existedOpportunity.getNextStep())
                .probability(existedOpportunity.getProbability())
                .leadSource(existedOpportunity.getLeadSource())
                .description(existedOpportunity.getDescription())
                .priceBook(existedOpportunity.getPriceBook()==null?null:
                        priceBookRepository.findById(existedOpportunity.getPriceBook()).orElse(null))
                .lastModifiedBy(existedOpportunity.getLastModifiedBy())
                .editDate(existedOpportunity.getEditDate())
                .createBy(existedOpportunity.getCreateBy())
                .createDate(existedOpportunity.getCreateDate())
                .isDeleted(existedOpportunity.getIsDeleted())
                .build();
    }


    public ProductResponse entityToProductResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .productCode(product.getProductCode())
                .productDescription(product.getProductDescription())
                .isActive(product.getIsActive())
                .productFamily(product.getProductFamily()==null?null:
                        productFamilyRepository.findById(product.getProductFamily().getProductFamilyId()).orElse(null))
                .build();
    }

    public PriceBookResponse entityToPriceBookResponse(PriceBook priceBook) {
        return PriceBookResponse.builder()
                .priceBookId(priceBook.getPriceBookId())
                .priceBookName(priceBook.getPriceBookName())
                .priceBookDescription(priceBook.getPriceBookDescription())
                .isActive(priceBook.getIsActive())
                .isStandardPriceBook(priceBook.getIsStandardPriceBook())
                .build();
    }

    public OpportunityProductResponse OppproToOppproResponse(OpportunityProduct opportunityProduct){
        if(opportunityProduct==null) return null;
        return OpportunityProductResponse.builder()
                .opportunityProductId(opportunityProduct.getOpportunityProductId())
                .opportunityId(opportunityProduct.getOpportunityId())
                .product(productRepository.findById(opportunityProduct.getProductId()).orElse(null))
                .quantity(opportunityProduct.getQuantity())
                .sales_price(opportunityProduct.getSales_price())
                .date(opportunityProduct.getDate())
                .line_description(opportunityProduct.getLine_description())
                .currency(opportunityProduct.getCurrency()==null?null:
                        currencyRepository.findById(opportunityProduct.getCurrency()).orElse(null))
                .build();
    }

    public ProductFamilyDTO entityToProductFamilyDTO(ProductFamily productFamily){
        return ProductFamilyDTO.builder()
                .productFamilyId(productFamily.getProductFamilyId())
                .productFamilyName(productFamily.getProductFamilyName())
                .build();
    }

    public ProductPriceBookResponse convertToProductPriceBookResponse(ProductPriceBook productPriceBook) {
        return ProductPriceBookResponse.builder()
                .product(productRepository.findById(productPriceBook.getProductId()).orElse(null))
                .priceBook(priceBookRepository.findById(productPriceBook.getPriceBookId()).orElse(null))
                .listPrice(productPriceBook.getListPrice())
                .useStandardPrice(productPriceBook.getUseStandardPrice())
                .createdBy(productPriceBook.getCreatedBy())
                .editBy(productPriceBook.getEditBy())
                .editDate(productPriceBook.getEditDate())
                .currency(currencyRepository.findById(productPriceBook.getCurrency().getId()).orElse(null))
                .build();
    }
    public CampaignResponse entityToCampaignResponse(Campaign campaign){
        return CampaignResponse.builder()
                .campaignId(campaign.getCampaignId())
                .campaignName(campaign.getCampaignName())
                .isActive(campaign.getIsActive())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .description(campaign.getDescription())
                .num_Sent(campaign.getNum_Sent())
                .budgetedCost(campaign.getBudgetedCost())
                .actualCost(campaign.getActualCost())
                .expectedResponse(campaign.getExpectedResponse())
                .expectedRevenue(campaign.getExpectedRevenue())
                .createdBy(campaign.getCreatedBy())
                .edit_By(campaign.getEditBy())
                .editDate(campaign.getEditDate())
                .campaignStatus(campaign.getCampaignStatus())
                .campaignType(campaign.getCampaignType())
                .build();
    }


    public Campaign convertDTOToCampaign(CampaignDTO campaign) {
        return Campaign.builder()
                .campaignName(campaign.getCampaignName())
                .isActive(campaign.getIsActive())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .description(campaign.getDescription())
                .num_Sent(campaign.getNum_Sent())
                .budgetedCost(campaign.getBudgetedCost())
                .actualCost(campaign.getActualCost())
                .expectedResponse(campaign.getExpectedResponse())
                .expectedRevenue(campaign.getExpectedRevenue())
                .campaignStatus(campaign.getCampaignStatus()==null?null:
                        campaignStatusRepository.findById(campaign.getCampaignStatus()).orElse(null))
                .campaignType(campaign.getCampaignType()==null?null:
                        campaignTypeRepository.findById(campaign.getCampaignType()).orElse(null))
                .build();
    }

    public Stage DTOToStage(StageDTO stageDTO) {
        return Stage.builder()
                .stageId(stageDTO.getId())
                .stageName(stageDTO.getStageName())
                .probability(stageDTO.getProbability())
                .index(stageDTO.getIndex())
                .isClose(stageDTO.getIsClose())
                .build();
    }

    public PageResponse<?> convertToPageResponseStage(Page<Stage> stagePage, Pageable pageable) {
        return PageResponse.builder()
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .total(stagePage.getTotalElements())
                .items(stagePage.getContent())
                .build();
    }

    public Quote convertDTOToQuote (QuoteDTO dto){
        return Quote.builder()
                .quoteName(dto.getQuoteName())
                .expirationDate(dto.getExpirationDate())
                .description(dto.getDescription())
                .discount(dto.getDiscount()==null?BigDecimal.valueOf(0) :dto.getDiscount())
                .tax(dto.getTax()==null? BigDecimal.valueOf(0) :dto.getTax())
                .shippingHandling(dto.getShippingHandling()==null?BigDecimal.valueOf(0) :dto.getShippingHandling())
                .contactId(dto.getContactId())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .fax(dto.getFax())
                .quoteStatus(dto.getQuoteStatus()==null?quoteStatusRepository.findById(1).orElse(null)
                        :quoteStatusRepository.findById(dto.getQuoteStatus()).orElse(null))
                .build();
    }
}
