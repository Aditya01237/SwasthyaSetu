package com.medicine.SwasthyaSetu.exception;
import com.medicine.SwasthyaSetu.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {

        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                400
        );

        // ADD THESE 3 LINES
        System.out.println("=== EXCEPTION MESSAGE: " + ex.getMessage());
        System.out.println("=== CAUSE: " + ex.getCause());
        if (ex.getCause() != null) ex.getCause().printStackTrace();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();

        ErrorResponse error = new ErrorResponse(errorMessage, 400);

        return ResponseEntity.badRequest().body(error);
    }
}
