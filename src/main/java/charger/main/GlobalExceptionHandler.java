package charger.main;

import java.util.ArrayList;
import java.util.List;



import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import charger.main.exception.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler{
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
	    MethodArgumentNotValidException ex,
	    HttpHeaders headers,
	    HttpStatusCode status,   // HttpStatus가 아니라 HttpStatusCode!
	    WebRequest request
	) {
	    List<String> errors = new ArrayList<>();
	    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
	        errors.add(error.getField() + ": " + error.getDefaultMessage());
	    }
	    ErrorResponse errorResponse = new ErrorResponse("Validation Failed", errors);
	    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<?> handleIllegalStateException(IllegalStateException ex){
		
		ErrorResponse error = new ErrorResponse("잘못된 값 형태 : ", List.of(ex.getMessage()));
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}
	
	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException ex){
		
		ErrorResponse error = new ErrorResponse("잘못된 사용자 정보 : ", List.of(ex.getMessage()));
		
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}
}
