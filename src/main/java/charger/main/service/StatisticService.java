package charger.main.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import charger.main.domain.QMember;
import charger.main.domain.QPrediction;
import charger.main.domain.QStatistics;
import charger.main.domain.QStoreInfo;
import charger.main.domain.QTimeSlot;
import charger.main.domain.QUserCarInfo;
import charger.main.dto.EvStoreResultDto;
import charger.main.dto.IdleStaticDto;
import charger.main.dto.StaticsticDto;
import charger.main.dto.StoreResultsDto;
import charger.main.persistence.UserCarInfoRepository;
import charger.main.util.StoreUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class StatisticService {
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private StoreUtil util;
	
	@Autowired
	private UserCarInfoRepository carInfoRepository;
	
	public List<StaticsticDto> getStatisticWeekdayByStatId(String statId) {
		// Native Query 예제 (EntityManager 사용)
		String sql = "SELECT station_location, " +
		             "DAYNAME(FROM_UNIXTIME(charging_start_time_ts)) AS 요일, " +
		             "AVG(requested_kwh) AS 평균_requested_kwh " +
		             "FROM store_time_data " +
		             "WHERE station_location LIKE :statId " +
		             "AND DAYOFWEEK(FROM_UNIXTIME(charging_start_time_ts)) BETWEEN 1 AND 7 " +
		             "GROUP BY station_location, 요일";

		Query nativeQuery = em.createNativeQuery(sql);

		// statId 파라미터 세팅 (예: "CSCS2015%")
		nativeQuery.setParameter("statId", statId);

		List<Object[]> results = nativeQuery.getResultList();
		
		List<StaticsticDto> dtos = new ArrayList<>();
		
		for (Object[] row : results) {
		    String stationLocation = (String) row[0];
		    String dayName = (String) row[1];
		    Double avgRequestedKwh = row[2] != null ? ((Number) row[2]).doubleValue() : null;
		    // 처리 로직
		    dtos.add(new StaticsticDto(stationLocation, dayName, avgRequestedKwh));
		}


		return dtos;
	}
	
	public IdleStaticDto getIdleInfo(String local) {
		
		JPAQueryFactory queryFactory = new JPAQueryFactory(em);
		BooleanBuilder builder = new BooleanBuilder();
		QStoreInfo qinfo = QStoreInfo.storeInfo;
		
		builder.and(qinfo.addr.like("%" + local + "%"));
		
		
		List<String> codes = queryFactory
								.select(qinfo.statId)
								.from(qinfo)
								.where(builder)
								.fetch();
		
//		Mono<List<List<EvStoreResultDto>>> kepcoResults = util.getKepco(codes.stream().collect(Collectors.toSet()));
//		List<List<EvStoreResultDto>> items = kepcoResults.block();
//		더미
//		사용시 StoreInfo 로그 못쓰게 해야함 
		List<List<EvStoreResultDto>> items = util.getDummy(codes.stream().collect(Collectors.toSet()));
		IdleStaticDto result = new IdleStaticDto(0,0,0,null);
		
		int[] stat = new int[10];
		
		for(List<EvStoreResultDto> item:items) {
			StoreResultsDto resultDto = util.getStoreResultsDto(item);
			
			
			for(String key: resultDto.getChargerInfo().keySet()) {
				
				int index = Integer.parseInt(resultDto.getChargerInfo().get(key).getStat());
				stat[index]++;
			}
			
			result.setTotalCharger(result.getTotalCharger() + resultDto.getTotalChargeNum());
			result.setTotalUseableCharger(result.getTotalUseableCharger() + resultDto.getTotalChargeNum() - resultDto.getChargeNum());
			result.setTotalDisableCharger(result.getTotalDisableCharger() + resultDto.getChargeNum());
		}
		result.setTotalUseableCharger(result.getTotalUseableCharger() - stat[3] - stat[4] - stat[5]);
		result.setTotalDisableCharger(result.getTotalDisableCharger() + stat[3] + stat[4] + stat[5]); 
		result.setStat(stat);
		
		return result;
	}
	
	public Long getCarTotal() {
		
		JPAQueryFactory queryFactory = new JPAQueryFactory(em);
		BooleanBuilder builder= new BooleanBuilder();
		QUserCarInfo qcar = QUserCarInfo.userCarInfo;
		builder.and(qcar.enabled.eq(true));
		return queryFactory
				.select(qcar.count())
				.from(qcar)
				.where(builder)
				.fetchOne();
	}
	
	public Long getUserTotal() {
			
			JPAQueryFactory queryFactory = new JPAQueryFactory(em);
			BooleanBuilder builder= new BooleanBuilder();
			QMember qmember = QMember.member;
			builder.and(qmember.enabled.eq(true));
			return queryFactory
					.select(qmember.count())
					.from(qmember)
					.where(builder)
					.fetchOne();
		}
	public Long getUserDisableTotal() {
		
		JPAQueryFactory queryFactory = new JPAQueryFactory(em);
		BooleanBuilder builder= new BooleanBuilder();
		QMember qmember = QMember.member;
		builder.and(qmember.enabled.eq(false));
		return queryFactory
				.select(qmember.count())
				.from(qmember)
				.where(builder)
				.fetchOne();
	}
	public Long getReserveTotal() {
		
		JPAQueryFactory queryFactory = new JPAQueryFactory(em);
		BooleanBuilder builder= new BooleanBuilder();
		QTimeSlot qtime = QTimeSlot.timeSlot;
		builder.and(qtime.enabled.eq(true));
		builder.and(qtime.date.after(LocalDate.now()));
		return queryFactory
				.select(qtime.count())
				.from(qtime)
				.where(builder)
			.fetchOne();
	}
}