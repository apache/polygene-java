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

package org.apache.polygene.runtime.value;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.association.NamedAssociation;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.unitofwork.concern.UnitOfWorkConcern;
import org.apache.polygene.api.unitofwork.concern.UnitOfWorkPropagation;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.Test;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class AssociationToValueTest extends AbstractPolygeneTest
{
    @Service
    PersonRepository repo;

    @Test
    public void givenAdamWhenRequestingChildrenListExpectAbelAndKain()
    {
        Person adam = repo.findPersonByName( "Adam" );
        Person abel = repo.findPersonByName( "Abel" );
        Person kain = repo.findPersonByName( "Kain" );
        List<Person> children = repo.transact( adam, ( p, uow ) -> uow.toValueList( p.children() ) );
        assertThat( children, containsInAnyOrder( kain, abel ) );
    }

    @Test
    public void givenAbelWhenRequestingChildrenSetExpectAdamAndEve()
    {
        Person abel = repo.findPersonByName( "Abel" );
        Person adam = repo.findPersonByName( "Adam" );
        Person eve = repo.findPersonByName( "Eve" );
        Set<Person> children = repo.transact( abel, ( p, uow ) -> uow.toValueSet( p.children() ) );
        assertThat( children, containsInAnyOrder( adam, eve ) );
    }

    @Test
    public void givenBobWhenRequestingRolesExpectAllRolesWithCorrectPerson()
    {
        Person bob = repo.findPersonByName( "Bob" );
        Person alice = repo.findPersonByName( "Alice" );
        Person john = repo.findPersonByName( "John" );
        Person jane = repo.findPersonByName( "Jane" );
        Person kim = repo.findPersonByName( "Kim" );
        Person robin = repo.findPersonByName( "Robin" );
        Map<String, Person> roles = repo.transact( bob, ( p, uow ) -> uow.toValueMap( p.roles() ) );
        assertThat( roles.keySet(), containsInAnyOrder( "spouse", "mechanic", "maid", "plumber", "electrician" ) );
        assertThat( roles.values(), containsInAnyOrder( alice, john, jane, kim, robin ) );
    }

    @Test
    public void givenLouisWhenRequestingRolesExpectAllRolesOfMarie()
    {
        Person louis = repo.findPersonByName( "Louis" );
        Person marie = repo.findPersonByName( "Marie" );
        Map<String, Person> roles = repo.transact( louis, ( p, uow ) -> uow.toValueMap( p.roles() ) );
        assertThat( roles.keySet(), containsInAnyOrder( "spouse", "lover", "death-mate" ) );
        assertThat( roles.values(), containsInAnyOrder( marie, marie, marie ) );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( Person.class );
        module.values( Person.class );
        module.services( PersonRepository.class ).withConcerns( UnitOfWorkConcern.class );

        module.services( Runnable.class )
            .withMixins( LoadData.class )
            .withConcerns( UnitOfWorkConcern.class )
            .instantiateOnStartup();

        new EntityTestAssembler().assemble( module );
    }

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        serviceFinder.findService( Runnable.class ).get().run();
    }

    public interface Person extends HasIdentity
    {
        ManyAssociation<Person> children();

        NamedAssociation<Person> roles();
    }

    @Mixins( PersonRepositoryMixin.class )
    public interface PersonRepository
    {
        @UnitOfWorkPropagation
        <T, R> R transact( T arg, BiFunction<T, UnitOfWork, R> closure );

        @UnitOfWorkPropagation
        Person findPersonByName( String name );
    }

    protected static class PersonRepositoryMixin
        implements PersonRepository
    {
        @Structure
        UnitOfWorkFactory unitOfWorkFactory;

        @Override
        public <T, R> R transact( T arg, BiFunction<T, UnitOfWork, R> closure )
        {
            UnitOfWork uow = unitOfWorkFactory.currentUnitOfWork();
            return closure.apply( arg, uow );
        }

        @Override
        public Person findPersonByName( String name )
        {
            UnitOfWork uow = unitOfWorkFactory.currentUnitOfWork();
            return uow.toValue( Person.class, uow.get( Person.class, StringIdentity.fromString( name ) ) );
        }
    }

    public static class LoadData
        implements Runnable
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Override
        @UnitOfWorkPropagation
        public void run()
        {
            Person bob = createPerson( "Bob" );
            Person alice = createPerson( "Alice" );
            Person john = createPerson( "John" );
            Person jane = createPerson( "Jane" );
            Person kim = createPerson( "Kim" );
            Person robin = createPerson( "Robin" );
            Person william = createPerson( "William" );
            Person adam = createPerson( "Adam" );
            Person eve = createPerson( "Eve" );
            Person abel = createPerson( "Abel" );
            Person kain = createPerson( "Kain" );
            Person louis = createPerson( "Louis" );
            Person marie = createPerson( "Marie" );
            Person romeo = createPerson( "Romeo" );
            Person juliette = createPerson( "Juliette" );
            adam.children().add( abel );
            adam.children().add( kain );
            eve.children().add( abel );
            eve.children().add( kain );
            abel.children().add( adam );
            abel.children().add( eve );
            abel.children().add( adam );
            abel.children().add( eve );
            abel.children().add( eve );
            bob.roles().put( "spouse", alice );
            bob.roles().put( "mechanic", john );
            bob.roles().put( "maid", jane );
            bob.roles().put( "plumber", kim );
            bob.roles().put( "electrician", robin );
            louis.roles().put( "spouse", marie );
            louis.roles().put( "lover", marie );
            louis.roles().put( "death-mate", marie );
            juliette.roles().put( "lover", romeo );
            juliette.roles().put( "author", william );
            romeo.roles().put( "lover", juliette );
            romeo.roles().put( "author", william );
        }

        private Person createPerson( String name )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            return uow.newEntity( Person.class, StringIdentity.fromString( name ) );
        }
    }
}
