package flinkdemo.client;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

public class App {

	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("i", true, "input file");
		options.addOption("n", true, "node id");
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		if ( false == cmd.hasOption("i") )
			return;
		
		String nodeId = cmd.getOptionValue("n");
	
//		System.out.println(Long.BYTES + Float.BYTES + Float.BYTES);
		MQTT mqtt = new MQTT();
		mqtt.setHost("localhost", 1883);
		BlockingConnection blockingConnection = mqtt.blockingConnection();
		
		blockingConnection.connect();
		
		try ( Reader in = new FileReader(cmd.getOptionValue("i")) ) {
			Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
			for ( CSVRecord record : records ) {
				ByteBuffer buf = ByteBuffer.allocate(Long.BYTES + Double.BYTES + Double.BYTES);
				buf.putLong(System.currentTimeMillis());
				buf.putDouble(Double.parseDouble(record.get(0)));
				buf.putDouble(Double.parseDouble(record.get(1)));
				blockingConnection.publish("location/" + nodeId, buf.array(), QoS.AT_LEAST_ONCE, false);
				Thread.sleep(1000);
			}
		} catch ( Exception e ) {
			
		}		
	}

}
