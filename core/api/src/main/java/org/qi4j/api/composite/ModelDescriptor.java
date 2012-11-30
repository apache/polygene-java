package org.qi4j.api.composite;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.MetaInfoHolder;
import org.qi4j.api.type.HasTypes;

/**
 * Composite ModelDescriptor.
 */
public interface ModelDescriptor extends HasTypes, MetaInfoHolder
{
    Visibility visibility();

    boolean isAssignableTo( Class<?> type );
}
