package lombok.mapstruct;

import java.lang.reflect.Field;

import javax.lang.model.type.TypeMirror;

import org.mapstruct.ap.spi.AstModifyingAnnotationProcessor;

class NotifierHider {
	public static class AstModificationNotifier implements AstModifyingAnnotationProcessor {
		private static Field lombokInvoked;
		
		@Override public boolean isTypeComplete(TypeMirror type) {
			if (System.getProperty("lombok.disable") != null) return true;
			return isLombokInvoked();
		}
		
		private static boolean isLombokInvoked() {
			if (lombokInvoked != null) {
				try {
					return lombokInvoked.getBoolean(null);
				} catch (Exception e) {}
				return true;
			}
			
			try {
				Class<?> data = Class.forName("lombok.launch.AnnotationProcessorHider$AstModificationNotifierData");
				lombokInvoked = data.getField("lombokInvoked");
				return lombokInvoked.getBoolean(null);
			} catch (Exception e) {}
			return true;
		}
	}
}
