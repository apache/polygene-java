/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.library.appbrowser;

import java.io.StringWriter;
import java.io.Writer;
import org.junit.Test;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.concern.ConcernOf;
import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.identity.HasIdentity;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.memory.MemoryEntityStoreService;
import org.apache.zest.library.appbrowser.json.JsonFormatterFactory;
import org.apache.zest.test.AbstractZestTest;

public class AppBrowserTest extends AbstractZestTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( Person.class );
        module.values( Age.class );
        module.services( MemoryEntityStoreService.class );
        module.importedServices( ValueSerialization.class );
    }

    @Test
    public void testBrowser()
        throws Exception
    {
        Writer output = new StringWriter();
        FormatterFactory jsonFactory = new JsonFormatterFactory( output );
        Browser browser = new Browser( applicationModel, jsonFactory );
        browser.toJson();
    }

    @Mixins( Person.Mixin.class )
    @Concerns( Person.AgeLimitConcern.class )
    public interface Person extends HasIdentity
    {
        String name();

        int yearsOld();

        interface State
        {
            Property<String> name();

            Property<Age> age();

            @Optional
            Association<Person> spouse();

            ManyAssociation<Person> children();
        }

        abstract class Mixin
            implements Person
        {

            @This
            private State state;

            @Override
            public String name()
            {
                return state.name().get();
            }

            @Override
            public int yearsOld()
            {
                return state.age().get().numberOfYearsOld();
            }
        }

        abstract class AgeLimitConcern extends ConcernOf<Person>
            implements Person
        {
            @This
            private Person me;
            @Service
            private AgeCheckService ageCheck;

            @Override
            public int yearsOld()
            {
                int years = next.yearsOld();
                if( ageCheck.checkAge( identity(), years ))
                    throw new DeathException( "Person is dead.");
                return 0;
            }
        }
    }

    @Mixins( Age.AgeMixin.class )
    public interface Age
    {
        Property<Integer> birthYear();

        int numberOfYearsOld();

        abstract class AgeMixin
            implements Age
        {

            @Override
            public int numberOfYearsOld()
            {
                return DateTime.now().getYearOfEra() - birthYear().get();
            }
        }
    }

    public static class DeathException extends RuntimeException
    {
        public DeathException( String message )
        {
            super( message );
        }
    }

    @Mixins(AgeCheckService.AgeCheckerMixin.class)
    public interface AgeCheckService
    {

        boolean checkAge( Property<Identity> identity, int years );

        class AgeCheckerMixin
            implements AgeCheckService
        {

            @Override
            public boolean checkAge( Property<Identity> identity, int years )
            {
                double probabiility = years/(Math.random()*120+1);
                return probabiility < 0.9;
            }
        }
    }
}
