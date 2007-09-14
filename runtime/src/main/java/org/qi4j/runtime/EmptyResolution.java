package org.qi4j.runtime;

import java.util.Collections;
import org.qi4j.api.DependencyInjectionContext;
import org.qi4j.api.DependencyResolution;

/**
 * TODO
 */
public final class EmptyResolution
    implements DependencyResolution
{
    public Iterable getDependencyInjection( DependencyInjectionContext context )
    {
        return Collections.EMPTY_LIST;
    }
}
