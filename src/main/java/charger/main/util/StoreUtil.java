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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import charger.main.domain.ConnectorTypes;
import charger.main.domain.QStoreInfo;
import charger.main.domain.StoreInfo;
import charger.main.dto.CoorDinatesDto;
import charger.main.dto.EvStoreResultDto;
import charger.main.dto.ItemWrapper;
import charger.main.dto.KaKaoResultDto;
import charger.main.dto.MapInfoResultDto;
import charger.main.dto.MapQueryDto;
import charger.main.dto.RouteSummaryDto;
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
	
//	public StoreUtil(WebClient.Builder webClientBuilder,String kakaoApiKey,String ddApiKey,String ddApiKeyDecode) {
//	        this.webClientBuilder = webClientBuilder;
//	        this.KAKAO_API_KEY = kakaoApiKey;
//	        this.DD_API_KEY = ddApiKey;
//	        this.DD_API_KEY_DECODE = ddApiKeyDecode;
//	    }
	
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
		
		GeoUtil geoUtil = new GeoUtil();
		BoundingBox bb = geoUtil.getBoundingBox(dto.getLat(), dto.getLon(), dto.getRadius());
		
		
		JPAQueryFactory queryFactory = new JPAQueryFactory(em);
		
		BooleanBuilder builder = new BooleanBuilder();
		QStoreInfo qinfo = QStoreInfo.storeInfo;
		
		//필터가 없으면 거리만으로
		if(MapDto.getMapQueryDto() == null) {
			builder.and(qinfo.lng.between(bb.getMinLng(), bb.getMaxLng()));
			builder.and(qinfo.lat.between(bb.getMinLat(), bb.getMaxLat()));
			BooleanExpression distanceCondition = Expressions.booleanTemplate(
				    "ST_Distance_Sphere(POINT({0}, {1}), POINT({2}, {3})) <= {4}",
				    qinfo.lng, qinfo.lat, dto.getLon(), dto.getLat(), ((double)dto.getRadius())
				);

			builder.and(distanceCondition);
			return  queryFactory
					.select(qinfo.statId)
					.from(qinfo)
					.where(builder)
					.fetch();
		}
		
		MapQueryDto mqDto = MapDto.getMapQueryDto();
		
		
		//검색어가 있으면
		if(mqDto.getKeyWord() != null && !mqDto.getKeyWord().isBlank()) {
			builder.and(qinfo.statNm.like("%"+ mqDto.getKeyWord()+ "%"));
		}
		
		if(mqDto.getLimitYn() != null && mqDto.getLimitYn()) {
			//접근 제한이 없는 곳을 고른다.
			//false이면 제한이 없는곳
			builder.and(qinfo.limitYn.eq(false));
		}
		if(mqDto.getParkingFree() != null && mqDto.getParkingFree()) {
			//주차요금이 없는 곳을 고른다.
			builder.and(qinfo.parkingFree.eq(true));
		}
		//busId와 같은지
		if(mqDto.getBusiId() != null && mqDto.getBusiId().size() != 0) {
			for(String busiId:mqDto.getBusiId()) {
				builder.or(qinfo.busiId.contains(busiId));
			}
		}
		//타입 조건이 걸렸으면
		if(mqDto.getChgerType() != null && mqDto.getChgerType().size() != 0) {
			for(ConnectorTypes chagerType:mqDto.getChgerType()) {
				builder.or(qinfo.enabledCharger.contains(chagerType));
			}
		}
		
		if(mqDto.getUseMap() != null && mqDto.getUseMap()) {
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
	
	
	@Cacheable(value="setIds",  keyGenerator = "sortedSetKeyGenerator")
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
	@Cacheable(value = "kakaoDisCache", key = "#dto.toString() + '-' + #ids.hashCode()")
	public Mono<List<RouteSummaryDto>> getKakaoDis(CoorDinatesDto dto, List<Tuple> ids){
		 
		 WebClient webClient = WebClient.builder()
			        .baseUrl("https://apis-navi.kakaomobility.com/v1/directions")
			        .defaultHeader("Authorization", "KakaoAK " + KAKAO_API_KEY)
			        .build();
	 			
		 			
			    return Flux.fromIterable(ids)
			        .flatMap(id -> {
			        	String origin= String.valueOf(dto.getLon()) + "," + String.valueOf(dto.getLat());
			            String destination = String.valueOf(id.get(1,Double.class)) + "," + String.valueOf(id.get(0,Double.class));
			            return webClient.get()
			                .uri(uriBuilder -> uriBuilder
			                    .queryParam("origin", origin)
			                    .queryParam("destination", destination)
			                    .build())
			                .retrieve()
			                .bodyToMono(String.class)  // JSON을 문자열로 받음
			                .flatMap(json -> {
			                    try {
			                        ObjectMapper mapper = new ObjectMapper();
			                        JsonNode root = mapper.readTree(json);
			                        JsonNode routes = root.path("routes");
			                        if (routes.isArray() && routes.size() > 0) {
			                            JsonNode summary = routes.get(0).path("summary");
			                            int distance = summary.path("distance").asInt();
			                            int duration = summary.path("duration").asInt();
			                            RouteSummaryDto routeSummaryDto = new RouteSummaryDto(distance, duration);
			                            return Mono.just(routeSummaryDto);
			                        } else {
			                            return Mono.empty();  // routes가 없으면 빈 Mono 반환
			                        }
			                    } catch (Exception e) {
			                        return Mono.error(e);
			                    }
			                });
			        }).collectList();
		
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
				
				double deliveredRadom = random.nextDouble(100); 
				
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
					    .deliveredKwh(deliveredRadom - random.nextDouble(100))
					    .requestKwh(deliveredRadom)
					    .build());
			}
			results.add(temp);
		}
		
		return results;
	}
	

	@Cacheable(value = "statids", keyGenerator = "sortedSetKeyGenerator")
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
			stDto.setUseTime(item.get(0).getUseTime());
			
			int totalChargeNum = 0;
			int chargeNum = 0;
			int totalFastNum = 0;
			int chargeFastNum = 0;
			int totalSlowNum = 0;
			int chargeSlowNum = 0;
			int totalMidNum = 0;
			int chargeMidNum = 0;
			int nacsTotalNum = 0;
			Set<ConnectorTypes> enabledCharger = new HashSet<>();
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
						enabledCharger.add(ConnectorTypes.DC차데모);
						break;
					case "02":
						chargeSlowNum++;
						enabledCharger.add(ConnectorTypes.AC완속);
						break;
					case "03":
						chargeFastNum++;
						enabledCharger.add(ConnectorTypes.DC차데모);
						enabledCharger.add(ConnectorTypes.AC3상);
						break;
					case "04":
						chargeFastNum++;
						enabledCharger.add(ConnectorTypes.DC콤보);
						break;
					case "05":
						chargeFastNum++;
						enabledCharger.add(ConnectorTypes.DC차데모);
						enabledCharger.add(ConnectorTypes.DC콤보);
						break;
					case "06":
						chargeFastNum++;
						enabledCharger.add(ConnectorTypes.DC차데모);
						enabledCharger.add(ConnectorTypes.DC콤보);
						enabledCharger.add(ConnectorTypes.AC3상);
						break;
					case "07":
						enabledCharger.add(ConnectorTypes.AC3상);
						chargeSlowNum++;
						break;
					case "08":
						enabledCharger.add(ConnectorTypes.DC콤보_완속);
						chargeSlowNum++;
						break;
					case "09":
						enabledCharger.add(ConnectorTypes.NACS);
						chargeFastNum++;
						break;
					case "10":
						enabledCharger.add(ConnectorTypes.NACS);
						enabledCharger.add(ConnectorTypes.DC콤보);
						chargeFastNum++;
						nacsTotalNum++;
						break;
					
				}
				}
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
			
//			StoreInfo 정보가 등록 안되어있으면 등록
//			enabledChager 정보가 등록안되어있어서함
			infoRepo.save(StoreInfo.builder()
					.statId(item.get(0).getStatId())
	                .statNm(item.get(0).getStatNm())
	                .addr(item.get(0).getAddr())
	                .lat(item.get(0).getLat())
	                .lng(item.get(0).getLng())
	                .parkingFree(item.get(0).getParkingFree().equals("Y")?true:false)
	                .limitYn(item.get(0).getLimitYn().equals("Y") ?true:false)
	                .enabledCharger(enabledCharger)
	                .busiId(item.get(0).getBusiId())
	                .busiNm(item.get(0).getBusiNm())
//	                충전기 갯수 저장
	                .chargerNm(totalChargeNum)
	                .build());
			
		return stDto;
	}
}
