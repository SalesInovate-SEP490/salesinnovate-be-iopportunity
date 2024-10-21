package fpt.capstone.iOpportunity.controllers;

import fpt.capstone.iOpportunity.dto.request.*;
import fpt.capstone.iOpportunity.dto.request.quote.QuoteDTO;
import fpt.capstone.iOpportunity.dto.response.ResponseData;
import fpt.capstone.iOpportunity.dto.response.ResponseError;
import fpt.capstone.iOpportunity.services.ProductService;
import fpt.capstone.iOpportunity.services.QuoteService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/quote")
public class QuoteController {
    @Autowired
    private final QuoteService quoteService;

    @PostMapping("/create-quote")
    public ResponseData<?> createQuote(@RequestParam Long opportunityId,
                                         @RequestBody QuoteDTO dto) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            long quote = quoteService.createQuote(userId,opportunityId,dto);
            return new ResponseData<>(HttpStatus.CREATED.value(), "Create quote Success",quote, 1);
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @DeleteMapping("/delete-quote/{quoteId}")
    public ResponseData<?> deleteQuote(@PathVariable(name = "quoteId") Long quoteId) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return quoteService.deleteQuote(userId,quoteId)?
                    new ResponseData<>(HttpStatus.OK.value(), "Delete quote success", 1):
                    new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), "Delete quote fail");
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PatchMapping("/patch-quote/{quoteId}")
    public ResponseData<?> editQuote(@RequestBody QuoteDTO dto, @PathVariable(name = "quoteId") Long quoteId) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return quoteService.editQuote(userId,quoteId, dto) ?
                    new ResponseData<>(HttpStatus.OK.value(), "Update Quote success", 1)
                    : new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Update Quote fail");
        }catch (Exception e){
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/add-product/{quoteId}")
    public ResponseData<?> addProducts(@PathVariable(name = "quoteId") Long quoteId
            ,@RequestBody List<OpportunityProductDTO> dtoList) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return quoteService.addProducts(userId,quoteId,dtoList) ?
                    new ResponseData<>(1, HttpStatus.OK.value(), "add Product to Quote success") :
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "add Product to Quote fail");
        }catch (Exception e){
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PatchMapping("/edit-product/{quoteId}")
    public ResponseData<?> editProducts(@PathVariable(name = "quoteId") Long quoteId
            ,@RequestBody List<OpportunityProductDTO> dtoList) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return quoteService.editProducts(userId,quoteId,dtoList) ?
                    new ResponseData<>(1, HttpStatus.OK.value(), "update product success") :
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "update product fail");
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @DeleteMapping("/delete-product")
    public ResponseData<?> deleteProducts(@RequestParam Long quoteId,@RequestParam Long opportunityProductId) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return quoteService.deleteProducts(userId,quoteId,opportunityProductId)?
                    new ResponseData<>(HttpStatus.OK.value(), "Delete product success", 1):
                    new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), "Delete product fail");
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/{quoteId}")
    public ResponseData<?> getDetailQuote(@PathVariable Long quoteId) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    quoteService.getDetailQuote(quoteId));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "can not get quote");
        }
    }

    @GetMapping("/get-all-opp")
    public ResponseData<?> getAllQuoteInOpportunity(
            @RequestParam(value = "currentPage", defaultValue = "0") int pageNo,
            @RequestParam(value = "perPage", defaultValue = "10") int pageSize,
            @RequestParam Long opportunityId
    ) {
        try{
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    quoteService.getAllQuoteInOpportunity( pageNo, pageSize,opportunityId));
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), "Get list quote failed");
        }
    }

    @GetMapping("/get-all")
    public ResponseData<?> getAllQuote(
            @RequestParam(value = "currentPage", defaultValue = "0") int pageNo,
            @RequestParam(value = "perPage", defaultValue = "10") int pageSize
    ) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    quoteService.getAllQuote( pageNo, pageSize,userId));
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), "Get list quote failed");
        }
    }

    @PatchMapping("/start-sync/{quoteId}")
    public ResponseData<?> startSync(@PathVariable(name = "quoteId") Long quoteId) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return quoteService.startSync(userId,quoteId) ?
                    new ResponseData<>(HttpStatus.OK.value(), "start Sync Quote success", 1)
                    : new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "start Sync Quote fail");
        }catch (Exception e){
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PatchMapping("/stop-sync/{quoteId}")
    public ResponseData<?> stopSync(@PathVariable(name = "quoteId") Long quoteId) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return quoteService.stopSync(userId,quoteId) ?
                    new ResponseData<>(HttpStatus.OK.value(), "stop Sync Quote success", 1)
                    : new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "stop Sync Quote fail");
        }catch (Exception e){
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/related/{quoteId}")
    public ResponseData<?> getRelatedQuote(@PathVariable Long quoteId) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    quoteService.getRelatedQuote(quoteId));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "can not get related quote");
        }
    }

    @GetMapping("/quote-status")
    public ResponseData<?> getStatusList() {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    quoteService.getStatusList());
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "can not get status quote");
        }
    }

    @GetMapping("/export-quote/{quoteId}")
    public ResponseEntity<byte[]> generateReport(@PathVariable Long quoteId) {
        try {
            byte[] bytes = quoteService.generateReport(quoteId);
            String fileName = "Quote_export.pdf";
            String fileNameURL = URLEncoder.encode(fileName, "UTF-8");
            ByteArrayResource resource = new ByteArrayResource(bytes);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename*=UTF-8''" + fileNameURL);
            headers.add("Content-Type", "application/pdf");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(bytes);
        }catch (Exception e){
            throw new RuntimeException("Could not generate report!", e);
        }
    }
}
