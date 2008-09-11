package org.qi4j.library.struts2.support.add;

public class AddsAnnotationMissingException extends RuntimeException {

    public AddsAnnotationMissingException(Class<?> type) {
        super("Specify the type to add with the @Adds annotation on type " + type.getName());
    }
}
