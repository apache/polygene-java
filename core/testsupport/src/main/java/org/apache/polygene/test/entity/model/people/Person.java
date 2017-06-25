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
package org.apache.polygene.test.entity.model.people;

import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.association.NamedAssociation;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.entity.Aggregated;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.unitofwork.concern.UnitOfWorkPropagation;

@Mixins( Person.Mixin.class )
public interface Person extends HasIdentity
{
    @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.MANDATORY )
    void movedToNewAddress( String street, String zipCode, City city, Country country, Rent rent );

    @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.MANDATORY )
    void amendAddress( String street, String zipCode, City city, Country country );

    Property<String> name();

    Association<Country> nationality();

    @Aggregated
    Association<Address> address();

    @Optional
    Association<Person> spouse();

    ManyAssociation<Person> children();

    @Aggregated
    ManyAssociation<Address> oldAddresses();

    NamedAssociation<Person> relationships();

    @Aggregated
    NamedAssociation<PhoneNumber> phoneNumbers();

    abstract class Mixin
        implements Person
    {
        @Structure
        private UnitOfWorkFactory uowf;

        @Service
        private PeopleRepository repository;

        @Override
        public void movedToNewAddress( String street, String zipCode, City city, Country country, Rent rent )
        {
            Address newAddress = repository.createAddress( street, zipCode, city, country, rent );
            Address oldAddress = address().get();
            oldAddresses().add( oldAddress );
            address().set( newAddress );
        }

        @Override
        public void amendAddress( String street, String zipCode, City city, Country country )
        {
            Address newAddress = repository.createAddress( street, zipCode, city, country, address().get().rent().get() );
            address().set( newAddress );
        }
    }
}
