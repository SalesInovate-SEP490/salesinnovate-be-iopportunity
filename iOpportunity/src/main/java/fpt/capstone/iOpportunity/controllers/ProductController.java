package fpt.capstone.iOpportunity.controllers;

import fpt.capstone.iOpportunity.dto.request.OpportunityDTO;
import fpt.capstone.iOpportunity.dto.request.ProductDTO;
import fpt.capstone.iOpportunity.dto.request.ProductFamilyDTO;
import fpt.capstone.iOpportunity.dto.response.ResponseData;
import fpt.capstone.iOpportunity.dto.response.ResponseError;
import fpt.capstone.iOpportunity.services.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/product")
public class ProductController {
    @Autowired
    private final ProductService productService;

    @PostMapping("/create-product")
    public ResponseData<?> createProduct(@RequestBody ProductDTO productDTO) {
        try{
            long product = productService.createProduct(productDTO);
            return new ResponseData<>(HttpStatus.CREATED.value(), "Create product Success",product, 1);
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseData<?> deleteProduct(@PathVariable(name = "id") Long id) {
        try{
            return productService.deleteProduct(id)?
             new ResponseData<>(HttpStatus.OK.value(), "Delete opportunity success", 1):
                    new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), "Delete opportunity fail");
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public ResponseData<?> detailProduct(
            @PathVariable(name = "id") Long id
    ) {
        try{
            return new ResponseData<>(1, HttpStatus.OK.value(), productService.getProductDetail(id));
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/list-product")
    public ResponseData<?> getListProduct(
            @RequestParam(value = "currentPage", defaultValue = "0") int pageNo,
            @RequestParam(value = "perPage", defaultValue = "10") int pageSize
    ) {
        try{
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    productService.getListProducts( pageNo, pageSize));
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), "Get list product failed");
        }
    }

    @GetMapping("/filter")
    public ResponseData<?> filterProduct(Pageable pageable,
                                             @RequestParam(required = false) String[] search) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    productService.filterProduct(pageable, search));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PatchMapping("/patch-product/{id}")
    public ResponseData<?> patchProduct(@RequestBody ProductDTO productDTO, @PathVariable(name = "id") long id) {
        return productService.patchProduct(productDTO, id) ?
                new ResponseData<>(HttpStatus.OK.value(), "Update Product success", 1)
                : new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Update Product fail");
    }

    @GetMapping("/get-pricebooks")
    public ResponseData<?> getListPriceBookByProduct(@RequestParam int pageNo,
                                                     @RequestParam int pageSize,@RequestParam long productId) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    productService.getListPriceBookByProduct(pageNo, pageSize,productId));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/get-productfamily")
    public ResponseData<?> getListProductFamily() {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    productService.getListProductFamily());
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/product-family")
    public ResponseData<?> createProductFamily(@RequestBody ProductFamilyDTO productDTO) {
        return productService.createProductFamily(productDTO) ?
                new ResponseData<>(HttpStatus.OK.value(), "Create Product family success", 1)
                : new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "\"Create Product family fail");
    }

    @DeleteMapping("/product-family/{id}")
    public ResponseData<?> patchProduct(@PathVariable(name = "id") long id) {
        return productService.deleteProductFamily(id) ?
                new ResponseData<>(HttpStatus.OK.value(), "Delete Product family success", 1)
                : new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Delete Product family fail");
    }
}
