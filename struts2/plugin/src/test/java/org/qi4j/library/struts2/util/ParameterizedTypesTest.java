package org.qi4j.library.struts2.util;

import java.lang.reflect.ParameterizedType;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ParameterizedTypesTest
{
    @Test
    public void findParameterizedType()
    {
        assertEquals(
            GarbageMan.class.getGenericInterfaces()[ 0 ],
            ParameterizedTypes.findParameterizedType( GarbageMan.class, HandlerOf.class )
        );
    }

    @Test
    public void findTypeVariables()
    {
        assertEquals(
            ( (ParameterizedType) GarbageMan.class.getGenericInterfaces()[ 0 ] ).getActualTypeArguments(),
            ParameterizedTypes.findTypeVariables( GarbageMan.class, HandlerOf.class )
        );
    }

    static interface HandlerOf<T>
    {
    }

    static interface Trash
    {
    }

    static final class GarbageMan implements HandlerOf<Trash>
    {
    }
}
