package charger.main.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class GeoUtil {

	public BoundingBox getBoundingBox(double lat, double lon, int radius) {
		double earthRadius = 6378137;
		double dLat = Math.toDegrees(radius / earthRadius);
		double dLon = Math.toDegrees(radius/ (earthRadius * Math.cos(Math.toRadians(lat))));
		return new BoundingBox(
				lat - dLat, lat + dLat,
				lon - dLon, lon + dLon);
	}
	
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public class BoundingBox{
		private double minLat,maxLat,minLng,maxLng;
	}
	
}
