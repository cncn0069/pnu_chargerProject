package charger.main.dto;

import com.google.gson.annotations.SerializedName;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@ToString
@Builder
public class KaKaoResultDto {
	@SerializedName("address_name") 
	private String addressName;
	private String code;
}
