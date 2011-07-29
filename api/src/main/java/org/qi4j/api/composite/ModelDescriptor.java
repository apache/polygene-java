package org.qi4j.api.composite;

import org.qi4j.api.common.Visibility;

/**
 * TODO
 */
public interface ModelDescriptor
{
    Class<?> type();

    <T> T metaInfo( Class<T> infoType );

    Visibility visibility();

    boolean isAssignableTo(Class<?> type);
}
