package ru.hipeoplea.is.lab1.web;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Converts data integrity violations to a conflict response.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest req) {
        return error(
                HttpStatus.CONFLICT,
                req.getRequestURI(),
                "Невозможно удалить: запись связана с другими объектами");
    }

    /**
     * Handles transaction and persistence errors, mapping common cases to
     * validation or foreign-key responses.
     */
    @ExceptionHandler({
        TransactionSystemException.class,
        PersistenceException.class
    })
    public ResponseEntity<Map<String, Object>> handleTx(
            Exception ex,
            HttpServletRequest req) {
        if (isForeignKeyViolation(ex)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(
                            error(
                                    HttpStatus.CONFLICT,
                                    req.getRequestURI(),
                                    "Невозможно удалить: запись связана "
                                            + "с другими объектами"));
        }
        if (hasConstraintValidation(ex)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            error(
                                    HttpStatus.BAD_REQUEST,
                                    req.getRequestURI(),
                                    "Операция отклонена из-за нарушения "
                                            + "ограничений валидации"));
        }
        String msg = rootMessage(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        error(
                                HttpStatus.BAD_REQUEST,
                                req.getRequestURI(),
                                "Операция не выполнена: "
                                        + (msg != null
                                                ? msg
                                                : ex.getMessage())));
    }

    /**
     * Converts bean validation errors to a readable bad-request payload.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleBeanValidation(
            ConstraintViolationException ex, HttpServletRequest req) {
        return error(
                HttpStatus.BAD_REQUEST,
                req.getRequestURI(),
                "Ошибки валидации данных");
    }

    public static boolean isForeignKeyViolation(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur instanceof SQLException sqlEx) {
                String sqlState = sqlEx.getSQLState();
                if ("23503".equals(sqlState)) {
                    return true;
                }
                String m = sqlEx.getMessage();
                if (m != null
                        && m.toLowerCase()
                                .contains("violates foreign key constraint")) {
                    return true;
                }
            }
            cur = cur.getCause();
        }
        return false;
    }

    private boolean hasConstraintValidation(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur instanceof ConstraintViolationException) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    private Map<String, Object> error(
            HttpStatus status, String path, String message) {
        return Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "path", path
        );
    }

    private String rootMessage(Throwable t) {
        Throwable cur = t;
        String last = null;
        while (cur != null) {
            if (cur.getMessage() != null) {
                last = cur.getMessage();
            }
            cur = cur.getCause();
        }
        return last;
    }
}
