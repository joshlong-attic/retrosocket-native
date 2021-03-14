package com.example.retrosocketnative;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.retrosocket.EnableRSocketClients;
import org.springframework.retrosocket.RSocketClient;
import reactor.core.publisher.Mono;

import java.awt.desktop.AppForegroundListener;

@TypeHint(typeNames = {"org.springframework.retrosocket.RSocketClientsRegistrar"})
@TypeHint(types = Greeting.class)
@TypeHint(typeNames = {
	"org.apache.logging.log4j.message.ReusableMessageFactory",
	"org.apache.logging.log4j.message.DefaultFlowMessageFactory"
},
	access = AccessBits.ALL
)
@EnableRSocketClients
@SpringBootApplication
public class RetrosocketNativeApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(RetrosocketNativeApplication.class, args);
		Thread.sleep(1000);
	}

	@Bean
	RSocketRequester requester(RSocketRequester.Builder builder) {
		return builder.tcp("localhost", 8181);
	}

	@Bean
	ApplicationListener<ApplicationReadyEvent> ready(GreetingClient gc) {
		return event -> gc.greet("World").subscribe(g -> System.out.println(g.toString()));
	}


}


@RSocketClient
interface GreetingClient {

	@MessageMapping("greeting.{name}")
	Mono<Greeting> greet(@DestinationVariable String name);

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Greeting {
	private String message;
}