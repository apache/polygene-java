package org.qi4j.bootstrap;

import org.qi4j.api.type.HasTypes;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Specifications;

/**
 * Utility specifications for Assemblies.
 */
public class AssemblySpecifications
{
    public static Specification<HasTypes> types( final Class... types )
    {
        return new Specification<HasTypes>()
        {
            @Override
            public boolean satisfiedBy( HasTypes item )
            {

                for( Class<?> type : item.types() )
                {
                    if( Specifications.in( types ).satisfiedBy( type ) )
                    {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
