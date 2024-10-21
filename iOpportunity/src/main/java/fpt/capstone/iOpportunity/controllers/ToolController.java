package fpt.capstone.iOpportunity.controllers;

import fpt.capstone.iOpportunity.dto.request.ProductDTO;
import fpt.capstone.iOpportunity.dto.response.ResponseData;
import fpt.capstone.iOpportunity.dto.response.ResponseError;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/tool")
public class ToolController {

    @PostMapping("/sync-opportunity-campaign")
    public ResponseData<?> createProduct(@RequestBody ProductDTO productDTO) {
        try{
            return new ResponseData<>(HttpStatus.CREATED.value(), "Create product Success", 1);
        }catch (Exception e){
            return new ResponseError(0,  HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}
