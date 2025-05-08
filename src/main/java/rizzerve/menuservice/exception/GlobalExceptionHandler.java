package rizzerve.menuservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        BindingResult result = ex.getBindingResult();
        ValidationErrorResponse errorResponse = new ValidationErrorResponse();
        
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("Validation error");
        
        result.getFieldErrors().forEach(fieldError -> {
            errorResponse.getErrors().add(
                new ValidationErrorResponse.FieldError(
                    fieldError.getField(), 
                    fieldError.getDefaultMessage()
                )
            );
        });
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ValidationErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        ValidationErrorResponse errorResponse = new ValidationErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage(ex.getMessage());
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
}