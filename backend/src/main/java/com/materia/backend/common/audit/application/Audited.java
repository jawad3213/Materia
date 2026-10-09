package com.materia.backend.common.audit.application;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a persistence entity whose changes are recorded in the change history. Each context marks its own entities,
 * so the shared history code needs no list of other contexts' classes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Audited {

    /** The record type shown in the history, e.g. {@code PurchaseOrder}. */
    String type();

    /** Property whose value identifies the record to a person (a code, an e-mail...). */
    String code();

    /** For a line: the property holding its document; empty for a document. */
    String parent() default "";

    /** For a line: the history type of its document. */
    String parentType() default "";

    /** Permission that lets a user read this record, and so its history; empty when only log:read may. */
    String readPermission() default "";
}
