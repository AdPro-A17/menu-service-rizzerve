package rizzerve.menuservice.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ValidationErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String message;
    private List<FieldError> errors = new ArrayList<>();

    @Getter
    @Setter
    public static class FieldError {
        private String field;
        private String message;
        
        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}