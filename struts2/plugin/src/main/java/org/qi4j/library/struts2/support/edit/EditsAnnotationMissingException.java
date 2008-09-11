package org.qi4j.library.struts2.support.edit;

public class EditsAnnotationMissingException extends RuntimeException {

    public EditsAnnotationMissingException(Class<?> type) {
        super("Specify the type to edit with the @Edits annotation on type " + type.getClass().getName());
    }
}
