/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.qi4j.entity.index.rdf;

import org.junit.Before;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entity.UnitOfWorkCompletionException;
import static org.qi4j.query.QueryExpressions.*;
import org.qi4j.spi.entity.UuidIdentityGeneratorComposite;
import org.qi4j.spi.query.SearchEngine;
import org.qi4j.spi.query.SearchException;

public class SesameQueryTest
{
    private SingletonAssembler assembler;
    private SearchEngine searchEngine;

    @Before
    public void setUp() throws UnitOfWorkCompletionException
    {
        assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addComposites(
                    PersonComposite.class,
                    CityComposite.class,
                    DomainComposite.class,
                    CatComposite.class
                );
                module.addServices(
                    IndexedMemoryEntityStoreComposite.class,
                    UuidIdentityGeneratorComposite.class,
                    RDFIndexerExporterComposite.class
                );
            }
        };
        Network.populate( assembler.getUnitOfWorkFactory().newUnitOfWork() );
        searchEngine = assembler.getServiceLocator().lookupService( RDFIndexerComposite.class ).getService();
    }

    @Test
    public void showNetwork()
    {
        assembler.getServiceLocator().lookupService( RDFIndexerExporterComposite.class ).getService().toRDF( System.out );
    }

    @Test
    public void script01() throws SearchException
    {
        // should return all persons (Joe, Ann, Jack Doe)
        searchEngine.find(
            PersonComposite.class,
            null // all
        );
    }

    @Test
    public void script02() throws SearchException
    {
        Nameable nameable = templateFor( Nameable.class );
        // should return Gaming domain
        searchEngine.find(
            Domain.class,
            eq( nameable.name(), "Gaming" )
        );
    }

    @Test
    public void script03() throws SearchException
    {
        // should return all entities
        searchEngine.find(
            Nameable.class,
            null // all
        );
    }

    @Test
    public void script04() throws SearchException
    {
        Person person = templateFor( Person.class );
        // should return Joe and Ann Doe
        searchEngine.find(
            Person.class,
            eq( person.placeOfBirth().get().name(), "Kuala Lumpur" )
        );
    }

    @Test
    public void script05() throws SearchException
    {
        Person person = templateFor( Person.class );
        // should return Joe Doe
        searchEngine.find(
            Person.class,
            eq( person.mother().get().placeOfBirth().get().name(), "Kuala Lumpur" )
        );
    }

    @Test
    public void script06() throws SearchException
    {
        Person person = templateFor( Person.class );
        // should return Joe and Ann Doe
        searchEngine.find(
            Person.class,
            ge( person.yearOfBirth(), 1973 )
        );
    }

    @Test
    public void script07() throws SearchException
    {
        Person person = templateFor( Person.class );
        // should return Jack Doe
        searchEngine.find(
            Nameable.class,
            and(
                ge( person.yearOfBirth(), 1900 ),
                eq( person.placeOfBirth().get().name(), "Penang" )
            )
        );
    }

}