/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.index.solr;

import java.util.ArrayList;
import java.util.List;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.fileconfig.FileConfiguration;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.hamcrest.CoreMatchers.*;

/**
 * JAVADOC
 */
public class SolrQueryServiceTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.layer().application().setMode( Application.Mode.test );

        module.services( FileConfiguration.class ).instantiateOnStartup();

        new EntityTestAssembler().assemble( module );
        new SolrAssembler().assemble( module );

        module.entities( TestEntity.class );
    }

    @Before
    public void index()
        throws UnitOfWorkCompletionException, InterruptedException
    {
        // Create and index an entity
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        TestEntity test = uow.newEntity( TestEntity.class );
        test.name().set( "Hello World" );
        uow.complete();
        Thread.sleep( 40 );
    }

    @Test
    public void testQuery()
        throws UnitOfWorkCompletionException
    {
        // Search for it
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        Query<TestEntity> query = queryBuilderFactory.newNamedQuery( TestEntity.class, uow, "search" );
        query.setVariable( "query", "hello" );

        TestEntity test = query.find();
        Assert.assertThat( test.name().get(), equalTo( "Hello World" ) );

        uow.discard();
    }

    @Test
    public void testSearch()
        throws UnitOfWorkCompletionException, SolrServerException
    {
        // Search for it using search interface
        SolrSearch search = (SolrSearch) serviceLocator.findService( SolrSearch.class ).get();

        SolrDocumentList results = search.search( "hello" );

        List<String> lookAhead = new ArrayList<String>();
        for( SolrDocument result : results )
        {
            lookAhead.add( result.getFieldValue( "name" ).toString() );
        }

        Assert.assertThat( lookAhead.toString(), equalTo( "[Hello World]" ) );
    }

    public interface TestEntity
        extends EntityComposite
    {
        @UseDefaults
        Property<String> name();
    }
}
