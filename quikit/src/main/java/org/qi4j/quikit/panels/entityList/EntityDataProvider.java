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
package org.qi4j.quikit.panels.entityList;

import java.util.Iterator;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.quikit.application.QuikitSession;

final class EntityDataProvider
    implements IDataProvider<EntityComposite>
{
    private static final long serialVersionUID = 1L;

    @Uses
    private IModel<Class<EntityComposite>> entityCompositeClass;
    private Class<EntityComposite> computedEntityCompositeClass;

    // TODO: Remove transient once unit of work factory is serializable
    @Structure
    private transient UnitOfWorkFactory uowf;

    private transient Query<EntityComposite> query;

    public EntityDataProvider()
    {
        computedEntityCompositeClass = null;
        query = null;
    }

    public final Iterator<EntityComposite> iterator( int first, int count )
    {
        Query<EntityComposite> entityCompositeQuery = getQuery();
        entityCompositeQuery.firstResult( first );
        entityCompositeQuery.maxResults( count );
        return entityCompositeQuery.iterator();
    }

    private Query<EntityComposite> getQuery()
    {
        Class<EntityComposite> currentEntityCompositeClass = entityCompositeClass.getObject();
        if( computedEntityCompositeClass != currentEntityCompositeClass || query == null )
        {
            // TODO: Remove the next line once unit of work factory is serializable
            QuikitSession quikitSession = QuikitSession.get();
            uowf = quikitSession.getUnitOfWorkFactory();

            UnitOfWork currentUnitOfWork = uowf.currentUnitOfWork();
            QueryBuilderFactory builderFactory = currentUnitOfWork.queryBuilderFactory();
            QueryBuilder<EntityComposite> builder = builderFactory.newQueryBuilder( currentEntityCompositeClass );
            query = builder.newQuery();
        }
        return query;
    }

    public final int size()
    {
        Query<EntityComposite> entityCompositeQuery = getQuery();
        return (int) entityCompositeQuery.count();
    }

    public final IModel<EntityComposite> model( EntityComposite object )
    {
        return new Model<EntityComposite>( object );
    }

    public final void detach()
    {
        computedEntityCompositeClass = null;
        query = null;
    }
}
