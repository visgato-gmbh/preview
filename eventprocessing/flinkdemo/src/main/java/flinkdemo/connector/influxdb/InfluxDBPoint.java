package flinkdemo.connector.influxdb;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfluxDBPoint implements Serializable {

	private static final long serialVersionUID = -7023015810306677557L;

	@NonNull private String measurement;
	@NonNull private Long timestamp;
    @Singular("tags") private Map<String, String> tags;
    @Singular("fields") private Map<String, Object> fields;

    public InfluxDBPoint(String measurement, long timestamp) {
        this.measurement = measurement;
        this.timestamp = timestamp;
        this.fields = new HashMap<>();
        this.tags = new HashMap<>();
    }

}
