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

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.qi4j.entity.EntityComposite;
import org.qi4j.query.Query;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.Uses;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.structure.Module;
import java.util.Iterator;
import java.util.ArrayList;

public class QueryMetaDataProvider
    implements IDataProvider
{
    private ArrayList names;

    public QueryMetaDataProvider( @Uses Class<EntityComposite> type, @Structure Module module, @Structure Qi4jSPI spi )
    {
        CompositeBinding binding = spi.getCompositeBinding( type, module );
        Iterable<PropertyBinding> properties = binding.getPropertyBindings();
        names = new ArrayList();
        for( PropertyBinding property : properties )
        {
            String name = property.getPropertyResolution().getPropertyModel().getName();
            names.add( name );
        }
    }

    public Iterator iterator( int first, int count )
    {
        return names.iterator();
    }

    public int size()
    {
        return names.size();
    }

    public IModel model( Object object )
    {
        return new Model( (String) object );
    }

    public void detach()
    {
    }
}
