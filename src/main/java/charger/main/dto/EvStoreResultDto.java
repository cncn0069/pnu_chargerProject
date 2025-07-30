package charger.main.dto;

import java.io.Serializable;

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
public class EvStoreResultDto implements Serializable{
	  private static final long serialVersionUID = 1L;
	//충전소 명
	private String statNm;
	//충전소 ID
	private String statId;
	//충전기 ID
	private String chgerId;
	//	충전기타입
	//	(01:DC차데모,
	//	02: AC완속,
	//	03: DC차데모+AC3상,
	//	04: DC콤보,
	//	05: DC차데모+DC콤보
	//	06: DC차데모+AC3상
	//	+DC콤보,
	//	07: AC3상
	//	08: DC콤보(완속)
	//	09: NACS
	//	10: DC콤보+NACS)
	private String chgerType;
	//충전방식(1:B타입(5핀), 2: C타입(5핀), 3:BC타입(5핀),4: BC타입(7핀),5: DC차 데모, 6:AC 3상, 7: DC콤보,8: DC차데모+DC콤보. 9:DC차데모+AC3상, 10: DC차데모+DC콤보, AC3상)
	private String addr;
	private double lat;
	private double lng;
	//사용 가능 시간
	private String useTime;
	//상세위치
	private String location;
	//충전기 상태 갱신 시각
	private String startUpdatetime;
	//충전기 상태
	//	충전기상태
	//	(1: 통신이상, 2: 충전대기,
	//	3: 충전중, 4: 운영중지,
	//	5: 점검중, 9: 상태미확인)
	private String stat;
	//충전기 상태가 변경된 일시
	private String statUpdDt;
	//마지막 충전 시작일시
	private String lastTsdt;
	//마지막 충전 종료 일시
	private String lastTedt;
	//충전 중 시작일시
	private String nowTsdt;
	//충전 용량
	private String output;
	//충전 방식
	private String method;
	//충전소 구분코드
	private String kind;
	//충전소 구분코드 상세코드
	private String kindDetail;
	//주차료 무료 y/n
	private String parkingFree;
	//충전소 안내
	private String note;
	//이용자 제한 y/n
	private String limitYn;
	//이용 제한 사유
	private String limitDetail;
	//삭제여부
	private String delYn;
	
	private String busiId;
	
	private String busiNm;
	
	private double deliveredKwh;
	
	private double requestKwh;
}
