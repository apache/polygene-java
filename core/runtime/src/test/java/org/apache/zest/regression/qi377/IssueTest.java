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
 *
 *
 */
package org.apache.zest.regression.qi377;

import org.junit.Test;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.property.Property;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractPolygeneTest;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class IssueTest
    extends AbstractPolygeneTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TeamMember.class );
    }

    @Test
    public void propertyNameCollisionsShouldWork()
    {
        TeamMember m = transientBuilderFactory.newTransient( TeamMember.class );
        m.name().set( "Niclas" );
        Person p = m;
        p.name().set( "Chris" );
        Employee e = m;
        e.name().set( "Paul" );

        assertThat( m.name().get(), equalTo( "Paul" ) );
        assertThat( e.name().get(), equalTo( "Paul" ) );
        assertThat( p.name().get(), equalTo( "Paul" ) );
    }

    public interface Person
    {
        @UseDefaults
        Property<String> name();
    }

    public interface Employee
    {
        @UseDefaults
        Property<String> name();
    }

    public interface TeamMember
        extends Person, Employee
    {
    }

}
