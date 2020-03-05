package com.fasterxml.jackson.databind.annotation;

import java.lang.annotation.*;

@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonPOJOBuilder {
	public String buildMethodName() default "build";
	public String withPrefix() default "with";
}
