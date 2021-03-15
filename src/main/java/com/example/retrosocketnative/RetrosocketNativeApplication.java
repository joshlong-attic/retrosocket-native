package com.example.retrosocketnative;

import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactoryBean;
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
import org.springframework.nativex.hint.*;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.*;
import java.util.Set;

@TypeHint(typeNames = {"org.springframework.retrosocket.RSocketClientsRegistrar"})
@TypeHint(typeNames = {
	"org.apache.logging.log4j.message.ReusableMessageFactory",
	"org.apache.logging.log4j.message.DefaultFlowMessageFactory"
},
	access = AccessBits.ALL
)
@TypeHint(types = GreetingClient.class)
@ProxyHint( types = {
	GreetingClient.class, org.springframework.aop.SpringProxy.class,
	org.springframework.aop.framework.Advised.class, org.springframework.core.DecoratingProxy.class
})
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
			GreetingClient gc = buildProxy(GreetingClient.class, bf);
			var reply = gc.greet("josh");
			System.out.println("greeting is: " + reply);
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
