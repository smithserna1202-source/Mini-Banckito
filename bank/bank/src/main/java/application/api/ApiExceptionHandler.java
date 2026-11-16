package application.api;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
 public Map<String,String> badRequest(IllegalArgumentException e){return Map.of("error",e.getMessage());}
 @ExceptionHandler(IllegalStateException.class) @ResponseStatus(HttpStatus.CONFLICT)
 public Map<String,String> conflict(IllegalStateException e){return Map.of("error",e.getMessage());}
 @ExceptionHandler(SecurityException.class) @ResponseStatus(HttpStatus.FORBIDDEN)
 public Map<String,String> forbidden(SecurityException e){return Map.of("error",e.getMessage());}
 @ExceptionHandler(MethodArgumentNotValidException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
 public Map<String,String> invalid(MethodArgumentNotValidException e){return Map.of("error","Request validation failed");}
}