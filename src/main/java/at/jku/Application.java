package at.jku;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Random;

@SpringBootApplication
public class Application {
    private static final String power_usage = "power";
    private static final String order_id = "rfidID";
    private static final String name = "Stampfmaschine";
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    @Value("${my-project.topic}")
    private String topic;

  public static void main(String... args) {
    SpringApplication.run(Application.class, args);
  }
    @Bean
    public JavaDelegate randomPowerConsumption(){
        return execution -> {
            execution.setVariable(power_usage, new Random().nextLong());
            while (Long.valueOf(execution.getVariable(power_usage).toString()) <0){
                execution.setVariable(power_usage, new Random().nextLong());
            }
        };
    }
  @Bean
  public JavaDelegate helloMQTT(){
    return execution -> {
        LOGGER.debug("Starting with MQTT");
        final MQTT mqtt = new MQTT();
        mqtt.setHost("localhost", 1883);
        final BlockingConnection connection = mqtt.blockingConnection();
        connection.connect();
        final Object RFID = execution.getVariable(order_id);
        final Object POWERUSE = String.valueOf(execution.getVariable(power_usage));
        final Object MASCHINE = String.valueOf(name);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("order-id", RFID);
        jsonObject.put("machine-name", MASCHINE);
        jsonObject.put("power-consumption", POWERUSE);
        connection.publish(topic, jsonObject.toString().getBytes(), QoS.AT_LEAST_ONCE, false);
        LOGGER.debug("Published {} to {}", POWERUSE, topic);
    };
  }
}