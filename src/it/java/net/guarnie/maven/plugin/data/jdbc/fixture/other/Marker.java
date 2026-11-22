package net.guarnie.maven.plugin.data.jdbc.fixture.other;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation with the same simple name as {@code fixture.Marker}, used to test import conflicts. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface Marker {
}
