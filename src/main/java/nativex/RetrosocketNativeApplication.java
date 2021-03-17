package nativex;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
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

//@TypeHint(types = Greeting.class, access = AccessBits.ALL)
@EnableRSocketClients
@SpringBootApplication
public class RetrosocketNativeApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(RetrosocketNativeApplication.class, args);
		Thread.sleep(5_000);
	}

	@Bean
	ApplicationListener<ApplicationReadyEvent> ready(GreetingClient rgc) {
		return event -> {
			Mono<Greeting> greet = rgc.greet("Spring fans");
			greet.subscribe(gr -> System.out.println("response: " + gr.toString()));
			rgc
				.greetWithoutWrapper("RSocket fans")
				.subscribe(gr -> System.out.println("response: " + gr.toString()));
		};
	}

	@Bean
	RSocketRequester rSocketRequester(
		@Value("${service.host:localhost}") String host,
		RSocketRequester.Builder builder) {
		return builder.tcp(host, 8181);
	}
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Greeting {
	private String message;
}


@RSocketClient
interface GreetingClient {

	@MessageMapping("greeting.{name}")
	Mono<Greeting> greetWithoutWrapper(@DestinationVariable String name);

	@MessageMapping("greeting.{name}")
	Mono<Greeting> greet(@DestinationVariable String name);
}


