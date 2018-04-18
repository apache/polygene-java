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
package org.apache.polygene.index.elasticsearch;

import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.entity.Aggregated;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.query.Query;
import org.apache.polygene.api.query.QueryBuilder;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.index.elasticsearch.assembly.ESClientIndexQueryAssembler;
import org.apache.polygene.library.fileconfig.FileConfigurationAssembler;
import org.apache.polygene.library.fileconfig.FileConfigurationOverride;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.TemporaryFolder;
import org.apache.polygene.test.TestName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.apache.polygene.api.query.QueryExpressions.eq;
import static org.apache.polygene.api.query.QueryExpressions.ne;
import static org.apache.polygene.api.query.QueryExpressions.not;
import static org.apache.polygene.api.query.QueryExpressions.templateFor;
import static org.apache.polygene.test.util.Assume.assumeNoIbmJdk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

@ExtendWith( { TemporaryFolder.class, EmbeddedElasticSearchExtension.class, TestName.class } )
public class ElasticSearchTest
    extends AbstractPolygeneTest
{
    @BeforeAll
    public static void beforeClass_IBMJDK()
    {
        assumeNoIbmJdk();
    }


    public static EmbeddedElasticSearchExtension ELASTIC_SEARCH;

    public TestName testName;

    private TemporaryFolder tmpDir;

    public interface Post
        extends HasIdentity
    {
        Property<String> title();

        @UseDefaults
        Property<String> body();

        Property<Tagline> tagline();

        Association<Author> author();

        @Aggregated
        @UseDefaults
        ManyAssociation<Comment> comments();
    }

    public interface Page
        extends HasIdentity
    {
        Property<String> title();

        @UseDefaults
        Property<String> body();

        Property<Tagline> tagline();

        Association<Author> author();
    }

    public interface Tagline
    {
        @UseDefaults
        Property<String> tags();
    }

    public interface Author
        extends HasIdentity
    {
        Property<String> nickname();
    }

    public interface Comment
        extends HasIdentity
    {
        Property<String> content();
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // Config module
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );

        // EntityStore
        new EntityTestAssembler().assemble( module );

        // Index/Query
        new ESClientIndexQueryAssembler( ELASTIC_SEARCH.client() )
            .withConfig( config, Visibility.layer )
            .assemble( module );
        ElasticSearchIndexingConfiguration esConfig = config.forMixin( ElasticSearchIndexingConfiguration.class ).declareDefaults();
        esConfig.index().set( ELASTIC_SEARCH.indexName( ElasticSearchQueryTest.class.getName(), testName.getMethodName() ) );
        esConfig.indexNonAggregatedAssociations().set( Boolean.TRUE );

        // FileConfig
        new FileConfigurationAssembler()
            .withOverride( new FileConfigurationOverride().withConventionalRoot( tmpDir.getRoot() ) )
            .assemble( module );

        // Entities & Values
        module.entities( Post.class, Page.class, Author.class, Comment.class );
        module.values( Tagline.class );
    }

    @Test
    public void test()
        throws UnitOfWorkCompletionException
    {
        String title = "Foo Bar Bazar!";

        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

        EntityBuilder<Author> authorBuilder = uow.newEntityBuilder( Author.class );
        Author author = authorBuilder.instance();
        author.nickname().set( "eskatos" );
        author = authorBuilder.newInstance();

        EntityBuilder<Comment> commentBuilder = uow.newEntityBuilder( Comment.class );
        Comment comment1 = commentBuilder.instance();
        comment1.content().set( "Comment One" );
        comment1 = commentBuilder.newInstance();

        commentBuilder = uow.newEntityBuilder( Comment.class );
        Comment comment2 = commentBuilder.instance();
        comment2.content().set( "Comment Two" );
        comment2 = commentBuilder.newInstance();

        EntityBuilder<Post> postBuilder = uow.newEntityBuilder( Post.class );
        Post post = postBuilder.instance();
        post.title().set( title );
        post.author().set( author );
        post.tagline().set( valueBuilderFactory.newValue( Tagline.class ) );
        post.comments().add( comment1 );
        post.comments().add( comment2 );
        post = postBuilder.newInstance();

        EntityBuilder<Page> pageBuilder = uow.newEntityBuilder( Page.class );
        Page page = pageBuilder.instance();
        page.title().set( title );
        page.author().set( author );
        page.tagline().set( valueBuilderFactory.newValue( Tagline.class ) );
        page = pageBuilder.newInstance();

        System.out.println( "########################################" );
        System.out.println( "Post Identity: " + post.identity().get() );
        System.out.println( "Page Identity: " + page.identity().get() );
        System.out.println( "########################################" );

        uow.complete();

        uow = unitOfWorkFactory.newUnitOfWork();

        QueryBuilder<Post> queryBuilder = queryBuilderFactory.newQueryBuilder( Post.class );
        Query<Post> query = uow.newQuery( queryBuilder );
        assertThat( query.count(), equalTo( 1L ) );
        post = query.find();
        assertThat( post, notNullValue() );
        assertThat( post.title().get(), equalTo( title ) );

        post = templateFor( Post.class );
        queryBuilder = queryBuilderFactory.newQueryBuilder( Post.class ).where( eq( post.title(), title ) );
        query = uow.newQuery( queryBuilder );
        assertThat( query.count(), equalTo( 1L ) );
        post = query.find();
        assertThat( post, notNullValue() );
        assertThat( post.title().get(), equalTo( title ) );

        post = templateFor( Post.class );
        queryBuilder = queryBuilderFactory.newQueryBuilder( Post.class )
                                          .where( eq( post.title(), "Not available" ) );
        query = uow.newQuery( queryBuilder );
        assertThat( query.count(), equalTo( 0L ) );

        post = templateFor( Post.class );
        queryBuilder = queryBuilderFactory.newQueryBuilder( Post.class )
                                          .where( ne( post.title(), "Not available" ) );
        query = uow.newQuery( queryBuilder );
        assertThat( query.count(), equalTo( 1L ) );

        post = templateFor( Post.class );
        queryBuilder = queryBuilderFactory.newQueryBuilder( Post.class )
                                          .where( not( eq( post.title(), "Not available" ) ) );
        query = uow.newQuery( queryBuilder );
        post = query.find();
        assertThat( post, notNullValue() );
        assertThat( post.title().get(), equalTo( title ) );

        post = templateFor( Post.class );
        queryBuilder = queryBuilderFactory.newQueryBuilder( Post.class )
                                          .where( eq( post.author().get().nickname(), "eskatos" ) );
        query = uow.newQuery( queryBuilder );
        assertThat( query.count(), equalTo( 1L ) );
        post = query.find();
        assertThat( post, notNullValue() );
        assertThat( post.title().get(), equalTo( title ) );

        uow.discard();
    }
}
