package org.qi4j.library.struts2.support;

public interface ProvidesEntityOf<T>
{
    String getId();

    void setId( String id );

    T getEntity();
}
