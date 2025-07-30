package charger.main.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import charger.main.dto.RouteSummaryDto;

@Service
public class KakaoDirectionsService {

	@Value("${kakao.api.key}")
    private String restApiKey;
    // 거리와 소요시간을 담을 DTO 정의 (필요에 따라 변경 가능)
    

    // Directions API 호출 및 거리, 시간 리턴 메서드
    public RouteSummaryDto getRouteSummary(String origin, String destination) {
        String apiUrl = "https://apis-navi.kakaomobility.com/v1/directions";

        // URI 생성
        String uri = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("origin", origin)
                .queryParam("destination", destination)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + restApiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                String json = response.getBody();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(json);
                JsonNode routes = root.path("routes");
                if (routes.isArray() && routes.size() > 0) {
                    JsonNode summary = routes.get(0).path("summary");
                    int distance = summary.path("distance").asInt();
                    int duration = summary.path("duration").asInt();
                    return new RouteSummaryDto(distance, duration);
                } else {
                    System.err.println("routes가 비었거나 없음");
                }
            } else {
                System.err.println("API 호출 실패, 상태코드=" + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 실패 시 null 또는 Optional.empty() 반환 등으로 변경 가능
        return null;
    }
}
