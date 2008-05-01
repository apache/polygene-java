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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.qi4j.composite.scope.Structure;
import org.qi4j.entity.EntityComposite;
import org.qi4j.property.Property;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.property.PropertyBinding;

public class EntityDataProvider
    implements IDataProvider
{
    @Structure private Qi4jSPI spi;
    private EntityComposite entity;
    private SortedMap<Integer, EntityField> infos;

    // Is private to ensure that it is only used from the ObjectBuilderFactory
    private EntityDataProvider( EntityComposite entity )
    {
        CompositeBinding compositeBinding = spi.getCompositeBinding( entity );
        this.infos = new TreeMap<Integer, EntityField>();
        this.entity = entity;
        Iterable<PropertyBinding> models = compositeBinding.getPropertyBindings();
        for( PropertyBinding binding : models )
        {
            DisplayInfo info = binding.metaInfo( DisplayInfo.class );
            if( info.isVisible() )
            {
                Integer order = info.order();
                Method method = binding.getPropertyResolution().getPropertyModel().getAccessor();
                Serializable value = ( (Property<Serializable>) invoke( method ) ).get();
                EntityField field = new EntityField( value, info );
                infos.put( order, field );
            }
        }
    }

    public Iterator iterator( int first, int count )
    {
        return infos.values().iterator();
    }

    public int size()
    {
        return infos.size();
    }

    public IModel model( Object object )
    {
        return new Model( (EntityField) object );
    }

    private Object invoke( Method method )
    {
        try
        {
            method.setAccessible( true );
            return method.invoke( entity );
        }
        catch( IllegalAccessException e )
        {
            // TODO: This can't happen, right?
            e.printStackTrace();
        }
        catch( InvocationTargetException e )
        {
            // TODO: This can't happen, right?
            e.printStackTrace();
        }
        return null;
    }

    public void detach()
    {
    }
}
