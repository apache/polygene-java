package org.qi4j.runtime.objects;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;

/**
 * TODO
 */
public class OuterClass
{
    @Uses
    InnerClass instance;

    @Structure
    Module module;

    public String name()
    {
        return instance.name();
    }

    class InnerClass
    {
        public String name()
        {
            return module.name();
        }
    }
}
