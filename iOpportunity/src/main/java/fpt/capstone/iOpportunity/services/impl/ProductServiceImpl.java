package fpt.capstone.iOpportunity.services.impl;

import fpt.capstone.iOpportunity.dto.Converter;
import fpt.capstone.iOpportunity.dto.request.OpportunityDTO;
import fpt.capstone.iOpportunity.dto.request.ProductDTO;
import fpt.capstone.iOpportunity.dto.request.ProductFamilyDTO;
import fpt.capstone.iOpportunity.dto.response.PageResponse;
import fpt.capstone.iOpportunity.dto.response.ProductPriceBookResponse;
import fpt.capstone.iOpportunity.dto.response.ProductResponse;
import fpt.capstone.iOpportunity.model.*;
import fpt.capstone.iOpportunity.repositories.*;
import fpt.capstone.iOpportunity.repositories.specification.SpecificationsBuilder;
import fpt.capstone.iOpportunity.services.PriceBookService;
import fpt.capstone.iOpportunity.services.ProductService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fpt.capstone.iOpportunity.services.impl.PriceBookServiceImpl.listToPage;
import static fpt.capstone.iOpportunity.util.AppConst.SEARCH_SPEC_OPERATOR;

@Service
@AllArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final Converter converter;
    private final ProductRepository productRepository;
    private final ProductFamilyRepository productFamilyRepository;
    private final SearchProductRepository searchRepository;
    private final PriceBookRepository priceBookRepository;
    private final ProductPriceBookRepository productPriceBookRepository;

    @Override
    @Transactional
    public Long createProduct(ProductDTO productDTO) {
        try {
            Product product = Product.builder()
                    .productName(productDTO.getProductName())
                    .productCode(productDTO.getProductCode())
                    .productDescription(productDTO.getProductDescription())
                    .productFamily(productDTO.getProductFamily()==null?null:productFamilyRepository.findById(
                            productDTO.getProductFamily()).orElse(null))
                    .isActive(1)
                    .build();
            productRepository.save(product);
            return product.getProductId();
        }catch (Exception e){
            log.info(e.getMessage());
            throw new RuntimeException("Can not create Product");
        }
    }

    @Override
    public PageResponse<?> getListProducts(int pageNo, int pageSize) {

        List<Sort.Order> sorts = new ArrayList<>();
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sorts));

        Page<Product> products = productRepository.findAll( pageable);
        return converter.convertToPageResponse(products, pageable);
    }

    @Override
    public ProductResponse getProductDetail(long id) {
        try {
            Product product = productRepository.findById(id).orElse(null);
            if(product != null){
                return converter.entityToProductResponse(product);
            }
            return null ;
        }catch (Exception e){
            throw new RuntimeException("Failed to create user");
        }
    }

    @Override
    @Transactional
    public Boolean patchProduct(ProductDTO productDTO, long id) {
        Map<String, Object> patchMap = getPatchData(productDTO);
        if (patchMap.isEmpty()) {
            return true;
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find Product with id: " + id));

        if (product != null) {
            for (Map.Entry<String, Object> entry : patchMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                Field fieldDTO = ReflectionUtils.findField(ProductDTO.class, key);

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
                    case "productFamily":
                        product.setProductFamily(productFamilyRepository.findById((Long) value).orElse(null));
                        break;
                    default:
                        if (fieldDTO.getType().isAssignableFrom(value.getClass())) {
                            Field field = ReflectionUtils.findField(Product.class, fieldDTO.getName());
                            assert field != null;
                            field.setAccessible(true);
                            ReflectionUtils.setField(field, product, value);
                        } else {
                            return false;
                        }
                }
            }
            productRepository.save(product);
            return true;
        }
        return false;
    }

    @Override
    public PageResponse<?> filterProduct(Pageable pageable, String[] search) {
        SpecificationsBuilder builder = new SpecificationsBuilder();

        if (search != null) {
            Pattern pattern = Pattern.compile(SEARCH_SPEC_OPERATOR);
            for (String l : search) {
                Matcher matcher = pattern.matcher(l);
                if (matcher.find()) {
                    builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
                }
            }
            Page<Product> page = searchRepository.searchProductsByCriteriaWithJoin(builder.params, pageable);
            return converter.convertToPageResponse(page, pageable);
        }
        return getListProducts(pageable.getPageNumber(), pageable.getPageSize());
    }

    @Override
    @Transactional
    public Boolean deleteProduct(long id) {
        Product product = productRepository.findById(id).orElse(null);
        if(product !=null){
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public PageResponse<?> getListPriceBookByProduct(int pageNo, int pageSize,long productId) {
        try{
            Specification<ProductPriceBook> spec = new Specification<ProductPriceBook>() {
                @Override
                public Predicate toPredicate(Root<ProductPriceBook> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("productId"), productId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<ProductPriceBook> list = productPriceBookRepository.findAll(spec);
            List<ProductPriceBookResponse> priceBooks = new ArrayList<>();
            for(ProductPriceBook productPriceBook : list){
                PriceBook priceBook = priceBookRepository.findById(productPriceBook.getPriceBookId()).orElse(null);

                if(priceBook != null) priceBooks.add(converter.convertToProductPriceBookResponse(productPriceBook));
            }
            Page<ProductPriceBookResponse> pagePriceBook = listToPage(priceBooks, pageNo, pageSize);
            Pageable pageable = PageRequest.of(pageNo, pageSize);

            return converter.convertToPageResponse(pagePriceBook, pageable);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<ProductFamilyDTO> getListProductFamily() {
        try{
            List<ProductFamily> list = productFamilyRepository.findAll();

            return list.stream().map(converter::entityToProductFamilyDTO).toList();
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public Boolean createProductFamily(ProductFamilyDTO productFamilyDTO) {
        try{
            ProductFamily productFamily = ProductFamily.builder()
                    .productFamilyName(productFamilyDTO.getProductFamilyName())
                    .build();
            productFamilyRepository.save(productFamily);
            return true;
        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Boolean deleteProductFamily(long id) {
        try{
            ProductFamily productFamily = productFamilyRepository.findById(id).orElse(null);
            if(productFamily==null) return false ;
            productFamilyRepository.deleteById(id);
            return true;
        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
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
}
