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

public class MethodValueMapTest
{
    @Test
    public void whenStoringFiveMethodsInMethodValueMapThenItCanBeRetrieved()
        throws Exception
    {
        MethodValueMap<Integer> map = new MethodValueMap<Integer>();
        final Method method1 = getMethod( ArrayList.class, "add" );
        final Method method2 = getMethod( System.class, "currentTimeMillis" );
        final Method method3 = getMethod( String.class, "replace" );
        final Method method4 = getMethod( MethodValueMapTest.class, "whenStoringFiveMethodsInMethodValueMapThenItCanBeRetrieved" );
        final Method method5 = getMethod( Date.class, "toString" );
        map.put( 1, method1 );
        map.put( 2, method2 );
        map.put( 3, method3 );
        map.put( 4, method4 );
        map.put( 5, method5 );
        MethodValueMap resultMap = saveMapAndReloadIt( map );
        Assert.assertTrue( resultMap.containsValue( method1 ) );
        Assert.assertTrue( resultMap.containsValue( method2 ) );
        Assert.assertTrue( resultMap.containsValue( method3 ) );
        Assert.assertTrue( resultMap.containsValue( method4 ) );
        Assert.assertTrue( resultMap.containsValue( method5 ) );
    }

    @Test
    public void whenStoringOverloadedMethodsInMethodValueMapThenItCanBeRetrieved()
        throws Exception
    {
        MethodValueMap<Integer> map = new MethodValueMap<Integer>();
        Method method1 = getMethod( StringBuffer.class, "append", Boolean.TYPE );
        Method method2 = getMethod( StringBuffer.class, "append", Integer.TYPE );
        Method method3 = getMethod( StringBuffer.class, "append", Object.class );
        Method method4 = getMethod( StringBuffer.class, "append", Character.TYPE );
        Method method5 = getMethod( StringBuffer.class, "append", ( new char[0] ).getClass() );
        Method method6 = getMethod( StringBuffer.class, "append", ( new char[0] ).getClass(), Integer.TYPE, Integer.TYPE );
        Method method7 = getMethod( StringBuffer.class, "append", Long.TYPE );
        Method method8 = getMethod( StringBuffer.class, "append", Double.TYPE );
        map.put( 1, method1 );
        map.put( 2, method2 );
        map.put( 3, method3 );
        map.put( 4, method4 );
        map.put( 5, method5 );
        map.put( 6, method6 );
        map.put( 7, method7 );
        map.put( 8, method8 );
        MethodValueMap resultMap = saveMapAndReloadIt( map );
        Assert.assertTrue( resultMap.containsValue( method1 ) );
        Assert.assertTrue( resultMap.containsValue( method2 ) );
        Assert.assertTrue( resultMap.containsValue( method3 ) );
        Assert.assertTrue( resultMap.containsValue( method4 ) );
        Assert.assertTrue( resultMap.containsValue( method5 ) );
        Assert.assertTrue( resultMap.containsValue( method6 ) );
        Assert.assertTrue( resultMap.containsValue( method7 ) );
        Assert.assertTrue( resultMap.containsValue( method8 ) );
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

    private MethodValueMap saveMapAndReloadIt( MethodValueMap map )
        throws IOException, ClassNotFoundException
    {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( bas );
        oos.writeObject( map );
        oos.flush();
        ByteArrayInputStream in = new ByteArrayInputStream( bas.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( in );
        MethodValueMap resultSet = (MethodValueMap) ois.readObject();
        return resultSet;
    }
}