/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.zest.test.util;

import org.hamcrest.Matcher;
import org.junit.Assert;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * JMX Test Fixture.
 */
public class JmxFixture extends Assert {

    private final MBeanServer server;
    private final String prefix;

    public JmxFixture()
    {
        this( ManagementFactory.getPlatformMBeanServer(), "" );
    }

    public JmxFixture( String prefix )
    {
        this( ManagementFactory.getPlatformMBeanServer(), prefix );
    }

    public JmxFixture( MBeanServer server )
    {
        this( server, "" );
    }

    public JmxFixture( MBeanServer server, String prefix )
    {
        this.server = server;
        this.prefix = prefix;
    }

    public String prefix() {
        return prefix;
    }

    public boolean objectExists( String objName ) {
        try
        {
            ObjectName objectName = new ObjectName( prefix + objName );
            return server.isRegistered( objectName );
        }
        catch( MalformedObjectNameException ex )
        {
            throw new IllegalArgumentException( ex.getMessage(), ex );
        }
    }


    public void assertObjectPresent( String objName )
    {
        if( !objectExists( objName ) )
        {
            fail( objName + " is absent" );
        }
    }

    public void assertObjectAbsent( String objName )
    {
        if( objectExists( objName ) ) {
            fail( objName + " is present" );
        }
    }

    public <T> void assertAttributeValue( String objName, String attribute, Class<? extends T> type, Matcher<? super T> matcher )
    {
        assertThat( attributeValue( objName, attribute, type ), matcher );
    }

    public <T> T attributeValue( String objName, String attribute, Class<? extends T> type )
    {
        try
        {
            ObjectName objectName = new ObjectName( prefix + objName );
            Object value = server.getAttribute( objectName, attribute );
            return type.cast( value );
        }
        catch( MalformedObjectNameException ex )
        {
            throw new IllegalArgumentException( ex.getMessage(), ex );
        }
        catch( JMException ex )
        {
            throw new RuntimeException( ex.getMessage(), ex );
        }
    }

    public List<String> allObjectNames()
    {
        return server.queryNames( null, null ).stream().map( ObjectName::toString ).collect( toList() );
    }
}