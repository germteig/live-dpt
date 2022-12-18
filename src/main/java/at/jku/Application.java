package at.jku;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
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
        execution.getVariable(order_id);
        String value = String.valueOf(execution.getVariable(power_usage));
        connection.publish(topic, value.getBytes(), QoS.AT_LEAST_ONCE, false);
        LOGGER.debug("Published {} to {}");
    };
  }
}