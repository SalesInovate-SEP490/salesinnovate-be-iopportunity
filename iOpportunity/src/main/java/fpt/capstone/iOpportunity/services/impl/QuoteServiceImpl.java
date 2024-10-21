package fpt.capstone.iOpportunity.services.impl;

import fpt.capstone.iOpportunity.dto.Converter;
import fpt.capstone.iOpportunity.dto.request.OpportunityProductDTO;
import fpt.capstone.iOpportunity.dto.request.quote.QuoteDTO;
import fpt.capstone.iOpportunity.dto.response.ContactRoleResponse;
import fpt.capstone.iOpportunity.dto.response.OpportunityProductResponse;
import fpt.capstone.iOpportunity.dto.response.PageResponse;
import fpt.capstone.iOpportunity.dto.response.quote.QuoteOppProResponse;
import fpt.capstone.iOpportunity.dto.response.quote.QuoteProductResponse;
import fpt.capstone.iOpportunity.dto.response.quote.QuoteReport;
import fpt.capstone.iOpportunity.dto.response.quote.QuoteResponse;
import fpt.capstone.iOpportunity.model.*;
import fpt.capstone.iOpportunity.model.quote.AddressInformation;
import fpt.capstone.iOpportunity.model.quote.Quote;
import fpt.capstone.iOpportunity.model.quote.QuoteOppPro;
import fpt.capstone.iOpportunity.model.quote.QuoteStatus;
import fpt.capstone.iOpportunity.repositories.*;
import fpt.capstone.iOpportunity.repositories.quote.AddressInformationRepository;
import fpt.capstone.iOpportunity.repositories.quote.QuoteOppProRepository;
import fpt.capstone.iOpportunity.repositories.quote.QuoteRepository;
import fpt.capstone.iOpportunity.repositories.quote.QuoteStatusRepository;
import fpt.capstone.iOpportunity.services.OpportunityClientService;
import fpt.capstone.iOpportunity.services.QuoteService;
import fpt.capstone.proto.account.AccountDtoProto;
import fpt.capstone.proto.contact.ContactDtoProto;
import fpt.capstone.proto.user.UserDtoProto;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static fpt.capstone.iOpportunity.services.impl.PriceBookServiceImpl.listToPage;

@Slf4j
@Service
@AllArgsConstructor
public class QuoteServiceImpl implements QuoteService {
    private final OpportunityRepository opportunityRepository;
    private final OpportunityUserRepository opportunityUserRepository;
    private final OpportunityProductRepository opportunityProductRepository;
    private final ProductRepository productRepository;
    private final OpportunityClientService opportunityClientService;
    private final QuoteRepository quoteRepository;
    private final QuoteOppProRepository quoteOppProRepository;
    private final QuoteStatusRepository quoteStatusRepository;
    private final AddressInformationRepository addressInformationRepository;
    private final Converter converter;
    private static final int MINIMUM_LENGTH = 10;
    private final ProductPriceBookRepository productPriceBookRepository;


    @Override
    @Transactional
    public Long createQuote(String userId, Long opportunityId, QuoteDTO dto) {
        try {
            //Kiem tra user co quyen duoc add hay khong
            checkRelationOppAndUser(userId, opportunityId);

            Opportunity opportunity = opportunityRepository.findById(opportunityId).orElse(null);
            if (opportunity == null) throw new RuntimeException("Can not find opportunity");

            BigDecimal total = BigDecimal.ZERO;

            //Fill thong tin cac truong co trong quote
            Quote quote = converter.convertDTOToQuote(dto);
            quote.setQuoteNumber(generateQuoteNumber(opportunity.getOpportunityId()));
            quote.setCreatedBy(userId);
            quote.setCreateDate(LocalDateTime.now());
            quote.setEditBy(userId);
            quote.setEditDate(LocalDateTime.now());
            quote.setIsSync(false);

            quote.setOpportunityId(opportunityId);
            quote.setAccountId(opportunity.getAccountId());

            AccountDtoProto accountDtoProto = opportunityClientService.getAccount(opportunity.getAccountId());
            quote.setBillingName(accountDtoProto.getAccountName());
            quote.setShippingName(accountDtoProto.getAccountName());

            AddressInformation billInformation = mappingAddress(accountDtoProto.getBillingInformation());
            addressInformationRepository.save(billInformation);
            quote.setBillingInformation(billInformation);

            AddressInformation shipInformation = mappingAddress(accountDtoProto.getShippingInformation());
            addressInformationRepository.save(shipInformation);
            quote.setShippingInformation(shipInformation);
            quoteRepository.save(quote);


            //Tao quan he giua quote voi product
            Specification<OpportunityProduct> spec = new Specification<OpportunityProduct>() {
                @Override
                public Predicate toPredicate(Root<OpportunityProduct> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunityId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<OpportunityProduct> list = opportunityProductRepository.findAll(spec);
            for (OpportunityProduct product : list) {
                OpportunityProduct opportunityProduct = OpportunityProduct.builder()
                        .productId(product.getProductId())
                        .quantity(product.getQuantity())
                        .sales_price(product.getSales_price())
                        .date(product.getDate())
                        .line_description(product.getLine_description())
                        .currency(product.getCurrency())
                        .build();
                opportunityProductRepository.save(opportunityProduct);
                //thêm quan hệ giữa quote và opportunityProduct
                QuoteOppPro quoteOppPro = QuoteOppPro.builder()
                        .quoteId(quote.getQuoteId())
                        .opportunityProductId(opportunityProduct.getOpportunityProductId())
                        .build();
                quoteOppProRepository.save(quoteOppPro);
                //Thêm giá trị vào subtotal và totalPrice
                total = total.add(product.getSales_price()
                        .multiply(BigDecimal.valueOf(product.getQuantity())));
            }

            quote.setSubtotal(total);
            quote.setTotalPrice(total);
            quoteRepository.save(quote);
            return quote.getQuoteId();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean editQuote(String userId, Long quoteId, QuoteDTO dto) {
        try {
            Quote quote = quoteRepository.findById(quoteId).orElse(null);
            Opportunity opportunity = opportunityRepository.findById(quote.getOpportunityId()).orElse(null);

            if (quote == null || opportunity == null) return false;
            checkRelationOppAndUser(userId, quote.getOpportunityId());

            Map<String, Object> patchMap = getPatchData(dto);
            if (patchMap.isEmpty()) {
                return true;
            } else {
                for (Map.Entry<String, Object> entry : patchMap.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    Field fieldDTO = ReflectionUtils.findField(QuoteDTO.class, key);

                    if (fieldDTO == null) {
                        continue;
                    }

                    fieldDTO.setAccessible(true);
                    Class<?> type = fieldDTO.getType();

                    try {
                        if (type == long.class && value instanceof String) {
                            value = Long.parseLong((String) value);
                        } else if (type == Long.class && value instanceof String) {
                            value = Long.valueOf((String) value);
                        }
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    switch (key) {
                        case "quoteStatus":
                            quote.setQuoteStatus(quoteStatusRepository.findById((Integer) value).orElse(null));
                            break;
                        case "billingInformation":
                            AddressInformation address = (AddressInformation) value;
                            if (!Objects.equals(address.getStreet(), quote.getBillingInformation().getStreet()))
                                quote.getBillingInformation().setStreet(address.getStreet());
                            if (!Objects.equals(address.getCity(), quote.getBillingInformation().getCity()))
                                quote.getBillingInformation().setCity(address.getCity());
                            if (!Objects.equals(address.getProvince(), quote.getBillingInformation().getProvince()))
                                quote.getBillingInformation().setProvince(address.getProvince());
                            if (!Objects.equals(address.getPostalCode(), quote.getBillingInformation().getPostalCode()))
                                quote.getBillingInformation().setPostalCode(address.getPostalCode());
                            if (!Objects.equals(address.getCountry(), quote.getBillingInformation().getCountry()))
                                quote.getBillingInformation().setCountry(address.getCountry());
                            break;
                        case "shippingInformation":
                            AddressInformation address2 = (AddressInformation) value;
                            if (!Objects.equals(address2.getStreet(), quote.getShippingInformation().getStreet()))
                                quote.getShippingInformation().setStreet(address2.getStreet());
                            if (!Objects.equals(address2.getCity(), quote.getShippingInformation().getCity()))
                                quote.getShippingInformation().setCity(address2.getCity());
                            if (!Objects.equals(address2.getProvince(), quote.getShippingInformation().getProvince()))
                                quote.getShippingInformation().setProvince(address2.getProvince());
                            if (!Objects.equals(address2.getPostalCode(), quote.getShippingInformation().getPostalCode()))
                                quote.getShippingInformation().setPostalCode(address2.getPostalCode());
                            if (!Objects.equals(address2.getCountry(), quote.getShippingInformation().getCountry()))
                                quote.getShippingInformation().setCountry(address2.getCountry());
                            break;
                        default:
                            if (fieldDTO.getType().isAssignableFrom(value.getClass())) {
                                Field field = ReflectionUtils.findField(Quote.class, fieldDTO.getName());
                                assert field != null;
                                field.setAccessible(true);
                                ReflectionUtils.setField(field, quote, value);
                            } else {
                                return false;
                            }
                    }
                }
            }
            quoteRepository.save(quote);

            updatePriceForQuote(quoteId);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean deleteQuote(String userId, Long quoteId) {
        try {
            Quote quote = quoteRepository.findById(quoteId).orElse(null);
            Opportunity opportunity = opportunityRepository.findById(quote.getOpportunityId()).orElse(null);

            if (quote == null || opportunity == null) return false;
            checkRelationOppAndUser(userId, quote.getOpportunityId());

            Specification<QuoteOppPro> spec = new Specification<QuoteOppPro>() {
                @Override
                public Predicate toPredicate(Root<QuoteOppPro> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("quoteId"), quoteId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<QuoteOppPro> list = quoteOppProRepository.findAll(spec);
            quoteOppProRepository.deleteAll(list);

            quoteRepository.delete(quote);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean addProducts(String userId, Long quoteId, List<OpportunityProductDTO> dto) {
        try {
            Quote quote = quoteRepository.findById(quoteId).orElse(null);
            Opportunity opportunity = opportunityRepository.findById(quote.getOpportunityId()).orElse(null);

            if (quote == null || opportunity == null) return false;
            checkRelationOppAndUser(userId, quote.getOpportunityId());

            for (OpportunityProductDTO opportunityProductDTO : dto) {

                Specification<ProductPriceBook> spec = new Specification<ProductPriceBook>() {
                    @Override
                    public Predicate toPredicate(Root<ProductPriceBook> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("productId"), opportunityProductDTO.getProductId()));
                        predicates.add(criteriaBuilder.equal(root.get("priceBookId"), opportunity.getPriceBook()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<ProductPriceBook> list = productPriceBookRepository.findAll(spec);
                if (!list.isEmpty()) {
                    OpportunityProduct opportunityProduct = OpportunityProduct.builder()
                            .productId(opportunityProductDTO.getProductId())
                            .sales_price(opportunityProductDTO.getSales_price())
                            .quantity(opportunityProductDTO.getQuantity())
                            .date(opportunityProductDTO.getDate())
                            .line_description(opportunityProductDTO.getLine_description())
                            .currency(list.get(0).getCurrency().getId())
                            .build();

                    opportunityProductRepository.save(opportunityProduct);
                    //Them quan he giua quote va product
                    QuoteOppPro quoteOppPro = QuoteOppPro.builder()
                            .opportunityProductId(opportunityProduct.getOpportunityProductId())
                            .quoteId(quoteId)
                            .build();
                    quoteOppProRepository.save(quoteOppPro);
                }

                //Update lai cac price cua quote
                updatePriceForQuote(quoteId);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean editProducts(String userId, Long quoteId, List<OpportunityProductDTO> dtoList) {
        try {
            Quote quote = quoteRepository.findById(quoteId).orElse(null);
            Opportunity opportunity = opportunityRepository.findById(quote.getOpportunityId()).orElse(null);

            if (quote == null || opportunity == null) return false;
            checkRelationOppAndUser(userId, quote.getOpportunityId());

            for (OpportunityProductDTO productDTO : dtoList) {
                Map<String, Object> patchMap = getPatchData(productDTO);
                if (patchMap.isEmpty()) {
                    continue;
                }
                OpportunityProduct opportunityProduct = opportunityProductRepository
                        .findById(productDTO.getOpportunityProductId()).orElse(null);
                if (opportunityProduct == null) return false;
                else {
                    for (Map.Entry<String, Object> entry : patchMap.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        Field fieldDTO = ReflectionUtils.findField(OpportunityProductDTO.class, key);

                        if (fieldDTO == null) {
                            continue;
                        }

                        fieldDTO.setAccessible(true);
                        Class<?> type = fieldDTO.getType();

                        try {
                            if (type == long.class && value instanceof String) {
                                value = Long.parseLong((String) value);
                            } else if (type == Long.class && value instanceof String) {
                                value = Long.valueOf((String) value);
                            }
                        } catch (NumberFormatException e) {
                            return false;
                        }
                        switch (key) {
                            case "productId":
                                break;
                            case "opportunityProductId":
                                break;
                            default:
                                if (fieldDTO.getType().isAssignableFrom(value.getClass())) {
                                    Field field = ReflectionUtils.findField(OpportunityProduct.class, fieldDTO.getName());
                                    assert field != null;
                                    field.setAccessible(true);
                                    ReflectionUtils.setField(field, opportunityProduct, value);
                                } else {
                                    return false;
                                }
                        }
                        opportunityProductRepository.save(opportunityProduct);
                    }
                }
            }
            updatePriceForQuote(quoteId);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean deleteProducts(String userId, Long quoteId, Long opportunityProductId) {
        try {
            Quote quote = quoteRepository.findById(quoteId).orElse(null);
            Opportunity opportunity = opportunityRepository.findById(quote.getOpportunityId()).orElse(null);

            if (quote == null || opportunity == null) return false;
            checkRelationOppAndUser(userId, quote.getOpportunityId());

            //Xoa quan he giua quote voi Product
            Specification<QuoteOppPro> spec = new Specification<QuoteOppPro>() {
                @Override
                public Predicate toPredicate(Root<QuoteOppPro> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityProductId"), opportunityProductId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<QuoteOppPro> list = quoteOppProRepository.findAll(spec);
            //Xóa quan he giưa quote và product
            quoteOppProRepository.deleteAll(list);

            //Xóa quan product
            OpportunityProduct product = opportunityProductRepository.findById(opportunityProductId).orElse(null);
            if (product == null) return false;
            opportunityProductRepository.delete(product);
            return true;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public QuoteResponse getDetailQuote(Long quoteId) {
        try {
            Quote quote = quoteRepository.findById(quoteId).orElse(null);
            if (quote == null) return null;
            Opportunity opportunity = opportunityRepository.findById(quote.getOpportunityId()).orElse(null);
            AccountDtoProto protoAccount = opportunityClientService.getAccount(quote.getAccountId());
            UserDtoProto protoCreateUser = opportunityClientService.getUser(quote.getCreatedBy());
            UserDtoProto protoEditUser = opportunityClientService.getUser(quote.getEditBy());
            assert opportunity != null;
            QuoteResponse response = QuoteResponse.builder()
                    .quoteId(quote.getQuoteId())
                    .quoteNumber(quote.getQuoteNumber())
                    .expirationDate(quote.getExpirationDate())
                    .opportunityId(opportunity.getOpportunityId())
                    .opportunityName(opportunity.getOpportunityName())
                    .accountId(protoAccount.getAccountId())
                    .accountName(protoAccount.getAccountName())
                    .isSync(quote.getIsSync())
                    .description(quote.getDescription())
                    .subtotal(quote.getSubtotal())
                    .discount(quote.getDiscount())
                    .totalPrice(quote.getTotalPrice())
                    .tax(quote.getTax())
                    .shippingHandling(quote.getShippingHandling())
                    .grandTotal(quote.getGrandTotal())
                    .email(quote.getEmail())
                    .phone(quote.getPhone())
                    .fax(quote.getFax())
                    .billingName(quote.getBillingName())
                    .shippingName(quote.getShippingName())
                    .billingInformation(quote.getBillingInformation() == null ? null :
                            addressInformationRepository.findById(quote.getBillingInformation().getAddressInformationId()).orElse(null))
                    .shippingInformation(quote.getShippingInformation() == null ? null :
                            addressInformationRepository.findById(quote.getShippingInformation().getAddressInformationId()).orElse(null))
                    .quoteStatus(quote.getQuoteStatus() == null? null: quote.getQuoteStatus())
                    .createdBy(protoCreateUser.getUserId())
                    .createdByName(protoCreateUser.getUserName())
                    .createDate(quote.getCreateDate())
                    .editBy(protoEditUser.getUserId())
                    .editByName(protoEditUser.getUserName())
                    .editDate(quote.getEditDate())
                    .build();
            if (quote.getContactId() != null) {
                ContactDtoProto contactDtoProto = opportunityClientService.getContact(quote.getContactId());
                response.setContactId(contactDtoProto.getContactId());
                response.setContactName(contactDtoProto.getLastName() + " " + contactDtoProto.getLastName());
            }
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PageResponse<?> getAllQuoteInOpportunity(int pageNo, int pageSize, Long opportunityId) {
        try {

            List<Sort.Order> sorts = new ArrayList<>();
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sorts));

            Specification<Quote> spec = new Specification<Quote>() {
                @Override
                public Predicate toPredicate(Root<Quote> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunityId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };

            Page<Quote> quotes = quoteRepository.findAll(spec, pageable);
            return converter.convertToPageResponse(quotes, pageable);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PageResponse<?> getAllQuote(int pageNo, int pageSize, String userId) {
        try {

            List<Sort.Order> sorts = new ArrayList<>();
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sorts));

            List<Quote> listQuote = new ArrayList<>();

            Specification<Opportunity> spec = new Specification<Opportunity>() {
                @Override
                public Predicate toPredicate(Root<Opportunity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    Join<Opportunity, Users> join = root.join("users", JoinType.INNER);
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(join.get("userId"), userId));
                    predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<Opportunity> opportunities = opportunityRepository.findAll(spec);
            for (Opportunity opportunity : opportunities) {
                Specification<Quote> spec1 = new Specification<Quote>() {
                    @Override
                    public Predicate toPredicate(Root<Quote> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunity.getOpportunityId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                listQuote.addAll(quoteRepository.findAll(spec1));
            }

            Page<Quote> listToPage = listToPage(listQuote, pageNo, pageSize);
            return converter.convertToPageResponse(listToPage, pageable);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    @Transactional
    public boolean startSync(String userId, Long quoteId) {
        try{
            //B1:Tat toan bo sync cua cac quote khac
            Quote quote = quoteRepository.findById(quoteId).orElse(null);
            Opportunity opportunity = opportunityRepository.findById(quote.getOpportunityId()).orElse(null);

            if (quote == null || opportunity == null) return false;
            checkRelationOppAndUser(userId, quote.getOpportunityId());

            Specification<Quote> spec = new Specification<Quote>() {
                @Override
                public Predicate toPredicate(Root<Quote> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunity.getOpportunityId()));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<Quote> quotes = quoteRepository.findAll(spec);
            for(Quote i : quotes){
                i.setIsSync(false);
                quoteRepository.save(i);
            }
            quote.setIsSync(true);
            quoteRepository.save(quote);

            //B2:Opp voi OppPro cos quan he 1-n => xoa cac hang trong OppPro co lien quan den Opp
            Specification<OpportunityProduct> spec2 = new Specification<OpportunityProduct>() {
                @Override
                public Predicate toPredicate(Root<OpportunityProduct> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunity.getOpportunityId()));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<OpportunityProduct> opportunityProducts = opportunityProductRepository.findAll(spec2);
            opportunityProductRepository.deleteAll(opportunityProducts);

            //B3:Them OppId vao cac hang co lien quan toi quote sync
            Specification<QuoteOppPro> spec3 = new Specification<QuoteOppPro>() {
                @Override
                public Predicate toPredicate(Root<QuoteOppPro> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("quoteId"), quoteId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<QuoteOppPro> quoteOppPros = quoteOppProRepository.findAll(spec3);
            for(QuoteOppPro i : quoteOppPros){
                OpportunityProduct product = opportunityProductRepository.findById(i.getOpportunityProductId()).orElse(null);
                if (product==null) continue;
                product.setOpportunityId(opportunity.getOpportunityId());
                opportunityProductRepository.save(product);
            }
            return true;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean stopSync(String userId, Long quoteId) {
        try{
            Quote quote = quoteRepository.findById(quoteId).orElse(null);
            Opportunity opportunity = opportunityRepository.findById(quote.getOpportunityId()).orElse(null);

            if (quote == null || opportunity == null) return false;
            checkRelationOppAndUser(userId, quote.getOpportunityId());
            //B1:Tat sync cua quote
            quote.setIsSync(false);
            quoteRepository.save(quote);
            //B2:Opp voi OppPro cos quan he 1-n => xoa cac hang trong OppPro co lien quan den Quote
            Specification<QuoteOppPro> spec3 = new Specification<QuoteOppPro>() {
                @Override
                public Predicate toPredicate(Root<QuoteOppPro> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("quoteId"), quoteId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<QuoteOppPro> quoteOppPros = quoteOppProRepository.findAll(spec3);
            for(QuoteOppPro i : quoteOppPros){
                OpportunityProduct product = opportunityProductRepository.findById(i.getOpportunityProductId()).orElse(null);
                if (product==null) continue;
                product.setOpportunityId(null);
                opportunityProductRepository.save(product);

                OpportunityProduct newProduct = OpportunityProduct.builder()
                        .opportunityId(opportunity.getOpportunityId())
                        .productId(product.getProductId())
                        .quantity(product.getQuantity())
                        .sales_price(product.getSales_price())
                        .date(product.getDate())
                        .line_description(product.getLine_description())
                        .currency(product.getCurrency())
                        .build();
                opportunityProductRepository.save(newProduct);
            }
            return true;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public QuoteOppProResponse getRelatedQuote(Long quoteId) {
        try {
            Quote quote = quoteRepository.findById(quoteId).orElse(null);
            if (quote == null) throw new RuntimeException("Can not find Quote");

            Specification<QuoteOppPro> spec = new Specification<QuoteOppPro>() {
                @Override
                public Predicate toPredicate(Root<QuoteOppPro> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("quoteId"), quoteId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<QuoteOppPro> list = quoteOppProRepository.findAll(spec);
            QuoteOppProResponse response = QuoteOppProResponse.builder()
                    .opportunityId(quote.getOpportunityId())
                    .quoteId(quoteId)
                    .build();
            List<OpportunityProductResponse> products = new ArrayList<>();
            for (QuoteOppPro quoteOppPro : list) {
                OpportunityProduct product = opportunityProductRepository
                        .findById(quoteOppPro.getOpportunityProductId()).orElse(null);
                products.add(converter.OppproToOppproResponse(product));
            }
            response.setProducts(products);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public byte[] generateReport(Long quoteId) {
        try {
            // Bước 1: Lấy dữ liệu báo giá
            QuoteReport quoteReport = getQuoteReport(quoteId);
            if (quoteReport == null) {
                throw new RuntimeException("No data found for quoteId: " + quoteId);
            }

            List<QuoteProductResponse> quoteProductResponses = quoteReport.getQuoteProductResponses();
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(quoteProductResponses);

            // Biên dịch báo cáo
            JasperReport jasperReport = JasperCompileManager.compileReport(new ClassPathResource("Invoice.jrxml").getInputStream());

            // Khai báo các tham số
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("prepareBy", quoteReport.getPrepareBy());
            parameters.put("email", quoteReport.getEmail());
            parameters.put("shipToName", quoteReport.getShipToName());
            parameters.put("shipToNameStreet", quoteReport.getShipToNameStreet());
            parameters.put("shipToNameCity", quoteReport.getShipToNameCity());
            parameters.put("shipToNamePostalCode", quoteReport.getShipToNamePostalCode());
            parameters.put("shipToNameCountry", quoteReport.getShipToNameCountry());
            parameters.put("billToName", quoteReport.getBillToName());
            parameters.put("billToNameStreet", quoteReport.getBillToNameStreet());
            parameters.put("billToNameCity", quoteReport.getBillToNameCity());
            parameters.put("billToNamePostalCode", quoteReport.getBillToNamePostalCode());
            parameters.put("billToNameCountry", quoteReport.getBillToNameCountry());
            parameters.put("createDate", quoteReport.getCreateDate());
            parameters.put("quoteNumber", quoteReport.getQuoteNumber());
            parameters.put("subTotal", quoteReport.getSubTotal());
            parameters.put("discount", quoteReport.getDiscount());
            parameters.put("totalPrice", quoteReport.getTotalPrice());
            parameters.put("grandTotal", quoteReport.getGrandTotal());

            // Tạo đối tượng JasperPrint
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Tạo danh sách JasperPrint
            List<JasperPrint> jasperPrintList = new ArrayList<>();
            jasperPrintList.add(jasperPrint);

            // Xuất báo cáo
            return generateCombinedReport(jasperPrintList);
        } catch (JRException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating report", e);
        }
    }

    @Override
    public List<QuoteStatus> getStatusList() {
        List<QuoteStatus> list = quoteStatusRepository.findAll();
        return list.stream()
                .sorted(Comparator.comparing(QuoteStatus::getQuoteOrder))
                .collect(Collectors.toList());
    }

    private byte[] generateCombinedReport(List<JasperPrint> jasperPrintList) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            JRExporter exporter = new JRPdfExporter();
            exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT_LIST, jasperPrintList);
            exporter.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, stream);

            exporter.exportReport();
            return stream.toByteArray();

        } catch (JRException e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating combined PDF report", e);
        }
    }

    private void checkRelationOppAndUser(String userId, Long opportunityId) {
        Specification<OpportunityUser> spec = new Specification<OpportunityUser>() {
            @Override
            public Predicate toPredicate(Root<OpportunityUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunityId));
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        boolean exists = opportunityUserRepository.exists(spec);
        if (!exists) throw new RuntimeException("Did not been assigned to this opportunity ");
    }

    private AddressInformation mappingAddress(Long addressId) {
        AddressInformation addressInformation = addressInformationRepository.findById(addressId).orElse(null);
        if (addressInformation == null) {
            return new AddressInformation();
        } else {
            return AddressInformation.builder()
                    .street(addressInformation.getStreet())
                    .city(addressInformation.getCity())
                    .province(addressInformation.getProvince())
                    .postalCode(addressInformation.getPostalCode())
                    .country(addressInformation.getCountry())
                    .build();
        }
    }

    private String generateQuoteNumber(Long opportunityId) {
        Specification<Quote> spec = new Specification<Quote>() {
            @Override
            public Predicate toPredicate(Root<Quote> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunityId));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        List<Quote> list = quoteRepository.findAll(spec);
        int length = Math.max(MINIMUM_LENGTH, String.valueOf(list.size() + 1).length());
        return String.format("%0" + length + "d", list.size() + 1);
    }

    private void updatePriceForQuote(Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId).orElse(null);
        if (quote == null) throw new RuntimeException("Cannot find quote");

        Specification<QuoteOppPro> spec = new Specification<QuoteOppPro>() {
            @Override
            public Predicate toPredicate(Root<QuoteOppPro> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("quoteId"), quoteId));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal clone;

        List<QuoteOppPro> list = quoteOppProRepository.findAll(spec);
        for (QuoteOppPro oppPro : list) {
            OpportunityProduct product = opportunityProductRepository.findById(oppPro.getOpportunityProductId()).orElse(null);
            if (product == null) continue;
            subTotal = subTotal.add(product.getSales_price()
                    .multiply(BigDecimal.valueOf(product.getQuantity())));
        }
        quote.setSubtotal(subTotal);
        if (quote.getDiscount() == null) {
            quote.setTotalPrice(subTotal);
            clone = subTotal;
        } else {
            // Tính giá trị giảm giá
            BigDecimal discountAmount = subTotal.multiply(quote.getDiscount()).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);

            // Tính tổng tiền sau khi giảm giá
            BigDecimal total = subTotal.subtract(discountAmount);
            quote.setTotalPrice(total);
            clone = total;
        }

        if (quote.getTax() != null) clone = clone.add(quote.getTax());
        if (quote.getShippingHandling() != null) clone = clone.add(quote.getShippingHandling());
        quote.setGrandTotal(clone);
        quoteRepository.save(quote);
    }

    private Map<String, Object> getPatchData(Object obj) {
        Class<?> objClass = obj.getClass();
        Field[] fields = objClass.getDeclaredFields();
        Map<String, Object> patchMap = new HashMap<>();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value != null) {
                    patchMap.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                log.info(e.getMessage(), e.getCause());
            }
        }
        return patchMap;
    }

    private QuoteReport getQuoteReport(Long quoteId) {
        // Fetch QuoteOpportunity entity
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("QuoteOpportunity not found"));

        QuoteOppProResponse relatedQuote = getRelatedQuote(quoteId);

        List<QuoteProductResponse> productResponses = relatedQuote.getProducts().stream()
                .map(product -> QuoteProductResponse.builder()
                        .productName(product.getProduct().getProductName())
                        .quantity(product.getQuantity())
                        .unitPrice(product.getSales_price().doubleValue())
                        .totalPrice2(product.getQuantity() * product.getSales_price().doubleValue())
                        .listPrice(product.getSales_price().doubleValue())
                        .build())
                .toList();

        // Extract AddressInformation
        AddressInformation billingAddress = quote.getBillingInformation();
        AddressInformation shippingAddress = quote.getShippingInformation();

        // Map to QuoteReport
        return QuoteReport.builder()
                .prepareBy("System")
                .email(quote.getEmail())
                .shipToName(shippingAddress != null ? shippingAddress.getStreet() : null)
                .shipToNameStreet(shippingAddress != null ? shippingAddress.getStreet() : null)
                .shipToNameCity(shippingAddress != null ? shippingAddress.getCity() : null)
                .shipToNamePostalCode(shippingAddress != null ? shippingAddress.getPostalCode() : null)
                .shipToNameCountry(shippingAddress != null ? shippingAddress.getCountry() : null)
                .billToName(billingAddress != null ? billingAddress.getStreet() : null)
                .billToNameStreet(billingAddress != null ? billingAddress.getStreet() : null)
                .billToNameCity(billingAddress != null ? billingAddress.getCity() : null)
                .billToNamePostalCode(billingAddress != null ? billingAddress.getPostalCode() : null)
                .billToNameCountry(billingAddress != null ? billingAddress.getCountry() : null)
                .createDate(quote.getExpirationDate())
                .quoteNumber(quote.getQuoteNumber())
                .subTotal(quote.getSubtotal())  // Adjust as necessary
                .discount(quote.getDiscount())
                .totalPrice(quote.getTotalPrice())
                .grandTotal(quote.getGrandTotal())
                .quoteProductResponses(productResponses)
                .build();
    }

}
