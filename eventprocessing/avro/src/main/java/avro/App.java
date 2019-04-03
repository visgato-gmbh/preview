package avro;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.aiblab.data.minewg1.MinewDataDao;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {


	@SuppressWarnings("serial")
	public static class MinewServlet extends HttpServlet {
		
		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
			
			try {
				ObjectMapper mapper = new ObjectMapper();
				List<MinewDataDao> dataArray = mapper.readValue(request.getInputStream(), new TypeReference<List<MinewDataDao>>() {});
				
				Message<List<MinewDataDao>> message = MessageBuilder.withPayload(dataArray)
						.setHeader(KafkaHeaders.TOPIC, "minew")
						.build();
				kafkaTemplate.send(message);

			} catch (Exception e) {
				log.error("{}", e);
			}
			
			response.setStatus(HttpServletResponse.SC_OK);
		}
	}
	
	private static Server httpServer;
	
	private static void startServer() throws Exception {
		httpServer = new Server(8080);
		ServletHandler handler = new ServletHandler();
		httpServer.setHandler(handler);

		handler.addServletWithMapping(MinewServlet.class, "/*");
		 
		httpServer.start();
		httpServer.join();
	}
	
	private static KafkaTemplate<String, MinewDataDao> kafkaTemplate;
	
	private static void buildKafkaTemplate() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
	}
	
	public static void main(String[] args) throws Exception {
		
		log.info("connecting kafka...");		
		buildKafkaTemplate();
		log.info("connected kafka");		
		
		log.info("Starting server...");		
        startServer();        
		log.info("Starting started {}", httpServer);
		
	}

}
