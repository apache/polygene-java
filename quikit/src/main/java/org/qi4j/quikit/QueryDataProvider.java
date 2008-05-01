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
package org.qi4j.quikit;

import java.util.Iterator;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.qi4j.entity.EntityComposite;
import org.qi4j.query.Query;

public class QueryDataProvider
    implements IDataProvider
{
    private Query<EntityComposite> query;

    public QueryDataProvider( Query<EntityComposite> query )
    {
        this.query = query;
    }

    public Iterator iterator( int first, int count )
    {
        query.firstResult( first );
        query.maxResults( count );
        return query.iterator();
    }

    public int size()
    {
        // return query.size();
        return 100;
    }

    public IModel model( Object object )
    {
        if( object instanceof EntityComposite )
        {
            return new Model( (EntityComposite) object );
        }
        throw new InternalError( "Only EntityComposite instances should be passed as argument here: " + object.getClass().getName() );
    }

    public void detach()
    {
    }
}
