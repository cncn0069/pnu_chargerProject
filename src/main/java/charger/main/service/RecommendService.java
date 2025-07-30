package charger.main.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

import charger.main.domain.Prediction;
import charger.main.domain.QPrediction;
import charger.main.domain.QStoreInfo;
import charger.main.dto.CoorDinatesDto;
import charger.main.dto.EvStoreResultDto;
import charger.main.dto.EvStroekWhPredictionDto;
import charger.main.dto.RouteSummaryDto;
import charger.main.dto.StoreResultsDto;
import charger.main.persistence.PredictionRepository;
import charger.main.util.GeoUtil;
import charger.main.util.StoreUtil;
import charger.main.util.GeoUtil.BoundingBox;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RecommendService {
	@Autowired
	private StoreUtil util;
	
	@PersistenceContext
	private EntityManager em;
	
	@Value("${google.map.api-key}")
	private String GOOGLE_KEY;
	
	@Value("${kakao.api.key}")
	private String KAKAO_REST_API_KEY;
	
	private PredictionRepository predictionRepository;
	
	@Autowired
	private KakaoDirectionsService kakaoDirectionsService;
	
	public Set<StoreResultsDto> getRecommendStore(CoorDinatesDto dto) {
		
		GeoUtil geoUtil = new GeoUtil();
		
		//주변 저장소 정보 가져옴
		JPAQueryFactory queryFactory = new JPAQueryFactory(em);
		BoundingBox bb = geoUtil.getBoundingBox(dto.getLat(), dto.getLon(), dto.getRadius());
		BooleanBuilder builder = new BooleanBuilder();
		QStoreInfo qinfo = QStoreInfo.storeInfo;
		

		builder.and(qinfo.lng.between(bb.getMinLng(), bb.getMaxLng()));
		builder.and(qinfo.lat.between(bb.getMinLat(), bb.getMaxLat()));
		BooleanExpression distanceCondition = Expressions.booleanTemplate(
			    "ST_Distance_Sphere(POINT({0}, {1}), POINT({2}, {3})) <= {4}",
			    qinfo.lng, qinfo.lat, dto.getLon(), dto.getLat(), ((double)dto.getRadius())
			);
		builder.and(distanceCondition);
		List<Tuple> ids = queryFactory
					.select(qinfo.lat,qinfo.lng,qinfo.statId)
					.from(qinfo)
					.where(builder)
					.limit(20)
					.fetch();
		
		Mono<List<List<EvStoreResultDto>>> stores = util.getKepco(ids.stream().map(n->n.get(2,String.class)).collect(Collectors.toSet()));
		
		
		Mono<List<RouteSummaryDto>> broad = util.getKakaoDis(dto, ids);
		
		List<RouteSummaryDto> kakaoDis = broad.block();
		List<List<EvStoreResultDto>> items = stores.block();
		
		List<StoreResultsDto> result = new ArrayList<>();
		
		
		//거리 + 예상 수요량 -> 순위 도출
		for(int i = 0; i < kakaoDis.size();i++) {
			RouteSummaryDto dm = kakaoDis.get(i);
			//걸리는 시간
			int timeTake = dm.getDuration();
			//충전 대기시간
			//예측 대수로 인한 -> 대기시간
			//도착 했을 시간에 예상
			//8월 1일 기준
			LocalDateTime estimatedArrivalTime = LocalDateTime.of(2025, 8, 1, 10, 0, 0);
			int minute = estimatedArrivalTime.getMinute();
			int mod = minute % 30;

			if (mod != 0) {
			    int minutesToAdd = 30 - mod;
			    estimatedArrivalTime = estimatedArrivalTime.plusMinutes(minutesToAdd).truncatedTo(ChronoUnit.MINUTES);
			} else {
			    // 이미 30분 단위이면 초 이하만 잘라준다.
			    estimatedArrivalTime = estimatedArrivalTime.truncatedTo(ChronoUnit.MINUTES);
			}
			StoreResultsDto resultDto = util.getStoreResultsDto(items.get(i));
			QPrediction qpred = QPrediction.prediction;
			builder = new BooleanBuilder();
			
			builder.and(qpred.storeTimeStamp.eq(estimatedArrivalTime));
			
			List<Prediction> qpredList = queryFactory
					.select(qpred)
					.from(qpred)
					.where(builder)
					.fetch();
			
			
			for(String key : resultDto.getChargerInfo().keySet()) {
				EvStoreResultDto evDto = resultDto.getChargerInfo().get(key);
				queryFactory = new JPAQueryFactory(em);
				builder = new BooleanBuilder();
				
				Optional<Double> demandOpt = qpredList.stream()
					    .filter(n -> n.getStationLocation().equals(evDto.getStatId()) && n.getEvseName().equals(evDto.getChgerId()))
					    .map(Prediction::getYPred)
					    .findFirst();

				Double demand = demandOpt.orElse(0.1);  // 없으면 기본값 1.0 사용
				
				//충전기의 예측량이 나오면
				//타입2면 완속
				if(evDto.getChgerType().equals("2")) {
					 resultDto.setMinute((int) ((demand / 10.0) * 60));
				}else {
					 resultDto.setMinute((int) ((demand / 100.0) * 60));
				}
				
				resultDto.setMinute(resultDto.getMinute() + (int)timeTake/60);
				
				int minute2 = resultDto.getMinute();

				// 분(minute)을 10으로 나누어 구간으로 변환
				int range = (minute2 >= 60) ? 6 : (minute / 10);  // 60 이상은 6으로 구분

				switch (range) {
				    case 0:
				    case 1:
				        // 0~19분: 0~9분은 0, 10~19분은 1로 나눠야 하므로 세부 분기 처리
				        if (minute < 10) {
				            resultDto.setPredTag("0");
				        } else {
				            resultDto.setPredTag("1");
				        }
				        break;
				    case 2:
				    case 3:
				    case 4:
				    case 5:
				        // 20~59분 -> "2"
				        resultDto.setPredTag("2");
				        break;
				    case 6:
				    default:
				        // 60 이상 -> "3"
				        resultDto.setPredTag("3");
				        break;
				}
				result.add(resultDto);
			}
		}
		
		int bestChoice = Integer.MAX_VALUE;
		int bbb = 0;
		for(int i =0 ; i < result.size();i++) {
			if(result.get(i).getMinute() < bestChoice)
			{
				bbb = i;
				bestChoice = result.get(i).getMinute();
			}
		}
		
		result.get(bbb).setBestChoice(true);
		
		return result.stream().collect(Collectors.toSet());
	}
}


