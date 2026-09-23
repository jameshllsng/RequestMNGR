package br.com.requestmngr.shared.api;

import br.com.requestmngr.purchaserequest.service.DuplicatePurchaseRequestNumberException;
import br.com.requestmngr.purchaserequest.service.PurchaseRequestNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(PurchaseRequestNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(PurchaseRequestNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Requisição não encontrada.");
    }

    @ExceptionHandler(DuplicatePurchaseRequestNumberException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateRequestNumber(DuplicatePurchaseRequestNumberException exception) {
        return problem(HttpStatus.CONFLICT, "Já existe uma requisição com este número.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String field = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
            errors.put(field, error.getDefaultMessage());
        });

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Existem campos inválidos na requisição.");
        problemDetail.setTitle("Dados inválidos");
        problemDetail.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problemDetail);
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(status.getReasonPhrase());
        return ResponseEntity.status(status).body(problemDetail);
    }
}
