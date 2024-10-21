package fpt.capstone.iOpportunity.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
public class ResponseData<T> implements Serializable {
    private final int status;
    private  String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
    private final int code;

    /**
     * Response data for the API to retrieve data successfully. For GET, POST only
     * @param status
     * @param message
     * @param data
     * @param code
     */
    public ResponseData(int status, String message, T data, int code) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.code = code;
    }

    /**
     * Response data when the API executes successfully or getting error. For PUT, PATCH, DELETE
     * @param status
     * @param message
     * @param code
     */
    public ResponseData(int status, String message, int code) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

    public ResponseData(int code, int status, T data ) {
        this.code = code;
        this.status = status;
        this.data = data;
    }

}
