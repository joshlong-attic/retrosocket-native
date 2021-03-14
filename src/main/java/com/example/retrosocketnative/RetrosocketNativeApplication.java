package com.example.retrosocketnative;

import lombok.SneakyThrows;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHints;
import org.springframework.nativex.hint.ProxyHint;
import org.springframework.nativex.hint.TypeHint;

import java.lang.annotation.*;
import java.util.List;
import java.util.Set;

@TypeHint(typeNames = {"org.springframework.retrosocket.RSocketClientsRegistrar"})
@TypeHint(typeNames = {
	"org.apache.logging.log4j.message.ReusableMessageFactory",
	"org.apache.logging.log4j.message.DefaultFlowMessageFactory"
},
	access = AccessBits.ALL
)
@TypeHint(types = GreetingClient.class)
@ProxyHint(types = GreetingClient.class)
@SpringBootApplication
public class RetrosocketNativeApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(RetrosocketNativeApplication.class, args);
		Thread.sleep(10_000);
	}

	private ClassPathScanningCandidateComponentProvider buildScanner(
		ResourceLoader rl,
		Environment environment) {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
			false, environment) {
			@Override
			protected boolean isCandidateComponent(AnnotatedBeanDefinition metadata) {
				System.out.println("isCandidateComponent? " + metadata.toString());
				return metadata.getMetadata().isIndependent() && !metadata.getMetadata().isAnnotation();
			}
		};
		scanner.addIncludeFilter(new AnnotationTypeFilter(MyClient.class));
		scanner.setResourceLoader(rl);
		return scanner;
	}


	@Bean
	ApplicationListener<ApplicationReadyEvent> ready(ResourceLoader rl, Environment env, BeanFactory bf) {
		return event -> {
			var scanner = buildScanner(rl, env);
			var packages = AutoConfigurationPackages.get(bf);
			System.out.println("there are " + packages.size() + " package(s): " + packages);
			packages.forEach(pkg -> process(scanner.findCandidateComponents(pkg)));
		};
	}

	private void process(Set<BeanDefinition> definitions) {
		definitions.forEach(bd -> System.out.println("the bean definition is " + bd.getBeanClassName() + '.'));
	}

}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@interface MyClient {
}

@MyClient
interface GreetingClient {

	String greet(String name);
}

/*

@RSocketClient
interface GreetingClient {

	@MessageMapping("greeting.{name}")
	Mono<Greeting> greet(@DestinationVariable String name);

}
*/
/*

@Data
@AllArgsConstructor
@NoArgsConstructor
class Greeting {
	private String message;
}*/
