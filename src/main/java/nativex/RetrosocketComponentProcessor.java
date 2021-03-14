package nativex;

import org.springframework.nativex.type.ComponentProcessor;
import org.springframework.nativex.type.NativeContext;

import java.util.List;

/**
	* we need to register any interface that has {@code @RSocketClient} on it.
	*
	*/
public class RetrosocketComponentProcessor implements ComponentProcessor {

	@Override
	public boolean handle(NativeContext nativeContext, String className, List<String> list) {

		System.out.println("RETROSOCKET: should we handle " + nativeContext.getTypeSystem()
			.resolve( className) .getDottedName());


		return false;
	}

	@Override
	public void process(NativeContext nativeContext, String s, List<String> list) {

	}
}
