package org.qi4j.bootstrap;

import org.qi4j.functional.Specification;
import org.qi4j.functional.Specifications;

/**
 * Utility specifications for Assemblies.
 */
public class AssemblySpecifications
{
    public static final Specification<TypeAssembly> types( final Class... types)
    {
        return new Specification<TypeAssembly>()
        {
            @Override
            public boolean satisfiedBy( TypeAssembly item )
            {
                return Specifications.in( types ).satisfiedBy( item.type() );
            }
        };
    }
}
