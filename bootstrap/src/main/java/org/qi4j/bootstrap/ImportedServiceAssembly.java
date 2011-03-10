package org.qi4j.bootstrap;

import org.qi4j.api.entity.EntityComposite;

/**
 * TODO
 */
public interface ImportedServiceAssembly
{
    Class<? extends EntityComposite> type();
}
