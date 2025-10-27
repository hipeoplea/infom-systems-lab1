package ru.hipeoplea.infomsystemslab1.web;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.sql.SQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, req.getRequestURI(),
                "Невозможно удалить: запись связана с другими объектами");
    }

    @ExceptionHandler({TransactionSystemException.class, PersistenceException.class})
    public org.springframework.http.ResponseEntity<Map<String, Object>> handleTx(Exception ex, HttpServletRequest req) {
        if (isForeignKeyViolation(ex)) {
            return org.springframework.http.ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(error(HttpStatus.CONFLICT, req.getRequestURI(),
                            "Невозможно удалить: запись связана с другими объектами"));
        }
        if (hasConstraintValidation(ex)) {
            return org.springframework.http.ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(error(HttpStatus.BAD_REQUEST, req.getRequestURI(),
                            "Операция отклонена из-за нарушения ограничений валидации"));
        }
        String msg = rootMessage(ex);
        return org.springframework.http.ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error(HttpStatus.BAD_REQUEST, req.getRequestURI(),
                        "Операция не выполнена: " + (msg != null ? msg : ex.getMessage())));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleBeanValidation(ConstraintViolationException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, req.getRequestURI(), "Ошибки валидации данных");
    }

    private boolean isForeignKeyViolation(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur instanceof SQLException sqlEx) {
                String sqlState = sqlEx.getSQLState();
                if ("23503".equals(sqlState)) return true; // FK violation
                String m = sqlEx.getMessage();
                if (m != null && m.toLowerCase().contains("violates foreign key constraint")) return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    private boolean hasConstraintValidation(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur instanceof ConstraintViolationException) return true;
            cur = cur.getCause();
        }
        return false;
    }

    private Map<String, Object> error(HttpStatus status, String path, String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", OffsetDateTime.now().toString());
        map.put("status", status.value());
        map.put("error", status.getReasonPhrase());
        map.put("message", message);
        map.put("path", path);
        return map;
    }

    private String rootMessage(Throwable t) {
        Throwable cur = t;
        String last = null;
        while (cur != null) {
            if (cur.getMessage() != null) last = cur.getMessage();
            cur = cur.getCause();
        }
        return last;
    }
}
