/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.test;

import java.lang.reflect.Method;
import java.util.List;
import org.qi4j.api.composite.CompositeInvoker;

/**
 * JAVADOC
 */
public class SomeMixin_Stubx
    extends SomeMixin
{
    public CompositeInvoker _instance;

    private static Method m1;
    private static Method m2;
    private static Method m3;
    private static Method m4;
    private static Method m5;
    private static Method m6;

    private static Method m7;
    private static Method m8;

    static
    {
        try
        {
            m1 = Other.class.getMethod( "other" );
            m2 = Other.class.getMethod( "foo", String.class, Integer.TYPE );
            m3 = Other.class.getMethod( "bar",
                                        Double.TYPE,
                                        Boolean.TYPE,
                                        Float.TYPE,
                                        Character.TYPE,
                                        Integer.TYPE,
                                        Long.TYPE,
                                        Short.TYPE,
                                        Byte.TYPE,
                                        Double.class,
                                        Object[].class,
                                        int[].class );
            m4 = Other.class.getMethod( "multiEx", String.class );
            m5 = Other.class.getMethod( "unwrapResult" );
            m6 = Other.class.getMethod( "generic", List.class );

            m7 = Some.class.getMethod( "testConcern" );
            m8 = World.class.getMethod( "someMethod", String.class, Double.TYPE, Integer.TYPE );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
        }
    }

    public SomeMixin_Stubx()
    {
        super();
    }

    public SomeMixin_Stubx( String foo )
    {
        super( foo );
    }

    public String other()
    {
        try
        {
            return (String) _instance.invokeComposite( m1, null );
        }
        catch( RuntimeException runtime )
        {
            throw runtime;
        }
        catch( Throwable error )
        {
            throw (Error) error;
        }
    }

    public String foo( String bar, int x )
        throws IllegalArgumentException
    {
        try
        {
            return (String) _instance.invokeComposite( m2, new Object[]{ bar, x } );
        }
        catch( IllegalArgumentException ex )
        {
            throw ex;
        }
        catch( RuntimeException runtime )
        {
            throw runtime;
        }
        catch( Throwable error )
        {
            throw (Error) error;
        }
    }

    public void bar( double doub,
                     boolean bool,
                     float fl,
                     char ch,
                     int integer,
                     long lg,
                     short sh,
                     byte b,
                     Double doubObj,
                     Object[] objArr,
                     int[] intArr
    )
    {
        try
        {
            _instance.invokeComposite( m3, new Object[]{ doub, bool, fl, ch, integer, lg, sh, b, doubObj, objArr, intArr } );
        }
        catch( RuntimeException runtime )
        {
            throw runtime;
        }
        catch( Throwable error )
        {
            throw (Error) error;
        }
    }

    public void multiEx( String bar )
        throws Exception1, Exception2
    {
        try
        {
            _instance.invokeComposite( m4, new Object[]{ bar } );
        }
        catch( Exception1 throwable )
        {
            throw throwable;
        }
        catch( Exception2 throwable )
        {
            throw throwable;
        }
        catch( RuntimeException runtime )
        {
            throw runtime;
        }
        catch( Throwable error )
        {
            throw (Error) error;
        }
    }

    public long unwrapResult()
    {
        try
        {
            return (Long) _instance.invokeComposite( m5, null );
        }
        catch( RuntimeException runtime )
        {
            throw runtime;
        }
        catch( Throwable error )
        {
            throw (Error) error;
        }
    }

    public void generic( List<String> list )
    {
        try
        {
            _instance.invokeComposite( m6, new Object[]{ list } );
        }
        catch( RuntimeException runtime )
        {
            throw runtime;
        }
        catch( Throwable error )
        {
            throw (Error) error;
        }
    }

    public String testConcern()
    {
        try
        {
            return (String) _instance.invokeComposite( m7, null );
        }
        catch( RuntimeException runtime )
        {
            throw runtime;
        }
        catch( Throwable error )
        {
            throw (Error) error;
        }
    }

    public String _testConcern()
    {
        return super.testConcern();
    }

    @Override
    public String someMethod( String foo, double x, int y )
    {
        try
        {
            return (String) _instance.invokeComposite( m8, new Object[]{ foo, x, y } );
        }
        catch( RuntimeException runtime )
        {
            throw runtime;
        }
        catch( Throwable error )
        {
            throw (Error) error;
        }
    }

    public String _someMethod( String foo, double x, int y )
    {
        return super.someMethod( foo, x, y );
    }
}
