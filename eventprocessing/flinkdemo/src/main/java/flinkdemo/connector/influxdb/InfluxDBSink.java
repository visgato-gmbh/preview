package flinkdemo.connector.influxdb;

import java.util.concurrent.TimeUnit;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.util.CollectionUtil;
import org.apache.flink.util.StringUtils;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InfluxDBSink extends RichSinkFunction<InfluxDBPoint> {

	private static final long serialVersionUID = 150534448693367417L;

	private transient InfluxDB influxDBClient;
    @NonNull private final InfluxDBConfig influxDBConfig;
    
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

       if ( influxDBConfig.getUsername().isEmpty() )
    	   influxDBClient = InfluxDBFactory.connect(influxDBConfig.getUrl());
       else
    	   influxDBClient = InfluxDBFactory.connect(influxDBConfig.getUrl(), influxDBConfig.getUsername(), influxDBConfig.getPassword());
/*
        if (!influxDBClient.databaseExists(influxDBConfig.getDatabase())) {
            if(influxDBConfig.isCreateDatabase()) {
                influxDBClient.createDatabase(influxDBConfig.getDatabase());
            }
            else {
                throw new RuntimeException("This " + influxDBConfig.getDatabase() + " database does not exist!");
            }
        }
*/
        influxDBClient.setDatabase(influxDBConfig.getDatabase());

        if (influxDBConfig.getBatchActions() > 0) {
            influxDBClient.enableBatch(influxDBConfig.getBatchActions(), influxDBConfig.getFlushDuration(), influxDBConfig.getFlushDurationTimeUnit());
        }

        if (influxDBConfig.getEnableGzip()) {

            influxDBClient.enableGzip();
        }
    }

    /**
     * Called when new data arrives to the sink, and forwards it to InfluxDB.
     *
     * @param dataPoint {@link InfluxDBPoint}
     */
    @Override
    public void invoke(InfluxDBPoint dataPoint, @SuppressWarnings("rawtypes") Context context) throws Exception {
        if (StringUtils.isNullOrWhitespaceOnly(dataPoint.getMeasurement())) {
            throw new RuntimeException("No measurement defined");
        }

        Point.Builder builder = Point.measurement(dataPoint.getMeasurement())
                .time(dataPoint.getTimestamp(), TimeUnit.MILLISECONDS);

        if (!CollectionUtil.isNullOrEmpty(dataPoint.getFields())) {
            builder.fields(dataPoint.getFields());
        }

        if (!CollectionUtil.isNullOrEmpty(dataPoint.getTags())) {
            builder.tag(dataPoint.getTags());
        }
        
        Point point = builder.build();
        influxDBClient.write(point);
    }

    @Override
    public void close() {
        if (influxDBClient.isBatchEnabled()) {
            influxDBClient.disableBatch();
        }
        influxDBClient.close();
    }    
}
