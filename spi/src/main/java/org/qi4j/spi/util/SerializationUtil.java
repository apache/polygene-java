/*
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.spi.util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class SerializationUtil
{
    private SerializationUtil()
    {
    }

    public static void writeMethod( ObjectOutput out, Method method )
        throws IOException
    {
        Class clazz = method.getDeclaringClass();
        Class[] paramTypes = method.getParameterTypes();
        out.writeObject( clazz );
        out.writeObject( method.getName() );
        out.writeObject( paramTypes );
    }

    public static Method readMethod( ObjectInput in )
        throws IOException, ClassNotFoundException
    {
        Class clazz = (Class) in.readObject();
        String methodName = (String) in.readObject();
        Class[] paramTypes = (Class[]) in.readObject();
        Method method;
        try
        {
            method = clazz.getMethod( methodName, paramTypes );
        }
        catch( NoSuchMethodException e )
        {
            // Should not be possible.
            throw new ClassNotFoundException( clazz.getName() + " no longer contain a compatible method named '" + methodName + "'", e );
        }
        return method;
    }

    public static void writeConstructor( ObjectOutput out, Constructor constructor )
        throws IOException
    {
        Class clazz = constructor.getDeclaringClass();
        Class[] paramTypes = constructor.getParameterTypes();
        out.writeObject( clazz );
        out.writeObject( paramTypes );
    }

    public static Constructor readConstructor( ObjectInput in )
        throws IOException, ClassNotFoundException
    {
        Class clazz = (Class) in.readObject();
        Class[] paramTypes = (Class[]) in.readObject();
        Constructor constructor;
        try
        {
            constructor = clazz.getConstructor( paramTypes );
        }
        catch( NoSuchMethodException e )
        {
            // Should not be possible.
            throw new ClassNotFoundException( clazz.getName() + " no longer has a compatible constructor.", e );
        }
        return constructor;
    }

//    public static void writeType( ObjectOutput out, Type type )
//    {
//        // TODO: How on earth to do this???
//    }
//
//
//    public static Type readType( ObjectInput in )
//    {
//        // TODO: How on earth to do this???
//        return null;
//    }

    public static Field readField( ObjectInputStream in )
        throws IOException, ClassNotFoundException
    {
        Class declaringClass = (Class) in.readObject();
        String name = (String) in.readObject();
        Field field = null;
        try
        {
            field = declaringClass.getDeclaredField( name );
        }
        catch( NoSuchFieldException e )
        {
            // Should not be possible.
            throw new ClassNotFoundException( declaringClass.getName() + " no longer has a field name '" + name + "'.", e );
        }
        return field;  //To change body of created methods use File | Settings | File Templates.
    }

    public static void writeField( ObjectOutputStream out, Field field )
        throws IOException
    {
        final String name = field.getName();
        final Class<?> declaringClass = field.getDeclaringClass();
        out.writeObject( declaringClass );
        out.writeObject( name );
    }
}
