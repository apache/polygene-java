package org.qi4j.bootstrap;

import org.qi4j.api.type.HasTypes;

/**
 * This represents the assembly information of a single ServiceComposite in a Module.
 */
public interface ServiceAssembly extends HasTypes
{
    String identity();
}
