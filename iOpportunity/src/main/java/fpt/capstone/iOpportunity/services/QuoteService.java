package fpt.capstone.iOpportunity.services;

import fpt.capstone.iOpportunity.dto.request.OpportunityProductDTO;
import fpt.capstone.iOpportunity.dto.request.quote.QuoteDTO;
import fpt.capstone.iOpportunity.dto.response.PageResponse;
import fpt.capstone.iOpportunity.dto.response.quote.QuoteOppProResponse;
import fpt.capstone.iOpportunity.dto.response.quote.QuoteResponse;
import fpt.capstone.iOpportunity.model.quote.Quote;
import fpt.capstone.iOpportunity.model.quote.QuoteOppPro;
import fpt.capstone.iOpportunity.model.quote.QuoteStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuoteService {
    Long createQuote (String userId,Long opportunityId, QuoteDTO dto);
    boolean editQuote (String userId,Long quoteId,QuoteDTO dto);
    boolean deleteQuote (String userId,Long quoteId);
    boolean addProducts (String userId, Long quoteId, List<OpportunityProductDTO> dtoList);
    boolean editProducts (String userId, Long quoteId, List<OpportunityProductDTO> dtoList);
    boolean deleteProducts (String userId, Long quoteId,Long opportunityProductId);
    QuoteResponse getDetailQuote (Long quoteId);
    PageResponse<?> getAllQuoteInOpportunity (int pageNo, int pageSize,Long opportunityId);
    PageResponse<?> getAllQuote (int pageNo, int pageSize,String userId);
    boolean startSync (String userId, Long quoteId);
    boolean stopSync (String userId, Long quoteId);
    QuoteOppProResponse getRelatedQuote (Long quoteId);
    byte[] generateReport(Long quoteId);
    List<QuoteStatus> getStatusList();
}
