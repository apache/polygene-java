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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

public class MethodSetTest
{
    @Test
    public void whenStoringFiveMethodsInMethodSetThenItCanBeRetrieved()
        throws Exception
    {
        MethodSet set = new MethodSet();
        final Method method1 = getMethod( ArrayList.class, "add" );
        final Method method2 = getMethod( System.class, "currentTimeMillis" );
        final Method method3 = getMethod( String.class, "replace" );
        final Method method4 = getMethod( MethodSetTest.class, "whenStoringFiveMethodsInMethodSetThenItCanBeRetrieved" );
        final Method method5 = getMethod( Date.class, "toString" );
        set.add( method1 );
        set.add( method2 );
        set.add( method3 );
        set.add( method4 );
        set.add( method5 );
        MethodSet resultSet = saveSetAndReloadIt( set );
        Assert.assertTrue( resultSet.contains( method1 ) );
        Assert.assertTrue( resultSet.contains( method2 ) );
        Assert.assertTrue( resultSet.contains( method3 ) );
        Assert.assertTrue( resultSet.contains( method4 ) );
        Assert.assertTrue( resultSet.contains( method5 ) );
    }

    @Test
    public void whenStoringOverloadedMethodsInMethodSetThenItCanBeRetrieved()
        throws Exception
    {
        MethodSet set = new MethodSet();
        Method method1 = getMethod( StringBuffer.class, "append", Character.TYPE );
        Method method2 = getMethod( StringBuffer.class, "append", Integer.TYPE );
        Method method3 = getMethod( StringBuffer.class, "append", Object.class );
        Method method4 = getMethod( StringBuffer.class, "append", char[].class );
        Method method5 = getMethod( StringBuffer.class, "append", CharSequence.class );
        Method method6 = getMethod( StringBuffer.class, "append", Long.TYPE );
        Method method7 = getMethod( StringBuffer.class, "append", Float.TYPE );
        Method method8 = getMethod( StringBuffer.class, "append", Double.TYPE );
        set.add( method1 );
        set.add( method2 );
        set.add( method3 );
        set.add( method4 );
        set.add( method5 );
        set.add( method6 );
        set.add( method7 );
        set.add( method8 );
        MethodSet resultSet = saveSetAndReloadIt( set );
        Assert.assertTrue( resultSet.contains( method1 ) );
        Assert.assertTrue( resultSet.contains( method2 ) );
        Assert.assertTrue( resultSet.contains( method3 ) );
        Assert.assertTrue( resultSet.contains( method4 ) );
        Assert.assertTrue( resultSet.contains( method5 ) );
        Assert.assertTrue( resultSet.contains( method6 ) );
        Assert.assertTrue( resultSet.contains( method7 ) );
        Assert.assertTrue( resultSet.contains( method8 ) );
    }

    private Method getMethod( Class clazz, String methodName, Class... parameterTypes )
        throws NoSuchMethodException
    {
        return clazz.getMethod( methodName, parameterTypes );
    }

    private Method getMethod( Class clazz, String methodName )
    {
        for( Method method : clazz.getMethods() )
        {
            if( method.getName().equals( methodName ) )
            {
                return method;
            }
        }
        throw new InternalError( "Error in testcase." );
    }

    private MethodSet saveSetAndReloadIt( MethodSet set )
        throws IOException, ClassNotFoundException
    {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( bas );
        oos.writeObject( set );
        oos.flush();
        ByteArrayInputStream in = new ByteArrayInputStream( bas.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( in );
        MethodSet resultSet = (MethodSet) ois.readObject();
        return resultSet;
    }
}
