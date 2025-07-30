package charger.main.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "store_time_data")
public class Statistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    
    
    @Column(name = "last_charge_end_time_ts")
    private Long lastChargeEndTimeTs;

    @Column(name = "connection_start_time_ts")
    private Long connectionStartTimeTs;

    @Column(name = "charging_start_time_ts")
    private Long chargingStartTimeTs;

    @Column(name = "charging_start_time_missing")
    private Boolean chargingStartTimeMissing;

    @Column(name = "charging_end_time_ts")
    private Double chargingEndTimeTs;

    @Column(name = "charging_end_time_missing")
    private Boolean chargingEndTimeMissing;

    @Column(name = "connection_end_time_ts")
    private Long connectionEndTimeTs;

    @Column(name = "expected_departure_time_ts")
    private Integer expectedDepartureTimeTs;

    @Column(name = "expected_departure_time_missing")
    private Integer expectedDepartureTimeMissing;

    @Column(name = "idle_time_ts")
    private Integer idleTimeTs;

    @Column(name = "expected_usage_duration_ts")
    private Integer expectedUsageDurationTs;

    @Column(name = "expected_usage_duration_missing")
    private Integer expectedUsageDurationMissing;

    @Column(name = "expected_time_diff_ts")
    private Double expectedTimeDiffTs;

    @Column(name = "expected_time_diff_missing")
    private Integer expectedTimeDiffMissing;

    @Column(name = "actual_usage_duration_ts")
    private Integer actualUsageDurationTs;

    @Column(name = "actual_charging_duration_ts")
    private Integer actualChargingDurationTs;

    @Column(name = "actual_charging_duration_missing")
    private Integer actualChargingDurationMissing;

    @Column(name = "start_delay_duration_ts")
    private Double startDelayDurationTs;

    @Column(name = "start_delay_duration_missing")
    private Integer startDelayDurationMissing;

    @Column(name = "post_charge_departure_delay_ts")
    private Double postChargeDepartureDelayTs;

    @Column(name = "post_charge_departure_delay_missing")
    private Integer postChargeDepartureDelayMissing;

    @Column(name = "usage_departure_time_diff_ts")
    private Integer usageDepartureTimeDiffTs;

    @Column(name = "usage_departure_time_diff_missing")
    private Integer usageDepartureTimeDiffMissing;

    @Column(name = "duration_per_kwh_ts")
    private String durationPerKwhTs;

    @Column(name = "duration_per_kwh_missing")
    private Boolean durationPerKwhMissing;

    @Column(name = "delivered_kwh")
    private Double deliveredKwh;

    @Column(name = "requested_kwh")
    private Double requestedKwh;

    @Column(name = "kwh_request_diff")
    private Double kwhRequestDiff;

    @Column(name = "kwh_per_usage_time")
    private Double kwhPerUsageTime;

    @Column(name = "kwh_per_usage_time_missing")
    private Boolean kwhPerUsageTimeMissing;

    @Column(name = "station_location")
    private String stationLocation;

    @Column(name = "evse_name")
    private String evseName;

    @Column(name = "evse_type")
    private String evseType;

    @Column(name = "supports_discharge")
    private Boolean supportsDischarge;

    @Column(name = "scheduled_charge")
    private Boolean scheduledCharge;

    @Column(name = "weekday")
    private Integer weekday;

    @Column(name = "usage_departure_range")
    private Integer usageDepartureRange;

    @Column(name = "post_charge_departure_range")
    private Integer postChargeDepartureRange;

    @Column(name = "cluster")
    private Integer cluster;

}
