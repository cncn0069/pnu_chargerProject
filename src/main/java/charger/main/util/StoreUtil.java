package charger.main.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import charger.main.domain.ConnectorTypes;
import charger.main.domain.StoreInfo;
import charger.main.dto.CoorDinatesDto;
import charger.main.dto.EvStoreResultDto;
import charger.main.dto.ItemWrapper;
import charger.main.dto.KaKaoResultDto;
import charger.main.persistence.StoreInfoRepository;
import charger.main.util.GeoUtil.BoundingBox;
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
	
	
	public List<String> getMapInfo(CoorDinatesDto dto){
		
		List<String> codes = new ArrayList<>();
		GeoUtil geoUtil = new GeoUtil();
		BoundingBox bb = geoUtil.getBoundingBox(dto.getLat(), dto.getLon(), dto.getRadius());
		codes = infoRepo.getStatIdByLatLng(dto.getLat(), dto.getLon(), dto.getRadius(),bb.getMinLng(),bb.getMaxLng(),bb.getMinLat(),bb.getMaxLat());	
		return codes;
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
//			        + "&zscode="
//			        + code.substring(0,5)
			        + "&dataType="
			        + "JSON";
//			        + "&statId="
//			        + "CV003367";			       
			        
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
