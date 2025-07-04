package charger.main.exception;

import java.util.List;

public class ErrorResponse {
    private String message;      // 전체 에러에 대한 요약 메시지
    private List<String> errors; // 각 필드별 상세 에러 메시지

    public ErrorResponse(String message, List<String> errors) {
        this.message = message;
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getErrors() {
        return errors;
    }
}
