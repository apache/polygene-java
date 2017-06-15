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

package org.apache.polygene.envisage.school.domain.person.initialdata;

import org.apache.polygene.api.activation.ActivatorAdapter;
import org.apache.polygene.api.activation.Activators;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.envisage.school.domain.person.Person;
import org.apache.polygene.envisage.school.domain.person.assembly.PersonEntity;

@Mixins( SamplePersonInitialData.SamplePersonBootstrapMixin.class )
@Activators( SamplePersonInitialData.Activator.class )
public interface SamplePersonInitialData
{
    String EDWARD = "edward";
    String NICLAS = "niclas";
    String RICKARD = "rickard";

    void insertInitialData()
        throws Exception;

    class Activator
        extends ActivatorAdapter<ServiceReference<SamplePersonInitialData>>
    {

        @Override
        public void afterActivation( ServiceReference<SamplePersonInitialData> activated )
            throws Exception
        {
            activated.get().insertInitialData();
        }

    }

    abstract class SamplePersonBootstrapMixin
        implements SamplePersonInitialData
    {
        private static final String[][] DATAS =
        {
            {
                EDWARD, "Edward", "Yakop"
            },
            {
                NICLAS, "Niclas", "Hedhman"
            },
            {
                RICKARD, "Rickard", "Öberg"
            }
        };

        @Structure
        private UnitOfWorkFactory uowf;

        @Override
        public void insertInitialData()
            throws Exception
        {
            UnitOfWork uow = uowf.currentUnitOfWork();

            for( String[] data : DATAS )
            {
                String personId = data[ 0];
                String firstName = data[ 1];
                String lastName = data[ 2];
                createPerson( uow, personId, firstName, lastName );
            }

            uow.complete();
        }

        private void createPerson( UnitOfWork uow, String personId, String firstName, String lastName )
        {
            EntityBuilder<Person> person = uow.newEntityBuilder( Person.class, StringIdentity.identity( personId ) );

            PersonEntity.PersonState state = person.instanceFor( PersonEntity.PersonState.class );
            state.firstName().set( firstName );
            state.lastName().set( lastName );

            person.newInstance();
        }

    }

}
