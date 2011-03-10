package org.qi4j.bootstrap;

import org.qi4j.api.service.ServiceComposite;

/**
 * TODO
 */
public interface ServiceAssembly
{
    Class<? extends ServiceComposite> type();
}
