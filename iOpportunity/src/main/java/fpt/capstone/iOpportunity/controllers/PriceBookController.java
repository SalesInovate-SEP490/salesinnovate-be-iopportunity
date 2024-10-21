package fpt.capstone.iOpportunity.controllers;

import com.google.type.Decimal;
import fpt.capstone.iOpportunity.dto.request.PriceBookDTO;
import fpt.capstone.iOpportunity.dto.request.ProductDTO;
import fpt.capstone.iOpportunity.dto.request.ProductPriceBookDTO;
import fpt.capstone.iOpportunity.dto.response.ResponseData;
import fpt.capstone.iOpportunity.dto.response.ResponseError;
import fpt.capstone.iOpportunity.services.PriceBookService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/pricebook")
public class PriceBookController {

    @Autowired
    private final PriceBookService priceBookService;

    @PostMapping("/create-pricebook")
    public ResponseData<?> createProduct(@RequestBody PriceBookDTO priceBookDTO) {
        try{
            long priceBook = priceBookService.createPriceBook(priceBookDTO);
            return new ResponseData<>(HttpStatus.CREATED.value(), "Create pricebook Success",priceBook, 1);
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseData<?> deletePriceBook(@PathVariable(name = "id") Long id) {
        try{
            return priceBookService.deletePriceBook(id)?
                    new ResponseData<>(HttpStatus.OK.value(), "Delete priceBook success", 1):
                    new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), "Delete priceBook fail");
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public ResponseData<?> detailPriceBook(
            @PathVariable(name = "id") Long id
    ) {
        try{
            return new ResponseData<>(1, HttpStatus.OK.value(), priceBookService.getPriceDetail(id));
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/list-pricebook")
    public ResponseData<?> getListPriceBook(
            @RequestParam(value = "currentPage", defaultValue = "0") int pageNo,
            @RequestParam(value = "perPage", defaultValue = "10") int pageSize
    ) {
        try{
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    priceBookService.getListPriceBook( pageNo, pageSize));
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), "Get list pricebook failed");
        }
    }

    @GetMapping("/filter")
    public ResponseData<?> filterPriceBook(Pageable pageable,
                                             @RequestParam(required = false) String[] search) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    priceBookService.filterPriceBook(pageable, search));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "list filter pricebook fail");
        }
    }

    @PatchMapping("/patch-pricebook/{id}")
    public ResponseData<?> patchPriceBook(@RequestBody PriceBookDTO priceBookDTO, @PathVariable(name = "id") long id) {
        return priceBookService.patchPriceBook(priceBookDTO, id) ?
                new ResponseData<>(HttpStatus.OK.value(), "Update Product success", 1)
                : new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Update Product fail");
    }

    @PostMapping("/add-product")
    public ResponseData<?> addProductToPriceBook(@RequestParam
                                                     long pricebookId, @RequestBody List<ProductPriceBookDTO> dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return priceBookService.addProductToPriceBook(userId,pricebookId, dto) ?
                new ResponseData<>(HttpStatus.OK.value(), "add Product to pricebook success", 1)
                : new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "add Product to pricebook fail");
    }

    @PostMapping("/add-product-standard")
    public ResponseData<?> addProductToStandardPriceBook(@RequestParam long pricebookId,
                                                         @RequestParam long productId,@RequestParam float listPrice) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return priceBookService.addProductToStandardPriceBook(userId,pricebookId, productId,  BigDecimal.valueOf(listPrice)) ?
                new ResponseData<>(HttpStatus.OK.value(), "add Product to pricebook success", 1)
                : new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "add Product to pricebook fail");
    }

    @GetMapping("/get-products")
    public ResponseData<?> getListProductByPriceBook(@RequestParam int pageNo
            ,@RequestParam int pageSize,@RequestParam long pricebookId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    priceBookService.getListProductByPriceBook(pageNo, pageSize,pricebookId));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "list product fail");
        }
    }

    @GetMapping("/search-products")
    public ResponseData<?> searchProductToAddPriceBook(@RequestParam long pricebookId,@RequestParam(defaultValue = "", required = false) String search) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    priceBookService.searchProductToAddPriceBook(pricebookId,search));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "list product fail");
        }
    }

    @GetMapping("/view-product")
    public ResponseData<?> viewProductByPriceBook(@RequestParam long pricebookId,@RequestParam long productId) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    priceBookService.viewProductByPriceBook(pricebookId,productId));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PatchMapping("/patch-product/{pricebookId}/{productId}")
    public ResponseData<?> patchProduct(@PathVariable long pricebookId
            ,@PathVariable long productId,@RequestBody ProductPriceBookDTO dto) {
        return priceBookService.patchProduct(pricebookId, productId,dto) ?
                new ResponseData<>(HttpStatus.OK.value(), "Patch Product success", 1)
                : new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Patch Product fail");
    }

    @DeleteMapping("/product")
    public ResponseData<?> deleteProduct(@RequestParam long pricebookId,@RequestParam long productId) {
        try{
            return priceBookService.deleteProductFromPriceBook(pricebookId,productId)?
                    new ResponseData<>(HttpStatus.OK.value(), "Delete product success", 1):
                    new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), "Delete product fail");
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/get-currency")
    public ResponseData<?> getListCurrency() {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    priceBookService.getListCurrency());
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "list currency fail");
        }
    }

}
