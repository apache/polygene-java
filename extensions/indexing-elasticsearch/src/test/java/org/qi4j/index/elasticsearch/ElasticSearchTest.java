/*
 * Copyright 2012 Paul Merlin.
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
package org.qi4j.index.elasticsearch;

import java.io.File;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.elasticsearch.assembly.ESFilesystemIndexQueryAssembler;
import org.qi4j.index.elasticsearch.extensions.spatial.configuration.SpatialConfiguration;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.util.DelTreeAfter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.ne;
import static org.qi4j.api.query.QueryExpressions.not;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static org.qi4j.test.util.Assume.assumeNoIbmJdk;

public class ElasticSearchTest
    extends AbstractQi4jTest
{

    private static final File DATA_DIR = new File( "build/tmp/es-test" );
    @Rule
    public final DelTreeAfter delTreeAfter = new DelTreeAfter( DATA_DIR );

    @BeforeClass
    public static void beforeClass_IBMJDK()
    {
        assumeNoIbmJdk();
    }

    public interface Post
        extends Identity
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
        extends Identity
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
        extends Identity
    {

        Property<String> nickname();

    }

    public interface Comment
        extends Identity
    {

        Property<String> content();

    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // Config module
        ModuleAssembly config = module.layer().module( "config" );
        config.values(SpatialConfiguration.Configuration.class,
                SpatialConfiguration.FinderConfiguration.class,
                SpatialConfiguration.IndexerConfiguration.class,
                SpatialConfiguration.IndexingMethod.class,
                SpatialConfiguration.ProjectionSupport.class).
                visibleIn(Visibility.application);
        new EntityTestAssembler().assemble( config );

        // EntityStore
        new EntityTestAssembler().assemble( module );

        // Index/Query
        new ESFilesystemIndexQueryAssembler().
            withConfig( config, Visibility.layer ).
            assemble( module );
        ElasticSearchConfiguration esConfig = config.forMixin( ElasticSearchConfiguration.class ).declareDefaults();
        esConfig.indexNonAggregatedAssociations().set( Boolean.TRUE );

        // FileConfig
        FileConfigurationOverride override = new FileConfigurationOverride().
            withData( new File( DATA_DIR, "qi4j-data" ) ).
            withLog( new File( DATA_DIR, "qi4j-logs" ) ).
            withTemporary( new File( DATA_DIR, "qi4j-temp" ) );
        module.services( FileConfigurationService.class ).
            setMetaInfo( override );

        // Entities & Values
        module.entities( Post.class, Page.class, Author.class, Comment.class );
        module.values( Tagline.class );
    }

    @Test
    public void test()
        throws UnitOfWorkCompletionException
    {
        String title = "Foo Bar Bazar!";

        UnitOfWork uow = module.newUnitOfWork();

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
        post.tagline().set( module.newValue( Tagline.class ) );
        post.comments().add( comment1 );
        post.comments().add( comment2 );
        post = postBuilder.newInstance();

        EntityBuilder<Page> pageBuilder = uow.newEntityBuilder( Page.class );
        Page page = pageBuilder.instance();
        page.title().set( title );
        page.author().set( author );
        page.tagline().set( module.newValue( Tagline.class ) );
        page = pageBuilder.newInstance();

        System.out.println( "########################################" );
        System.out.println( "Post Identity: " + post.identity().get() );
        System.out.println( "Page Identity: " + page.identity().get() );
        System.out.println( "########################################" );

        uow.complete();

        uow = module.newUnitOfWork();

        QueryBuilder<Post> queryBuilder = module.newQueryBuilder( Post.class );
        Query<Post> query = uow.newQuery( queryBuilder );
        assertEquals( 1, query.count() );
        post = query.find();
        assertNotNull( post );
        assertEquals( title, post.title().get() );

        post = templateFor( Post.class );
        queryBuilder = module.newQueryBuilder( Post.class ).where( eq( post.title(), title ) );
        query = uow.newQuery( queryBuilder );
        assertEquals( 1, query.count() );
        post = query.find();
        assertNotNull( post );
        assertEquals( title, post.title().get() );

        post = templateFor( Post.class );
        queryBuilder = module.newQueryBuilder( Post.class ).where( eq( post.title(), "Not available" ) );
        query = uow.newQuery( queryBuilder );
        assertEquals( 0, query.count() );

        post = templateFor( Post.class );
        queryBuilder = module.newQueryBuilder( Post.class ).where( ne( post.title(), "Not available" ) );
        query = uow.newQuery( queryBuilder );
        assertEquals( 1, query.count() );

        post = templateFor( Post.class );
        queryBuilder = module.newQueryBuilder( Post.class ).where( not( eq( post.title(), "Not available" ) ) );
        query = uow.newQuery( queryBuilder );
        post = query.find();
        assertNotNull( post );
        assertEquals( title, post.title().get() );

        post = templateFor( Post.class );
        queryBuilder = module.newQueryBuilder( Post.class ).where( eq( post.author().get().nickname(), "eskatos" ) );
        query = uow.newQuery( queryBuilder );
        assertEquals( 1, query.count() );
        post = query.find();
        assertNotNull( post );
        assertEquals( title, post.title().get() );

        uow.discard();
    }

}
