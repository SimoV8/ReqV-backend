package it.sagelab.reqv;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.LinkedHashMap;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request) {
        return getResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    protected ResponseEntity<Object> getResponse(String message, HttpStatus status, HttpServletRequest request) {
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("timestamp", new Date());
        data.put("status", status.value());
        data.put("error", status);
        data.put("message", message);
        data.put("path", request.getRequestURL().toString());

        return new ResponseEntity<>(data, status);
    }
}
