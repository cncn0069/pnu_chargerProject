package charger.main.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MemberDto {
	@NotBlank
	private String username;
	@NotBlank
    private String nickname;
	private String password;
    private String phoneNumber;
    private String email;
    private String sex;
	private String zipcode;
	private String roadAddr;
	private String detailAddr;
    private boolean enabled;
    private LocalDateTime createAt;
}
