/*
 * Copyright 2007 Alin Dreghiciu. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.qi4j.library.beans.properties;

import java.lang.reflect.Proxy;
import java.util.Iterator;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.assertFalse;

public class PropertiesMixinTest
{
    private PropertiesMixin m_underTest;
    private Value m_proxy;

    @Before
    public void setUp() throws NoSuchMethodException
    {
        m_underTest = new PropertiesMixin();
        m_proxy = (Value) Proxy.newProxyInstance( Value.class.getClassLoader(), new Class[]{ Value.class }, m_underTest );
    }

    @Test
    public void setAndGetFoo()
    {
        m_proxy.setFoo( "aValue" );
        assertEquals( "aValue", m_proxy.getFoo() );
    }

    @Test
    public void setAndGet()
    {
        m_proxy.set( "aValue" );
        assertEquals( "aValue", m_proxy.get() );
    }

    @Test
    public void getFooWithoutSetFoo()
    {
        assertEquals( null, m_proxy.getFoo() );
    }

    @Test
    public void iterateBarAfterAddBar()
    {
        m_proxy.addBar( "aValue" );
        Iterator<String> iterator = m_proxy.barIterator();
        assertNotNull( "iterator should not be null", iterator );
        assertEquals( "iterator has a value", true, iterator.hasNext() );
        assertEquals( "iterator content", "aValue", iterator.next() );
    }

    @Test
    public void iterateAfterAdd()
    {
        m_proxy.add( "aValue" );
        Iterator<String> iterator = m_proxy.iterator();
        assertNotNull( "iterator should not be null", iterator );
        assertEquals( "iterator has a value", true, iterator.hasNext() );
        assertEquals( "iterator content", "aValue", iterator.next() );
    }

    @Test
    public void iterateBarAfterAddAndRemoveBar()
    {
        m_proxy.addBar( "aValue" );
        m_proxy.removeBar( "aValue" );
        Iterator<String> iterator = m_proxy.barIterator();
        assertNotNull( iterator );
        assertFalse( iterator.hasNext() );
    }

    @Test
    public void iterateAfterAddAndRemove()
    {
        m_proxy.add( "aValue" );
        m_proxy.remove( "aValue" );
        Iterator<String> iterator = m_proxy.iterator();
        assertNotNull( iterator );
        assertFalse( iterator.hasNext() );
    }

    @Test
    public void removeBarWithoutAddBar()
    {
        m_proxy.removeBar( "aValue" );
    }

    @Test
    public void removeWithoutAdd()
    {
        m_proxy.remove( "aValue" );
    }

    @Test
    public void iterateBarWithoutAddBar()
    {
        Iterator<String> iterator = m_proxy.barIterator();
        assertNotNull( "iterator not supposed to be null", iterator );
        assertFalse( iterator.hasNext() );
    }

    @Test
    public void iterateWithoutAdd()
    {
        Iterator<String> iterator = m_proxy.barIterator();
        assertNotNull( "iterator not supposed to be null", iterator );
        assertFalse( iterator.hasNext() );
    }

    @Test
    public void addFooAndGetFoo()
    {
        m_proxy.addFoo( "aValue" );
        assertNull( "getter supposed to be null", m_proxy.getFoo() );
    }

    @Test
    public void addFooAndSetFoo()
    {
        m_proxy.addFoo( "addValue" );
        m_proxy.setFoo( "setValue" );
    }

    @Test
    public void setFooAndAddFoo()
    {
        m_proxy.setFoo( "setValue" );
        m_proxy.addFoo( "addValue" );
    }

    @Test
    public void setFooAndRemoveFoo()
    {
        m_proxy.setFoo( "aValue" );
        m_proxy.removeFoo( "aValue" );
        assertEquals( "aValue", m_proxy.getFoo() );
    }

    @Test
    public void setFooAndIterateFoo()
    {
        m_proxy.setFoo( "aValue" );
        Iterator<String> iterator = m_proxy.fooIterator();
        assertNotNull( "iterator not supposed to be null", iterator );
        assertFalse(  iterator.hasNext() );
    }

    public static interface Value
    {
        public String getFoo();

        public void setFoo( String value );

        public String get();

        public void set( String value );

        public void addFoo( String value );

        public void removeFoo( String value );

        public Iterator<String> fooIterator();

        public void addBar( String value );

        public void removeBar( String value );

        public Iterator<String> barIterator();

        public void add( String value );

        public void remove( String value );

        public Iterator<String> iterator();
    }

}
