package flinkdemo.connector.influxdb;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;


import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class InfluxDBConfig implements Serializable {

	private static final long serialVersionUID = 7790863348544236493L;

	private static final int DEFAULT_BATCH_ACTIONS = 2000;
    private static final int DEFAULT_FLUSH_DURATION = 100;

    @NonNull private String url;
    @NonNull private String username;
    @NonNull private String password;
    @NonNull private String database;
    @Builder.Default private Integer batchActions = DEFAULT_BATCH_ACTIONS;
    @Builder.Default private Integer flushDuration = DEFAULT_FLUSH_DURATION;
    @Builder.Default private TimeUnit flushDurationTimeUnit = TimeUnit.MILLISECONDS;
    @Builder.Default private Boolean enableGzip = false;
    @Builder.Default private Boolean createDatabase = false;
 
}
