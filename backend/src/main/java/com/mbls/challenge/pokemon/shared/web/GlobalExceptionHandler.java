package com.mbls.challenge.pokemon.shared.web;

import com.mbls.challenge.pokemon.catalog.domain.exception.PokemonNotFoundException;
import com.mbls.challenge.pokemon.collection.domain.exception.CollectionConflictException;
import com.mbls.challenge.pokemon.collection.domain.exception.PokemonAlreadyInCollectionException;
import com.mbls.challenge.pokemon.collection.domain.exception.PokemonNotInCollectionException;
import com.mbls.challenge.pokemon.collection.domain.exception.UnknownPokemonException;
import com.mbls.challenge.pokemon.trainer.domain.exception.EmailAlreadyRegisteredException;
import com.mbls.challenge.pokemon.trainer.domain.exception.InvalidCredentialsException;
import com.mbls.challenge.pokemon.trainer.domain.exception.UsernameAlreadyTakenException;
import com.mbls.challenge.pokemon.trainer.domain.exception.WeakPasswordException;
import com.mbls.challenge.pokemon.shared.domain.InvalidValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

/**
 * Translates domain exceptions into RFC 7807 (application/problem+json) responses.
 *
 * Extends {@link ResponseEntityExceptionHandler} rather than adding a blanket
 * {@code @ExceptionHandler(Exception.class)} on top of a plain {@code
 * @RestControllerAdvice}: that base class already maps Spring MVC's own
 * exceptions (malformed JSON, a non-numeric path variable, an unsupported
 * HTTP method, ...) to the correct 4xx {@link ProblemDetail}, so this class
 * only has to add handling for this application's own domain exceptions on
 * top - it must not swallow the framework's handling of its own exceptions.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({UsernameAlreadyTakenException.class, EmailAlreadyRegisteredException.class,
            PokemonAlreadyInCollectionException.class, CollectionConflictException.class})
    public ProblemDetail handleConflict(RuntimeException ex) {
        return problemDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleUnauthorized(RuntimeException ex) {
        return problemDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler({PokemonNotFoundException.class, PokemonNotInCollectionException.class,
            UnknownPokemonException.class})
    public ProblemDetail handleNotFound(RuntimeException ex) {
        return problemDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({WeakPasswordException.class, InvalidValueException.class})
    public ProblemDetail handleBadRequest(RuntimeException ex) {
        return problemDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, WebRequest request) {
        log.error("Unhandled exception on {}", request.getDescription(false), ex);
        return problemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "an unexpected error occurred");
    }

    /**
     * Spring's default {@link ProblemDetail} for a validation failure has a
     * generic "Invalid request content." detail; override it with the actual
     * field errors, which is what the frontend surfaces to the trainer.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                   HttpHeaders headers, HttpStatusCode status,
                                                                   WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("validation failed");

        return handleExceptionInternal(ex, problemDetail(HttpStatus.BAD_REQUEST, message), headers, status, request);
    }

    private ProblemDetail problemDetail(HttpStatus status, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
