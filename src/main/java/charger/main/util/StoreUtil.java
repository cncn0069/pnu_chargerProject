package charger.main.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import charger.main.domain.QStoreInfo;
import charger.main.domain.StoreInfo;
import charger.main.dto.CoorDinatesDto;
import charger.main.dto.EvStoreResultDto;
import charger.main.dto.ItemWrapper;
import charger.main.dto.KaKaoResultDto;
import charger.main.dto.MapInfoResultDto;
import charger.main.dto.MapQueryDto;
import charger.main.dto.StoreResultsDto;
import charger.main.persistence.StoreInfoRepository;
import charger.main.util.GeoUtil.BoundingBox;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class StoreUtil {
	
	@Autowired
	private WebClient.Builder webClientBuilder;
	
	@Autowired
	private StoreInfoRepository infoRepo;
	
	@Value("${kakao.api.key}")
	private String KAKAO_API_KEY;
	
	@Value("${dd.api.key}")
	private String DD_API_KEY;
	
	@Value("${dd.api.key.decode}")
	private String DD_API_KEY_DECODE;
	
	@PersistenceContext
	private EntityManager em;
	
	public StoreUtil(WebClient.Builder webClientBuilder,String kakaoApiKey,String ddApiKey,String ddApiKeyDecode) {
	        this.webClientBuilder = webClientBuilder;
	        this.KAKAO_API_KEY = kakaoApiKey;
	        this.DD_API_KEY = ddApiKey;
	        this.DD_API_KEY_DECODE = ddApiKeyDecode;
	    }
	
	public Set<String> getKakao(CoorDinatesDto dto) {
		WebClient webClient = webClientBuilder.baseUrl("https://dapi.kakao.com").build();
		System.out.println(KAKAO_API_KEY);
		String resp = webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("v2/local/geo/coord2regioncode.JSON")
//						.queryParam("category_group_code", "CE7") // 전기차 충전소 카테고리
						.queryParam("x", dto.getLon())
						.queryParam("y", dto.getLat())
						.queryParam("radius",1000)
						.build())
				.header("Authorization", "KakaoAK " + KAKAO_API_KEY)
				.retrieve()
				.bodyToMono(String.class)
				.block();
		
		JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
		JsonArray infos = (JsonArray) obj.get("documents");
			
		Gson gson = new Gson();
		//필요한정보만 추출
		JsonObject info = infos.get(0).getAsJsonObject();
		KaKaoResultDto temp = gson.fromJson(info, KaKaoResultDto.class);
		Set<String> result = new HashSet<>();
		result.add(temp.getCode());
		return result;
	}
	
	//Map<String,KaKaoResultDto>
		public Set<String> getSetIds(List<String> dtos){
			Set<String> statids = new HashSet<>();
			Gson gson = new Gson();
			String serviceKey = DD_API_KEY;
			
			try {

				for(String code:dtos) {
						String baseUrl = "http://apis.data.go.kr/B552584/EvCharger/getChargerStatus";
				        
				        if(code.equals("")) {
				        	continue;
				        }
				       
				        String query = "page=1&perPage=9999&" 
				        + "&serviceKey="
				        + serviceKey
				        + "&zcode="
				        + code.substring(0,2)
//				        + "&zscode="
//				        + code.substring(0,5)
				        + "&dataType="
				        + "JSON";
//				        + "&statId="
//				        + "CV003367";			       
				        
				        URL url = new URL(baseUrl + "?" + query);
				        log.info("지번코드" + code.substring(0,5));
				        log.info("요청 주소 : " + url.toString());
				        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				        conn.setRequestMethod("GET");
				        int responseCode = conn.getResponseCode();
				        BufferedReader br = new BufferedReader(
				            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
				        );

				        StringBuilder sb = new StringBuilder();
				        String line;
				        while ((line = br.readLine()) != null) {
				            sb.append(line);
				        }
				        br.close();
			        JsonObject obj = JsonParser.parseString(sb.toString()).getAsJsonObject();
			        JsonArray infos = obj.getAsJsonObject("items").getAsJsonArray("item");
			        
			        //이제 여기서 statId를 다 뽑아야함
			        //똑같은 statId를 가진 데이터도 있었음 --> set으로 해결
			        for(JsonElement element: infos) {
			        	EvStoreResultDto dto = gson.fromJson(element, EvStoreResultDto.class);
			        	statids.add(dto.getStatId());
			        }
				}
				
			} catch (Exception e) {
				// TODO: handle exception
				log.info("statID 가져오기 실패");
			}
			
			return statids;
		}
	
	public List<String> getMapInfo(MapInfoResultDto MapDto){
		CoorDinatesDto dto = MapDto.getCoorDinatesDto();
		
		List<String> codes = new ArrayList<>();
		GeoUtil geoUtil = new GeoUtil();
		BoundingBox bb = geoUtil.getBoundingBox(dto.getLat(), dto.getLon(), dto.getRadius());
		
		
		JPAQueryFactory queryFactory = new JPAQueryFactory(em);
		
		BooleanBuilder builder = new BooleanBuilder();
		QStoreInfo qinfo = QStoreInfo.storeInfo;
		MapQueryDto mqDto = MapDto.getMapQueryDto();
		
		//검색어가 있으면
		if(mqDto.getKeyWord() != null && !mqDto.getKeyWord().isBlank()) {
			builder.and(qinfo.statNm.like("%"+ mqDto.getKeyWord()+ "%"));
		}
		
		if(mqDto.getLimitYn()) {
			//접근 제한이 없는 곳을 고른다.
			//false이면 제한이 없는곳
			builder.and(qinfo.limitYn.eq(false));
		}
		if(mqDto.getParkingFree()) {
			//주차요금이 없는 곳을 고른다.
			builder.and(qinfo.parkingFree.eq(true));
		}
		//busId와 같은지
		if(mqDto.getBusiId().size() != 0) {
			for(String busiId:mqDto.getBusiId()) {
				builder.and(qinfo.busiId.in(busiId));
			}
		}

		for(String chagerType:mqDto.getChgerType()) {
			builder.or(qinfo.busiNm.like(chagerType));
		}
		if(mqDto.getUseMap()) {
			builder.and(qinfo.lng.between(bb.getMinLng(), bb.getMaxLng()));
			builder.and(qinfo.lat.between(bb.getMinLat(), bb.getMaxLat()));
			BooleanExpression distanceCondition = Expressions.booleanTemplate(
				    "ST_Distance_Sphere(POINT({0}, {1}), POINT({2}, {3})) <= {4}",
				    qinfo.lng, qinfo.lat, dto.getLon(), dto.getLat(), ((double)dto.getRadius())
				);

			builder.and(distanceCondition);
		}

		//output, canUse, chgerType 는 MapService에서 처리 쪽에서 따로 처리
		
		List<String> results = queryFactory
				.select(qinfo.statId)
				.from(qinfo)
				.where(builder)
				.fetch();
		
		
		
		
//		for(String result: results) {
//			log.info("요구 코드 : " + result);
//		}
		
		return results;
	}
	
	
	
	public Mono<List<List<EvStoreResultDto>>> getKepco(Set<String> setId){
		 
		String serviceKey = DD_API_KEY;
		String BASE_URL = "http://apis.data.go.kr/B552584/EvCharger/getChargerInfo";
		
		// uribuild 설정을 도와주는 DefaultUriBUilderFactory 호출
		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(BASE_URL);

		// 인코딩 모드 설정
		factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
				
		WebClient webClient = WebClient.builder()
					.uriBuilderFactory(factory)
		 			.baseUrl(BASE_URL)
					.build();

		return Flux.fromIterable(setId)
		    .flatMap(id -> webClient.get()
		            .uri(uriBuilder -> uriBuilder
		            	    .queryParam("page", "1")
		            	    .queryParam("perPage", "9999")
		            	    .queryParam("serviceKey", serviceKey)
		            	    .queryParam("dataType", "JSON")
		            	    .queryParam("statId", id)
		                    .build())
		                .retrieve()
		        .bodyToMono(ItemWrapper.class)
		        .map(wrapper -> wrapper.getItems().getItem())
		    )
		    .collectList();
		
	}
	
	
	public List<List<EvStoreResultDto>> getDummy(Set<String> setIds){
		
		List<List<EvStoreResultDto>> results = new ArrayList<>();
		
		
		for(String setId:setIds) {
			StoreInfo storeinfo = infoRepo.findById(setId).get();
			Random random = new Random();
			//0~99
			int randomEvSlot = random.nextInt(10)+1;
			
			List<EvStoreResultDto> temp = new ArrayList<>();
			
			for(int i = 0; i < randomEvSlot;i++) {
				temp.add(EvStoreResultDto.builder()
						.statNm(storeinfo.getStatNm())
					    .statId(storeinfo.getStatId())
					    .chgerId("0" + String.valueOf(i))
					    .chgerType("0"+String.valueOf(random.nextInt(5)+1))
					    .addr(storeinfo.getAddr())
					    .lat(storeinfo.getLat())
					    .lng(storeinfo.getLng())
					    .useTime(String.valueOf(i) + "시간")
					    .location("지하"+ String.valueOf(5)+1  +"층")
					    .startUpdatetime("20250708150026")
					    .stat(String.valueOf(random.nextInt(5)+1))
					    .statUpdDt("2025-07-08 09:55:00")
					    .lastTsdt("20250708150026")
					    .lastTedt("20250708150013")
					    .nowTsdt(Math.random() < 0.5 ? "20250708150026" : null)
					    .output(String.valueOf(random.nextInt(200)+1))
					    .method(Math.random() < 0.5 ? "단독" : "동시")
					    .kind("G0")
					    .kindDetail("G004")
					    .parkingFree(storeinfo.getParkingFree() ? "Y" : "N")
					    .note("빠른 충전 가능")
					    .limitYn(storeinfo.getLimitYn() ? "Y" : "N")
					    .limitDetail(null)
					    .delYn("N")
					    .busiId(storeinfo.getBusiId())
					    .busiNm(storeinfo.getBusiNm())
					    .build());
			}
			results.add(temp);
		}
		
		return results;
	}
	
//	public 
//	.path("B552584/EvCharger/getChargerInfo")
//    .queryParam("page", "1")
//    .queryParam("perPage", "9999")
//    .queryParam("serviceKey", serviceKey)
//    .queryParam("dataType", "JSON")
//    .queryParam("statId", id)
//    .build();

	public Map<String,List<EvStoreResultDto>> setMapInfo(Set<String> statids) {
		Gson gson = new Gson();
		Map<String,List<EvStoreResultDto>> result = new HashMap<>();
		for(String statid:statids) {
			//등록된 ID가 없으면 등록 아니면 통과
			if(infoRepo.findById(statid).isEmpty()) {
				
				String baseUrl = "http://apis.data.go.kr/B552584/EvCharger/getChargerInfo";
		        String serviceKey = DD_API_KEY;
	
				String query = "page=1&perPage=9999" 
		        + "&serviceKey="
		        + serviceKey
		        + "&dataType="
		        + "JSON"
		        + "&statId="
		        + statid;			       
		        try {
		        	URL url = new URL(baseUrl + "?" + query);
			        log.info("매장정보" + statid);
			        log.info("요청 주소 : " + url.toString());
			        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			        conn.setRequestMethod("GET");
			        int responseCode = conn.getResponseCode();
			        BufferedReader br = new BufferedReader(
			            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
			        );
		
			        StringBuilder sb = new StringBuilder();
			        String line;
			        while ((line = br.readLine()) != null) {
			            sb.append(line);
			        }
			        br.close();
			        JsonObject obj = JsonParser.parseString(sb.toString()).getAsJsonObject();
			        JsonArray infos = obj.getAsJsonObject("items").getAsJsonArray("item");
			        try {	
						//item들 안에서 같은 곳인 곳을 찾아야함
						for(JsonElement element: infos) {
							EvStoreResultDto dto = gson.fromJson(element, EvStoreResultDto.class);
							result.computeIfAbsent(
								    dto.getStatId(), 
								    k -> new ArrayList<>()
								).add(dto);
						}
					}catch (Exception e) {
						// TODO: handle exception
						log.error("주소가 없습니다.");
						e.printStackTrace();
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		return result;
	}
	
	
	public StoreResultsDto getStoreResultsDto(List<EvStoreResultDto> item) {
		
			
		
		
			StoreResultsDto stDto = new StoreResultsDto();
			stDto.setStatNm(item.get(0).getStatNm());
			stDto.setStatId(item.get(0).getStatId());
			stDto.setAddr(item.get(0).getAddr());
			stDto.setLat(item.get(0).getLat());
			stDto.setLng(item.get(0).getLng());
			stDto.setParkingFree(item.get(0).getParkingFree().equals("Y")?true:false);
			stDto.setLimitYn(item.get(0).getLimitYn().equals("Y") ?true:false);
			stDto.setBusiId(item.get(0).getBusiId());
			stDto.setBusiNm(item.get(0).getBusiNm());
			int totalChargeNum = 0;
			int chargeNum = 0;
			int totalFastNum = 0;
			int chargeFastNum = 0;
			int totalSlowNum = 0;
			int chargeSlowNum = 0;
			int totalMidNum = 0;
			int chargeMidNum = 0;
			int nacsTotalNum = 0;
			Set<String> enabledCharger = new HashSet<>();
			Map<String, EvStoreResultDto> chargerInfo = new HashMap<>();
			for(EvStoreResultDto evDto: item) {
				if(evDto == null)
					continue;
				
				totalChargeNum++;
				chargerInfo.put(evDto.getChgerId(), evDto);
				if(evDto.getStat().equals("2")){
					chargeNum++;
					switch (evDto.getChgerType()) {
					case "01":
						chargeFastNum++;
						break;
					case "02":
						chargeSlowNum++;
						break;
					case "03":
						chargeFastNum++;
						break;
					case "04":
						chargeFastNum++;
						break;
					case "05":
						chargeFastNum++;
						break;
					case "06":
						chargeFastNum++;
						break;
					case "07":
						chargeSlowNum++;
						break;
					case "08":
						chargeSlowNum++;
						break;
					case "09":
						chargeFastNum++;
						break;
					case "10":
						chargeFastNum++;
						nacsTotalNum++;
						break;
					
				}
				}
				enabledCharger.add(evDto.getChgerType());
				
	
				int output = Integer.parseInt(evDto.getOutput().equals("")? "0": evDto.getOutput());
				
				if(output <= 7) {
					totalSlowNum++;
				}else if(output <= 30) {
					totalMidNum++;
				}else if(output <= 100) {
					totalFastNum++;
				}else if(output > 100) {
					totalFastNum++;
				}
			}
			stDto.setTotalNacsNum(nacsTotalNum);
			stDto.setChargerInfo(chargerInfo);
			stDto.setChargeFastNum(chargeFastNum);
			stDto.setChargeMidNum(chargeMidNum);
			stDto.setChargeSlowNum(chargeSlowNum);
			stDto.setTotalFastNum(totalFastNum);
			stDto.setTotalMidNum(totalMidNum);
			stDto.setTotalSlowNum(totalSlowNum);
			stDto.setTotalChargeNum(totalChargeNum);
			stDto.setChargeNum(chargeNum);
			stDto.setEnabledCharger(enabledCharger);
		return stDto;
	}
}
//Map<String,List<EvStoreResultDto>> result = new HashMap<>();
//Gson gson = new Gson();
//
////충전소들		https://api.odcloud.kr/api/EvInfoServiceV2/v1/getEvSearchList?page=1&perPage=10&cond%5Baddr%3A%3ALIKE%5D=%EC%A0%84%EB%9D%BC%EB%82%A8%EB%8F%84%20%EB%82%98%EC%A3%BC%EC%8B%9C%20%EC%A0%84%EB%A0%A5%EB%A1%9C%2055&serviceKey=SqqAk1q%2BvWV6MAwbCqRTzoFfuIUi8A8sMtPEeDWsU%2Fs8OGT8DcGP4VGzGkYHmJoAzS60SjWRMeMRGFwq12ScGQ%3D%3D
//		try {
//				//statid로 재 검색
//				for(String statId:setId) {
//					String baseUrl = "http://apis.data.go.kr/B552584/EvCharger/getChargerInfo";
//			        
//		
//					String query = "page=1&perPage=9999&" 
//			        + "&serviceKey="
//			        + serviceKey
//			        + "&dataType="
//			        + "JSON"
//			        + "&statId="
//			        + statId;			       
//			        
//			        URL url = new URL(baseUrl + "?" + query);
//			        log.info("매장정보" + statId);
//			        log.info("요청 주소 : " + url.toString());
//			        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//			        conn.setRequestMethod("GET");
//			        int responseCode = conn.getResponseCode();
//			        BufferedReader br = new BufferedReader(
//			            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
//			        );
//		
//			        StringBuilder sb = new StringBuilder();
//			        String line;
//			        while ((line = br.readLine()) != null) {
//			            sb.append(line);
//			        }
//			        br.close();
//		        JsonObject obj = JsonParser.parseString(sb.toString()).getAsJsonObject();
//		        JsonArray infos = obj.getAsJsonObject("items").getAsJsonArray("item");
//			        try {	
//						//item들 안에서 같은 곳인 곳을 찾아야함
//						for(JsonElement element: infos) {
//							EvStoreResultDto dto = gson.fromJson(element, EvStoreResultDto.class);
//							result.computeIfAbsent(
//								    dto.getStatId(), 
//								    k -> new ArrayList<>()
//								).add(dto);
//						}
//					}catch (Exception e) {
//						// TODO: handle exception
//						log.error("주소가 없습니다.");
//						e.printStackTrace();
//					}
//				}
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//			log.info("Info 가져오기 실패");
//		}
//		return result;
