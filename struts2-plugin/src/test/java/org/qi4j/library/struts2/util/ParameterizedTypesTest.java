package org.qi4j.library.struts2.util;

import org.junit.Test;

import java.lang.reflect.ParameterizedType;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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
        assertArrayEquals(
                ((ParameterizedType) GarbageMan.class.getGenericInterfaces()[0]).getActualTypeArguments(),
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
