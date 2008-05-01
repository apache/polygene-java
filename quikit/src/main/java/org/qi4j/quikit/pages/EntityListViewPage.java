/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.quikit.pages;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.PageParameters;
import org.qi4j.composite.ObjectBuilder;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.Uses;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.ConcurrentEntityModificationException;
import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilder;
import org.qi4j.query.QueryBuilderFactory;
import org.qi4j.quikit.panels.EntityListViewPanel;
import org.qi4j.structure.Module;

public class EntityListViewPage extends WebPage
{
    public EntityListViewPage( @Structure ObjectBuilderFactory objectBuilderFactory,
                               @Structure UnitOfWorkFactory uowFactory,
                               @Structure Module module,
                               @Uses PageParameters parameters )
        throws UnitOfWorkCompletionException, ClassNotFoundException
    {
        UnitOfWork uow = uowFactory.newUnitOfWork();
        try
        {
            QueryBuilderFactory queryFactory = uow.queryBuilderFactory();
            Class entityType = module.lookupClass( parameters.getString( "entityType" ) );
            QueryBuilder<?> queryBuilder = queryFactory.newQueryBuilder( entityType );
            Query<?> query = queryBuilder.newQuery();
            IModel model = new Model( query );

            ObjectBuilder<EntityListViewPanel> builder = objectBuilderFactory.newObjectBuilder( EntityListViewPanel.class );
            builder.use( "view-panel" );
            builder.use( model );
            EntityListViewPanel panel = builder.newInstance();
            add( panel );
            uow.complete();
        }
        catch( RuntimeException e )
        {
            uow.discard();
            throw e;
        }
        catch( ConcurrentEntityModificationException e )
        {
            uow.discard();
            throw e;
        }
        catch( UnitOfWorkCompletionException e )
        {
            uow.discard();
            throw e;
        }
        catch( ClassNotFoundException e )
        {
            uow.discard();
            throw e;
        }
    }
}
