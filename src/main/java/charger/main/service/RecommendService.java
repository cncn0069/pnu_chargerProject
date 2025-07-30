package charger.main.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

import charger.main.domain.QStoreInfo;
import charger.main.dto.CoorDinatesDto;
import charger.main.dto.StoreResultsDto;
import charger.main.util.GeoUtil;
import charger.main.util.StoreUtil;
import charger.main.util.GeoUtil.BoundingBox;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class RecommendService {
	@Autowired
	private StoreUtil util;
	
	@PersistenceContext
	private EntityManager em;
	
	public StoreResultsDto getRecommendStore(CoorDinatesDto dto) {
		
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
					.select(qinfo.lat,qinfo.lng)
					.from(qinfo)
					.where(builder)
					.fetch();
		
		//구글
		GeoApiContext context = new GeoApiContext.Builder()
			    .apiKey("YOUR_API_KEY")
			    .build();
			

		ExecutorService executor = Executors.newFixedThreadPool(10);

		List<CompletableFuture<DistanceMatrix>> futures = ids.stream()
		    .map(tuple -> CompletableFuture.supplyAsync(() -> {
		        try {
		            return DistanceMatrixApi.newRequest(context)
		                .origins(new LatLng(dto.getLat(), dto.getLon()))
		                .destinations(new LatLng(tuple.get(0, Double.class), tuple.get(1, Double.class)))
		                .mode(TravelMode.DRIVING)
		                .await();
		        } catch (ApiException | InterruptedException | IOException e) {
		            throw new RuntimeException(e);
		        }
		    }, executor))
		    .collect(Collectors.toList());

		// 모든 요청이 완료될 때까지 대기
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		// 결과 가져오기
		List<DistanceMatrix> results = futures.stream()
		    .map(CompletableFuture::join)
		    .collect(Collectors.toList());

		executor.shutdown();

		
		
		return null;
	}
}
