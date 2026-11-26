package org.delcom.ifs25011p6pbo2026springbootcashflow.domain;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Penanda untuk kode yang tidak mungkin dijalankan di tes (dikecualikan Jacoco). */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Generated {
}
