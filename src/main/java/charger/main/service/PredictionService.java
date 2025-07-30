package charger.main.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import charger.main.domain.QPrediction;
import charger.main.domain.QStoreInfo;
import charger.main.domain.StoreInfo;
import charger.main.dto.PredictionByLocalDto;
import charger.main.dto.StoreResultsDto;
import charger.main.persistence.PredictionRepository;
import charger.main.persistence.StoreInfoRepository;
import charger.main.util.TimeUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PredictionService {
	
	@Autowired
	private StoreInfoRepository infoRepo;
	
	private TimeUtil tUtil = new TimeUtil();
	
	@Autowired
	private PredictionRepository predictionRepository;
	
	@PersistenceContext
	private EntityManager em;
	
	public List<StoreResultsDto> getPredctionEVStoreByLocal(PredictionByLocalDto dto){
			
			List<StoreResultsDto> results = new ArrayList<>();
			
			JPAQueryFactory queryFactory = new JPAQueryFactory(em);
			BooleanBuilder builder = new BooleanBuilder();
			QStoreInfo qinfo = QStoreInfo.storeInfo;
			
			builder.and(qinfo.addr.like("%" + dto.getLocal() + "%"));
			
			//가게들 정보 가져와서
			List<StoreInfo> ids = queryFactory
					.select(qinfo)
					.from(qinfo)
					.where(builder)
					.fetch();
					
			//예측치 가져오기
			for(StoreInfo item:ids) {
				StoreResultsDto stDto = new StoreResultsDto();
				stDto.setStatNm(item.getStatNm());
				stDto.setStatId(item.getStatId());
				stDto.setAddr(item.getAddr());
				stDto.setLat(item.getLat());
				stDto.setLng(item.getLng());
				stDto.setParkingFree(item.getParkingFree());
				stDto.setLimitYn(item.getLimitYn());
				stDto.setBusiId(item.getBusiId());
				stDto.setBusiNm(item.getBusiNm());

				if(item.getChargerNm() == null)
					continue;
				
				
				stDto.setChargeNum(item.getChargerNm());
				
				
				
				//예측값 가져오기
				//해당하는 가게의 해당 시간의 모든 예상 충전량 더해서
				queryFactory = new JPAQueryFactory(em);
				builder = new BooleanBuilder();
				QPrediction qpre = QPrediction.prediction;
				
				builder.and(qpre.stationLocation.like(item.getStatId()));
				
				
				//시간 변경해야함 z-> local korea
				builder.and(qpre.storeTimeStamp.eq(tUtil.changeToAsiaTime(dto.getTime())));
				
				
				Double temp = queryFactory
						.select(qpre.yPred.sumDouble())
						.from(qpre)
						.where(builder)
						.fetchOne();
				
				if(temp == null)
					continue;
				
				stDto.setChargingDemand(temp);
				
				results.add(stDto);
			}
			
			return results;
		}
}
