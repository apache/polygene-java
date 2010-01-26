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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Assert;
import org.junit.Test;

public class SerializationUtilTests
{
    @Test
    public void testWriteReadMethod()
        throws IOException, NoSuchMethodException, ClassNotFoundException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        Method method1 = getMethod( SerializationUtilTests.class, "testWriteReadMethod" );
        SerializationUtil.writeMethod( oos, method1 );
        oos.flush();
        ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( bais );
        final Method method2 = SerializationUtil.readMethod( ois );
        Assert.assertEquals( method1, method2 );
    }

    @Test
    public void testWriteReadConstructor()
        throws IOException, NoSuchMethodException, ClassNotFoundException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        Constructor cons1 = String.class.getConstructor( ( new char[0] ).getClass() );
        SerializationUtil.writeConstructor( oos, cons1 );
        oos.flush();
        ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( bais );
        Constructor cons2 = SerializationUtil.readConstructor( ois );
        Assert.assertEquals( cons1, cons2 );
    }

    @Test
    public void testWriteReadField()
        throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        Field field1 = String.class.getDeclaredField( "offset" );
        SerializationUtil.writeField( oos, field1 );
        oos.flush();
        ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( bais );
        Field field2 = SerializationUtil.readField( ois );
        Assert.assertEquals( field1, field2 );
    }

    private Method getMethod( Class clazz, String methodName, Class... parameterTypes )
        throws NoSuchMethodException
    {
        return clazz.getMethod( methodName, parameterTypes );
    }
}
