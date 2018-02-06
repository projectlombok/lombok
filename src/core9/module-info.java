module lombok {
	requires java.compiler;
	requires java.instrument;
	requires jdk.unsupported;
	
	exports lombok;
	exports lombok.experimental;
	exports lombok.extern.apachecommons;
	exports lombok.extern.java;
	exports lombok.extern.jbosslog;
	exports lombok.extern.log4j;
	exports lombok.extern.slf4j;
	
	provides javax.annotation.processing.Processor with lombok.launch.AnnotationProcessorHider.AnnotationProcessor;
	provides org.mapstruct.ap.spi.AstModifyingAnnotationProcessor with lombok.launch.AnnotationProcessorHider.AstModificationNotifier;
}

