package com.example.retrosocketnative;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.ProxyHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.retrosocket.EnableRSocketClients;
import org.springframework.retrosocket.PublicRSocketClientBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Set;

@TypeHint(typeNames = {"org.springframework.retrosocket.RSocketClientsRegistrar"})
@TypeHint(typeNames = {
	"org.apache.logging.log4j.message.ReusableMessageFactory",
	"org.apache.logging.log4j.message.DefaultFlowMessageFactory"
},
	access = AccessBits.ALL
)
@TypeHint(types = {Greeting.class, RSocketGreetingClient.class /* GreetingClient.class */})
@ProxyHint(types = {
	RSocketGreetingClient.class, /* GreetingClient.class, */ org.springframework.aop.SpringProxy.class,
	org.springframework.aop.framework.Advised.class, org.springframework.core.DecoratingProxy.class
})
@EnableRSocketClients
@SpringBootApplication
public class RetrosocketNativeApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(RetrosocketNativeApplication.class, args);
		Thread.sleep(10_000);
	}

/*
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
*/

	@Bean
	ApplicationListener<ApplicationReadyEvent> ready(
		RSocketGreetingClient rgc,
		ResourceLoader rl, Environment env, BeanFactory bf) {
		return event -> {

			Mono<Greeting> greet = rgc.greet("Spring fans!");
			greet.subscribe(gr -> System.out.println("response: " + gr.toString()));
//
//			var scanner = buildScanner(rl, env);
//			var packages = AutoConfigurationPackages.get(bf);
//			System.out.println("there are " + packages.size() + " package(s): " + packages);
//			packages.forEach(pkg -> process(scanner.findCandidateComponents(pkg)));
/*			GreetingClient gc = buildProxy(GreetingClient.class, bf);
			var reply = gc.greet("josh");
			System.out.println("greeting is: " + reply);*/
		};
	}

	private void process(Set<BeanDefinition> definitions) {
		definitions.forEach(bd -> System.out.println("the bean definition is " + bd.getBeanClassName() + '.'));
	}

	private static <T> T buildProxy(Class<T> iface, BeanFactory factory) {
		var pfb = new ProxyFactoryBean();
		pfb.setBeanFactory(factory);
		pfb.addInterface(iface);
		pfb.addAdvice((MethodInterceptor) methodInvocation -> {
			if (methodInvocation.getMethod().getName().contains("greet")) {
				System.out.println("arguments: " + StringUtils
					.arrayToCommaDelimitedString(methodInvocation.getArguments()));
				Assert.state(methodInvocation.getArguments()[0] instanceof String, () -> "the first parameter, name, should be a String");
				return doGreet((String) methodInvocation.getArguments()[0]);
			}
			return methodInvocation.proceed();
		});
		pfb.setTargetClass(iface);
		pfb.setTargetClass(iface);
		return (T) pfb.getObject();
	}

	private static String doGreet(String name) {
		return name.toUpperCase();
	}

	@Bean
	RSocketRequester rSocketRequester(
		@Value("${service.host:localhost}") String host,
		RSocketRequester.Builder builder) {
		return builder.tcp(host, 8181);
	}

	@Bean
	RSocketGreetingClient greetingClient(RSocketRequester rsr) {
		var rcfb = new PublicRSocketClientBuilder();
		return rcfb.buildClientFor(RSocketGreetingClient.class, rsr);
	}

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Greeting {
	private String message;
}

/*
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@interface MyClient {
}
*/

interface RSocketGreetingClient {

	@MessageMapping("greeting.{name}")
	Mono<Greeting> greet(@DestinationVariable String name);
}
/*
@MyClient
interface GreetingClient {
	String greet(String name);
}*/

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
