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
package org.apache.zest.index.solr;

import java.io.File;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.query.Query;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.fileconfig.FileConfigurationService;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.apache.zest.library.fileconfig.FileConfigurationOverride;
import org.apache.zest.test.util.DelTreeAfter;

import static org.hamcrest.CoreMatchers.equalTo;

public class SolrQueryServiceTest
    extends AbstractZestTest
{

    private static final File DATA_DIR = new File( "build/tmp/solr-query-service-test" );
    @Rule
    public final DelTreeAfter delTreeAfter = new DelTreeAfter( DATA_DIR );

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.layer().application().setMode( Application.Mode.test );

        FileConfigurationOverride override = new FileConfigurationOverride().withData( new File( DATA_DIR, "zest-data" ) ).
            withLog( new File( DATA_DIR, "zest-logs" ) ).withTemporary( new File( DATA_DIR, "zest-temp" ) );
        module.services( FileConfigurationService.class ).
            setMetaInfo( override );

        new EntityTestAssembler().assemble( module );
        // START SNIPPET: assembly
        new SolrAssembler().assemble( module );
        // END SNIPPET: assembly

        module.entities( TestEntity.class );
    }

    @Before
    public void index()
        throws UnitOfWorkCompletionException, InterruptedException
    {
        // Create and index an entity
        UnitOfWork uow = uowf.newUnitOfWork();
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
        UnitOfWork uow = uowf.newUnitOfWork();
        Query<TestEntity> query = uow.newQuery( queryBuilderFactory.newQueryBuilder( TestEntity.class ).where( SolrExpressions.search( "hello" ) ) );

        TestEntity test = query.find();
        Assert.assertThat( test.name().get(), equalTo( "Hello World" ) );

        uow.discard();
    }

    @Test
    public void testSearch()
        throws UnitOfWorkCompletionException, SolrServerException
    {
        // Search for it using search interface
        SolrSearch search = serviceFinder.findService( SolrSearch.class ).get();

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
