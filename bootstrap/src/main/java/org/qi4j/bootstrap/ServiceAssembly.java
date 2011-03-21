package org.qi4j.bootstrap;

import org.qi4j.api.service.ServiceComposite;

/**
 * This represents the assembly information of a single ServiceComposite in a Module.
 */
public interface ServiceAssembly
    extends TypeAssembly<ServiceComposite>
{
   String identity();
}
