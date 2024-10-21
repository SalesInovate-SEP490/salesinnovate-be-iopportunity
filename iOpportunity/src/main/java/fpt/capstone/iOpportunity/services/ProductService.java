package fpt.capstone.iOpportunity.services;

import fpt.capstone.iOpportunity.dto.request.ProductDTO;
import fpt.capstone.iOpportunity.dto.request.ProductFamilyDTO;
import fpt.capstone.iOpportunity.dto.response.PageResponse;
import fpt.capstone.iOpportunity.dto.response.ProductResponse;
import fpt.capstone.iOpportunity.model.PriceBook;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Long createProduct(ProductDTO productDTO);
    PageResponse<?> getListProducts(int pageNo, int pageSize);
    ProductResponse getProductDetail(long id);
    Boolean patchProduct(ProductDTO productDTO,long id);
    PageResponse<?> filterProduct(Pageable pageable, String[] search);
    Boolean deleteProduct(long id);
    PageResponse<?> getListPriceBookByProduct(int pageNo, int pageSize,long productId);
    List<ProductFamilyDTO> getListProductFamily();
    Boolean createProductFamily(ProductFamilyDTO productFamilyDTO);
    Boolean deleteProductFamily (long id);
}
