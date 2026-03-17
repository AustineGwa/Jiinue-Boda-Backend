package com.otblabs.jiinueboda.security.generic.exception;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ControllerAdvice
public class ExceptionControler {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleSQLIntegrityConstraintViolationException(
            SQLIntegrityConstraintViolationException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Data integrity violation error occurred"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ResponseEntity<List<ErrorResponse>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        List<ErrorResponse> errors = new ArrayList<>();
        exception.getAllErrors().forEach(e -> {
            errors.add(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getDefaultMessage()));

        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(GenericException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleGenericException(GenericException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage()));
    }

    // InvalidDataAccessApiUsageException

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleInvalidDataAccessApiUsageException(
            InvalidDataAccessApiUsageException exception) {
        exception.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Invalid data access error occurred"));
    }

    //

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage()));
    }


    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleJsonParseExceptionException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "One of the fields is missing its value"));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonErrors(HttpMessageNotReadableException exception) {
        String genericMessage = "Unacceptable JSON " + exception.getMessage();
        String errorDetails = genericMessage;

        if (exception.getCause() != null && exception.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ifx = (InvalidFormatException) exception.getCause();
            if (ifx.getTargetType() != null && ifx.getTargetType().isEnum()) {
                errorDetails = String.format(
                        "Invalid value: '%s' for the field: '%s'. The value must be one of: %s.", ifx.getValue(),
                        ifx.getPath().get(ifx.getPath().size() - 1).getDescription(),
                        Arrays.toString(ifx.getTargetType().getEnumConstants()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorDetails));

        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage()));
    }


    @ExceptionHandler(NumberFormatException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleNumberFormatException(NumberFormatException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Number format error " + exception.getMessage().toLowerCase() + ", integer value expected."));
    }

    // NoHandlerFoundException

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "url path not found"));
    }

    // 400
    @ExceptionHandler({ BadRequestException.class })
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleBadRequests(final RuntimeException ex) {
        return throwException(HttpStatus.BAD_REQUEST, ex);
    }

    // 404
    @ExceptionHandler({ NotFoundException.class })
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> handleNotFound(final RuntimeException ex) {
        return throwException(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler({ UsernameNotFoundException.class })
    @ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<Object> handleUsernameNotFoundException(final RuntimeException ex) {
        return throwException(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }

    @ExceptionHandler({ InvalidInputException.class })
    @ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<Object> handleInvalidInput(final RuntimeException ex) {
        return throwException(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }

    // 401
    @ExceptionHandler({ UnauthorizedException.class })
    @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Object> handleUnauthorized(final RuntimeException ex, final WebRequest request) {
        return throwException(HttpStatus.UNAUTHORIZED, ex);
    }

    //

    private ResponseEntity<Object> throwException(HttpStatus statusCode, Exception ex) {
        return ResponseEntity.status(statusCode).body(new ErrorResponse(statusCode.value(), ex.getMessage()));
    }

}
