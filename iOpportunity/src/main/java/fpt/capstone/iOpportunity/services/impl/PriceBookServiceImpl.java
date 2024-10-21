package fpt.capstone.iOpportunity.services.impl;

import com.google.type.Decimal;
import fpt.capstone.iOpportunity.dto.Converter;
import fpt.capstone.iOpportunity.dto.request.OpportunityDTO;
import fpt.capstone.iOpportunity.dto.request.PriceBookDTO;
import fpt.capstone.iOpportunity.dto.request.ProductDTO;
import fpt.capstone.iOpportunity.dto.request.ProductPriceBookDTO;
import fpt.capstone.iOpportunity.dto.response.PageResponse;
import fpt.capstone.iOpportunity.dto.response.PriceBookResponse;
import fpt.capstone.iOpportunity.dto.response.ProductPriceBookResponse;
import fpt.capstone.iOpportunity.dto.response.ProductResponse;
import fpt.capstone.iOpportunity.model.*;
import fpt.capstone.iOpportunity.repositories.*;
import fpt.capstone.iOpportunity.repositories.specification.SpecSearchCriteria;
import fpt.capstone.iOpportunity.repositories.specification.SpecificationsBuilder;
import fpt.capstone.iOpportunity.services.PriceBookService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fpt.capstone.iOpportunity.util.AppConst.SEARCH_SPEC_OPERATOR;

@Service
@AllArgsConstructor
@Slf4j
public class PriceBookServiceImpl implements PriceBookService {
    private final Converter converter;
    private final SearchPriceBookRepository searchRepository;
    private final PriceBookRepository priceBookRepository;
    private final ProductPriceBookRepository productPriceBookRepository;
    private final ProductRepository productRepository;
    private final CurrencyRepository currencyRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Long createPriceBook(PriceBookDTO priceBookDTO) {
        try {
            List<PriceBook> list= priceBookRepository.findAll();
            if(list.isEmpty()) createStandardPriceBook();
            PriceBook priceBook = PriceBook.builder()
                    .priceBookName(priceBookDTO.getPriceBookName())
                    .priceBookDescription(priceBookDTO.getPriceBookDescription())
                    .isActive(1)
                    .isStandardPriceBook(0)
                    .build();
            priceBookRepository.save(priceBook);
            return priceBook.getPriceBookId();
        }catch (Exception e){
            log.info(e.getMessage());
            throw new RuntimeException("Can not create PriceBook");
        }
    }

    @Override
    public PageResponse<?> getListPriceBook(int pageNo, int pageSize) {
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }

        List<Sort.Order> sorts = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sorts));

        Page<PriceBook> priceBooks = priceBookRepository.findAll( pageable);
        return converter.convertToPageResponse(priceBooks, pageable);
    }

    @Override
    public PriceBookResponse getPriceDetail(long id) {
        try {
            PriceBook priceBook = priceBookRepository.findById(id).orElse(null);
            if(priceBook != null){
                return converter.entityToPriceBookResponse(priceBook);
            }
            return null ;
        }catch (Exception e){
            throw new RuntimeException("Failed to create user");
        }
    }

    @Override
    public Boolean patchPriceBook(PriceBookDTO priceBookDTO, long id) {
        Map<String, Object> patchMap = getPatchData(priceBookDTO);
        if (patchMap.isEmpty()) {
            return true;
        }

        PriceBook priceBook = priceBookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find PriceBook with id: " + id));

        if (priceBook != null) {
            for (Map.Entry<String, Object> entry : patchMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                Field fieldDTO = ReflectionUtils.findField(PriceBookDTO.class, key);

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

                if (fieldDTO.getType().isAssignableFrom(value.getClass())) {
                            Field field = ReflectionUtils.findField(PriceBook.class, fieldDTO.getName());
                            assert field != null;
                            field.setAccessible(true);
                            ReflectionUtils.setField(field, priceBook, value);
                } else {
                            return false;
                }

            }
            priceBookRepository.save(priceBook);
            return true;
        }
        return false;
    }

    @Override
    public PageResponse<?> filterPriceBook(Pageable pageable, String[] search) {
        SpecificationsBuilder builder = new SpecificationsBuilder();

        if (search != null) {
            Pattern pattern = Pattern.compile(SEARCH_SPEC_OPERATOR);
            for (String l : search) {
                Matcher matcher = pattern.matcher(l);
                if (matcher.find()) {
                    builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
                }
            }
            Page<PriceBook> page = searchRepository.searchPriceBookByCriteriaWithJoin(builder.params, pageable);
            return converter.convertToPageResponse(page, pageable);
        }
        return getListPriceBook(pageable.getPageNumber(), pageable.getPageSize());
    }

    @Override
    public Boolean deletePriceBook(long id) {
        PriceBook priceBook = priceBookRepository.findById(id).orElse(null);
        if(priceBook !=null && priceBook.getIsStandardPriceBook() ==0){
            priceBookRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public Boolean addProductToPriceBook(String userId,long pricebookId, List<ProductPriceBookDTO> list) {
        try {
            for (ProductPriceBookDTO ppb : list) {
                Product product= productRepository.findById(ppb.getProductId()).orElse(null);
                if(product ==null) throw new NoSuchElementException("Can not find product to add");

                //Check neu trung thì sẽ khong duoc add
                Specification<ProductPriceBook> spec= new Specification<ProductPriceBook>() {
                    @Override
                    public Predicate toPredicate(Root<ProductPriceBook> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("priceBookId"), pricebookId));
                        predicates.add(criteriaBuilder.equal(root.get("productId"), ppb.getProductId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                boolean exist = productPriceBookRepository.exists(spec);
                if(exist) continue;

                ProductPriceBook productPriceBook= ProductPriceBook.builder()
                        .productId(ppb.getProductId())
                        .priceBookId(pricebookId)
                        .listPrice(ppb.getListPrice())
                        .createdBy(userId)
                        .editBy(userId)
                        .editDate(LocalDateTime.now())
                        .currency(currencyRepository.findById(ppb.getCurrency()).orElse(null))
                        .build();
                productPriceBookRepository.save(productPriceBook);
            }
            return true ;
        }catch (Exception e){
            log.info(e.getMessage(),e.getCause());
            throw new RuntimeException("Can not add product to price book");
        }
    }

    @Override
    public Boolean addProductToStandardPriceBook(String userId,long pricebookId, long productId, BigDecimal listprice) {
        //Khi tao moi 1 product thi phai them no vao standard pricebook neu khong thi se khong
        //the add vao pricebook khac
        try{
            if(pricebookId==getStandardId()){
                ProductPriceBook productPriceBook= ProductPriceBook.builder()
                        .productId(productId)
                        .priceBookId(pricebookId)
                        .listPrice(listprice)
                        .createdBy(userId)
                        .editBy(userId)
                        .editDate(LocalDateTime.now())
                        .currency(currencyRepository.findById(1L).orElse(null))
                        .build();
                productPriceBookRepository.save(productPriceBook);
                return true;
            }
            return false;
        }catch (Exception e){
            log.info(e.getMessage(),e.getCause());
            throw new RuntimeException("Can not add product to Standard price book");
        }
    }

    @Override
    public PageResponse<?> getListProductByPriceBook(int pageNo, int pageSize,long pricebookId) {
        try{
            int page = 0;
            if (pageNo > 0) {
                page = pageNo - 1;
            }
            Specification<ProductPriceBook> spec = new Specification<ProductPriceBook>() {
                @Override
                public Predicate toPredicate(Root<ProductPriceBook> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("priceBookId"), pricebookId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<ProductPriceBook> list = productPriceBookRepository.findAll(spec);
            List<ProductPriceBookResponse> products = new ArrayList<>();
            for(ProductPriceBook productPriceBook : list){
                ProductPriceBookResponse response =
                        converter.convertToProductPriceBookResponse(productPriceBook);
                products.add(response);
            }
            Page<ProductPriceBookResponse> pageProduct = listToPage(products, pageNo, pageSize);
            Pageable pageable = PageRequest.of(page, pageSize);

            return converter.convertToPageResponse(pageProduct, pageable);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Product> searchProductToAddPriceBook(long pricebookId, String search) {
        try{
            Specification<ProductPriceBook> spec= new Specification<ProductPriceBook>() {
                @Override
                public Predicate toPredicate(Root<ProductPriceBook> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("priceBookId"), pricebookId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<ProductPriceBook> listProductPriceBooks = productPriceBookRepository.findAll(spec);
            List<Product> productList = getListProductByProductPriceBook(listProductPriceBooks);

            Specification<Product> specProduct = new Specification<Product>() {
                @Override
                public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.like(root.get("productName"), "%"+search+"%"));
                    predicates.add(criteriaBuilder.equal(root.get("isActive"), 1));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<Product> listSearchProduct = productRepository.findAll(specProduct);

            if (productList != null) {
                listSearchProduct.removeAll(productList);
            }
            return listSearchProduct;

        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public ProductPriceBookResponse viewProductByPriceBook(long pricebookId, long productId) {
        try {
            Specification<ProductPriceBook> spec= new Specification<ProductPriceBook>() {
                @Override
                public Predicate toPredicate(Root<ProductPriceBook> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("priceBookId"), pricebookId));
                    predicates.add(criteriaBuilder.equal(root.get("productId"), productId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<ProductPriceBook> listProductPriceBooks = productPriceBookRepository.findAll(spec);
            if(listProductPriceBooks.isEmpty()) return null;
            return converter.convertToProductPriceBookResponse(listProductPriceBooks.get(0));
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Boolean patchProduct(long pricebookId, long productId, ProductPriceBookDTO dto) {
        try {
            Map<String, Object> patchMap = getPatchData(dto);
            if (patchMap.isEmpty()) {
                return true;
            }
            Specification<ProductPriceBook> spec= new Specification<ProductPriceBook>() {
                @Override
                public Predicate toPredicate(Root<ProductPriceBook> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("priceBookId"), pricebookId));
                    predicates.add(criteriaBuilder.equal(root.get("productId"), productId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<ProductPriceBook> listProductPriceBooks = productPriceBookRepository.findAll(spec);
            if (listProductPriceBooks.isEmpty()) return false;
            else {
                ProductPriceBook product = listProductPriceBooks.get(0);
                for (Map.Entry<String, Object> entry : patchMap.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    Field fieldDTO = ReflectionUtils.findField(ProductPriceBookDTO.class, key);

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
                        default:
                            if (fieldDTO.getType().isAssignableFrom(value.getClass())) {
                                Field field = ReflectionUtils.findField(ProductPriceBook.class, fieldDTO.getName());
                                assert field != null;
                                field.setAccessible(true);
                                ReflectionUtils.setField(field, product, value);
                            } else {
                                return false;
                            }
                    }
                    productPriceBookRepository.save(product);
                }
                return true ;
            }
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Boolean deleteProductFromPriceBook(long pricebookId, long productId) {
        try {
            Specification<ProductPriceBook> spec= new Specification<ProductPriceBook>() {
                @Override
                public Predicate toPredicate(Root<ProductPriceBook> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("priceBookId"), pricebookId));
                    predicates.add(criteriaBuilder.equal(root.get("productId"), productId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<ProductPriceBook> listProductPriceBooks = productPriceBookRepository.findAll(spec);
            if (listProductPriceBooks.get(0)==null) return false;
            else {
                productPriceBookRepository.delete(listProductPriceBooks.get(0));
                return true ;
            }
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<ProductPriceBookCurrency> getListCurrency() {
        return currencyRepository.findAll();
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

    private void createStandardPriceBook(){
        PriceBook priceBook =  PriceBook.builder()
                .priceBookName("Standard Price Book")
                .priceBookDescription(null)
                .isActive(1)
                .isStandardPriceBook(1)
                .build();
        priceBookRepository.save(priceBook);
    }

    private long getStandardId(){
        Specification<PriceBook> spec = new Specification<PriceBook>() {
            @Override
            public Predicate toPredicate(Root<PriceBook> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("isStandardPriceBook"), 1);
            }
        };

        List<PriceBook> priceBookList = priceBookRepository.findAll(spec);
        return priceBookList.get(0).getPriceBookId();
    }

    public static <T> Page<T> listToPage(List<T> list, int pageIndex, int pageSize) {
        if (list == null || list.isEmpty() || pageIndex < 0 || pageSize <= 0) {
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(pageIndex, pageSize), 0);
        }

        int start = pageIndex * pageSize;
        if (start >= list.size()) {
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(pageIndex, pageSize), list.size());
        }

        int end = Math.min(start + pageSize, list.size());
        List<T> sublist = list.subList(start, end);
        return new PageImpl<>(sublist, PageRequest.of(pageIndex, pageSize), list.size());
    }

    private List<Product> getListProductByProductPriceBook(List<ProductPriceBook> list){
        List<Product> products = new ArrayList<>();
        for (ProductPriceBook productPriceBook: list){
            Product product = productRepository.findById(productPriceBook.getProductId()).orElse(null);
            if(product != null) products.add(product);
        }
        return products;
    }

}
