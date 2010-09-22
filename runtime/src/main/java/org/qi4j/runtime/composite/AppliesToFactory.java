package org.qi4j.runtime.composite;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.common.ConstructionException;

public class AppliesToFactory
{

    public AppliesToFilter createAppliesToFilter( Class<?> fragmentClass )
    {
        AppliesToFilter result = null;
        if( !InvocationHandler.class.isAssignableFrom( fragmentClass ) )
        {
            result = new TypedFragmentAppliesToFilter();
            if( Modifier.isAbstract( fragmentClass.getModifiers() ) )
            {
                result = new AndAppliesToFilter( result, new ImplementsMethodAppliesToFilter() );
            }
        }
        result = applyAppliesTo( result, fragmentClass );
        if( result == null )
        {
            return AppliesToFilter.ALWAYS;
        }
        return result;
    }

    private AppliesToFilter applyAppliesTo( AppliesToFilter existing, Class<?> modifierClass )
    {
        AppliesTo appliesTo = modifierClass.getAnnotation( AppliesTo.class );
        if( appliesTo != null )
        {
            // Use "or" for all filters specified in the annotation
            AppliesToFilter appliesToAnnotation = null;
            for( Class<?> appliesToClass : appliesTo.value() )
            {
                AppliesToFilter filter;
                if( AppliesToFilter.class.isAssignableFrom( appliesToClass ) )
                {
                    try
                    {
                        filter = (AppliesToFilter) appliesToClass.newInstance();
                    }
                    catch( Exception e )
                    {
                        throw new ConstructionException( e );
                    }
                }
                else if( Annotation.class.isAssignableFrom( appliesToClass ) )
                {
                    filter = new AnnotationAppliesToFilter( appliesToClass );
                }
                else // Type check
                {
                    filter = new TypeCheckAppliesToFilter( appliesToClass );
                }

                if( appliesToAnnotation == null )
                {
                    appliesToAnnotation = filter;
                }
                else
                {
                    appliesToAnnotation = new OrAppliesToFilter( appliesToAnnotation, filter );
                }
            }
            // Add to the rest of the rules using "and"
            if( existing == null )
            {
                return appliesToAnnotation;
            }
            else
            {
                return new AndAppliesToFilter( existing, appliesToAnnotation );
            }
        }
        return existing;
    }
}
