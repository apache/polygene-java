/*
 * Copyright 2009 Niclas Hedhman.
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

package org.qi4j.index.rdf;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.qi4j.spi.util.CollectionUtils;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;

public class QueryOfPropertySubTypeTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( FlatEntity.class, WoupsEntity.class );
        module.services( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class );
        new RdfMemoryStoreAssembler().assemble( module );
    }

    interface FlatEntity
        extends EntityComposite
    {
        Property<String> name();
    }

    interface WoupsEntity
        extends EntityComposite
    {
        Name name();
    }

    interface Name
        extends Property<String>
    {
    }

    @Test
    public void givenAnEntityWithSimplePropertyWhenQueriedOnPropertyThenJustWork()
        throws UnitOfWorkCompletionException
    {
        FlatEntity test;
        {
            UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
            EntityBuilder<FlatEntity> builder = uow.newEntityBuilder( FlatEntity.class );
            test = builder.instance();
            test.name().set( "Bob" );
            test = builder.newInstance();
            uow.complete();
        }
        {
            UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

            QueryBuilder<FlatEntity> queryBuilder = queryBuilderFactory.newQueryBuilder( FlatEntity.class );
            FlatEntity thingTemplate = QueryExpressions.templateFor( FlatEntity.class );
            queryBuilder.where( QueryExpressions.eq( thingTemplate.name(), "Bob" ) );

            Query<FlatEntity> query = queryBuilder.newQuery( uow );
            query.maxResults( 1 );

            FlatEntity foundByName = (FlatEntity) CollectionUtils.firstElementOrNull( query );
            Assert.assertEquals( "Bob", foundByName.name().get() );

            uow.complete();
        }
    }

    @Ignore( "// FIXME : This one do not work." )
    @Test
    public void givenAnEntityWithSubtypedPropertyWhenQueriedOnPropertyThenJustWork()
        throws UnitOfWorkCompletionException
    {
        WoupsEntity test;
        {
            UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
            EntityBuilder<WoupsEntity> builder = uow.newEntityBuilder( WoupsEntity.class );
            test = builder.instance();
            test.name().set( "Bob" );
            test = builder.newInstance();
            uow.complete();
        }
        {
            UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

            QueryBuilder<WoupsEntity> queryBuilder = queryBuilderFactory.newQueryBuilder( WoupsEntity.class );
            WoupsEntity thingTemplate = QueryExpressions.templateFor( WoupsEntity.class );
            queryBuilder.where( QueryExpressions.eq( thingTemplate.name(), "Bob" ) );

            Query<WoupsEntity> query = queryBuilder.newQuery( uow );
            query.maxResults( 1 );

            WoupsEntity foundByName = (WoupsEntity) CollectionUtils.firstElementOrNull( query );
            Assert.assertEquals( "Bob", foundByName.name().get() );

            uow.complete();
        }
    }
}
