package com.group.collectionofrecipes.dto.responsedto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_SUCCESS;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Стандартный ответ API")
public class ApiResponse<T> {

    private String status;
    private T data;
    private PaginationInfo pagination;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(FIELD_SUCCESS, data, null);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(FIELD_SUCCESS, null,null);
    }

    public static <T> ApiResponse<T> success(T data, PaginationInfo pagination) {
        return new ApiResponse<>(FIELD_SUCCESS, data, pagination);
    }

    public static <T> ApiResponse<T> unSuccess(HttpStatus status) {
        return new ApiResponse<>(status.getReasonPhrase(), null, null);
    }
}