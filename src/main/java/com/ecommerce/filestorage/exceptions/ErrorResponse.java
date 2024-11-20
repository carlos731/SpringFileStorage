package com.ecommerce.filestorage.exceptions;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private Long timestamp;
    private Integer status;
    private HttpStatus error;
    private String message;
    private String path;
}
