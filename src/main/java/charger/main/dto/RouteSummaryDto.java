package charger.main.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouteSummaryDto implements Serializable{
    private int distance; // 미터 단위
    private int duration; // 초 단위

    public int getDistance() {
        return distance;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "RouteSummary{" +
                "distance=" + distance +
                ", duration=" + duration +
                '}';
    }
}