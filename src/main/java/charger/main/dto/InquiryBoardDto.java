package charger.main.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InquiryBoardDto {
	private Long id;
	@NotBlank
    private String title;
	@NotBlank
    private String content;
    private String memberUsername; // Member의 username 정보만 DTO에 담음
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean enabled;
}
