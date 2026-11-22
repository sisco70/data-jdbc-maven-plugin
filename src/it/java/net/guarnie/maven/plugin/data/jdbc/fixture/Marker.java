package net.guarnie.maven.plugin.data.jdbc.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation used by the importRegistry tests; its arguments reference other registry classes. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface Marker {
    RecordKind kind() default RecordKind.SYSTEM_USER;

    String note() default "";
}
