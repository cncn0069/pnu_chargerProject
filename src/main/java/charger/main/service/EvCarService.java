package charger.main.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import charger.main.persistence.EVCarModelRepository;
import charger.main.persistence.EvCarsRepository;

@Service
public class EvCarService {
	
	@Autowired
	private	EVCarModelRepository carModelRepo;
	
	@Autowired
	private EvCarsRepository carsRepo;
	
	public List<String> getEvCarBrandName(){
		return carModelRepo.findAll()
				.stream().map(n-> carsRepo.getByCarId(n.getCarId()).getBrand())
				.collect(Collectors.toList());
	}
	
	public List<String> getEvCarModelName(String brand){
		return carsRepo.findByBrand(brand).stream()
				.map(n -> n.getModelID().getEvCarModelName())
				.collect(Collectors.toList());
	}
}
