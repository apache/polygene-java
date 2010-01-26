package org.qi4j.runtime.util;

import java.lang.reflect.Type;
import java.util.Collection;
import org.junit.Test;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.spi.util.Annotations;

import static org.junit.Assert.*;

public class AnnotationsTest
{
    @Mixins( value = AnnotatedClass.class )
    interface AnnotatedClass<T>
    {
        Collection<T> list();
    }

    @Test
    public void getAnnotationOrNull()
        throws NoSuchMethodException
    {
        assertNotNull( "Mixins annotation found", Annotations.getAnnotation( AnnotatedClass.class, Mixins.class ) );

        assertNull( "No SideEffects annotation found", Annotations.getAnnotation( AnnotatedClass.class, SideEffects.class ) );

        final Type returnType = AnnotatedClass.class.getDeclaredMethod( "list" ).getGenericReturnType();
        assertNull( "Null on no class type", Annotations.getAnnotation( returnType, Mixins.class ) );
    }
}
