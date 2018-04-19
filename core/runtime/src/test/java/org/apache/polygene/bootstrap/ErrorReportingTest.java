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
package org.apache.polygene.bootstrap;

import java.util.Map;
import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

public class ErrorReportingTest extends AbstractPolygeneTest
{
    private static final String NL = System.getProperty( "line.separator" );

    @Override
    public void assemble( ModuleAssembly module )
    {
        module.values( Person.class );
        module.values( Pet.class );
    }

    @Override
    protected void assemblyException( AssemblyException exception )
    {
        assertThat( exception.getMessage(), containsString( "Composition Problems Report:" + NL ) );
        assertThat( exception.getMessage(), containsString( "    message: No implementation found for method" + NL
                                                            + "    method: Map doAnotherThing(String name, int value)" + NL
                                                            + "    types: [Person,ValueComposite]" + NL ) );

        assertThat( exception.getMessage(), containsString( "    message: No implementation found for method" + NL
                                                            + "    method: void doOneThing()" + NL
                                                            + "    types: [Person,ValueComposite]" + NL ) );

        assertThat( exception.getMessage(), containsString( "    message: No implementation found for method" + NL
                                                            + "    method: void goForWalk(int minutes)" + NL
                                                            + "    types: [Pet,ValueComposite]" + NL ) );
    }

    @Test
    public void dummy()
    {

    }

    public interface Person
    {
        void doOneThing();

        Map<String, Integer> doAnotherThing( String name, int value );

        Property<String> name();

        Association<Person> spouse();

        ManyAssociation<Pet> pets();
    }

    public interface Pet
    {
        void goForWalk( int minutes );
    }
}
