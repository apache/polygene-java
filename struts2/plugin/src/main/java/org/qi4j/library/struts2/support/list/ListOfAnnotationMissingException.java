package org.qi4j.library.struts2.support.list;

public class ListOfAnnotationMissingException extends RuntimeException {

    public ListOfAnnotationMissingException(Class<?> type) {
        super("Specify the type to list with the @ListOf annotation on type " + type.getClass().getName());
    }
}
