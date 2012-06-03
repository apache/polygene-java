package org.qi4j.api.scala;

import org.qi4j.functional.Function;

class ClassClassFunction
    implements Function<Class, Class>
{
    private Class current;
    private final Class<?> declaringClass;

    public ClassClassFunction( Class<?> declaringClass )
    {
        this.declaringClass = declaringClass;
    }

    @Override
    public Class map( Class classToMap )
    {
        if( declaringClass.isAssignableFrom( classToMap ) )
        {
            try
            {
                TraitMixin.tryToLoadTraitClass( classToMap );
                if( current == null )
                {
                    current = classToMap;
                }
                else
                {
                    current = determineSubclassRelationship( classToMap );
                }
            }
            catch( ClassNotFoundException e )
            {
                // Ignore - no trait implementation found
            }
        }

        return current;
    }

    private Class determineSubclassRelationship( Class candidateClass )
    {
        boolean candidateClassIsSubclassOfCurrent = current.isAssignableFrom( candidateClass );
        if( candidateClassIsSubclassOfCurrent )
        {
            return candidateClass;
        }
        else
        {
            return current;
        }
    }

}
