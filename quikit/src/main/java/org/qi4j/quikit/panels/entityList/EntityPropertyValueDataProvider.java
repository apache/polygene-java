/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.quikit.panels.entityList;

import java.io.Serializable;
import static java.lang.Integer.MAX_VALUE;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.quikit.DisplayInfo;
import org.qi4j.quikit.application.QuikitSession;
import static org.qi4j.quikit.panels.entityList.EntityPropertyValueDataProvider.EntityFieldValue;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.api.structure.Module;

/**
 * @author edward.yakop@gmail.com
 * @since 0.2.0
 */
final class EntityPropertyValueDataProvider
    implements IDataProvider<EntityFieldValue>
{
    private static final long serialVersionUID = 1L;

    @Uses
    private IModel<EntityComposite> entityCompositeModel;

    @Uses
    private IModel<Class<EntityComposite>> entityClassModel;
    private Class<EntityComposite> calculatedEntityComposite;

    // TODO: Remove transient once spi is serializable
    @Structure
    private transient Qi4jSPI spi;

    // TODO: Remove transient once module is serializable
    @Structure
    private transient Module module;

    private final SortedSet<EntityFieldValue> entityFieldValues;

    public EntityPropertyValueDataProvider()
    {
        entityFieldValues = new TreeSet<EntityFieldValue>();
        calculatedEntityComposite = null;
    }

    public Iterator<EntityFieldValue> iterator( int first, int count )
    {
        SortedSet<EntityFieldValue> entityValues = getEntityValues();
        return entityValues.iterator();
    }

    private SortedSet<EntityFieldValue> getEntityValues()
    {
        Class<EntityComposite> currentEntityClass = entityClassModel.getObject();
        if( calculatedEntityComposite != currentEntityClass )
        {
            entityFieldValues.clear();

            // TODO: Remove once module is serializable
            QuikitSession quikitSession = QuikitSession.get();
            module = quikitSession.getModule();
            spi = quikitSession.getQi4jSpi();

            CompositeDescriptor compositeDescriptor = spi.getCompositeDescriptor( currentEntityClass, module );
            Iterable<PropertyDescriptor> propertyDescriptorIterable = compositeDescriptor.state().properties();
            for( PropertyDescriptor descriptor : propertyDescriptorIterable )
            {
                DisplayInfo displayInfo = descriptor.metaInfo( DisplayInfo.class );
                boolean isPropertyVisible = ( displayInfo == null ) || displayInfo.isVisible();
                if( isPropertyVisible )
                {
                    Method propertyMethodAccessor = descriptor.accessor();

                    int propertyOrder = ( displayInfo != null ) ? displayInfo.order() : MAX_VALUE;
                    EntityFieldValue fieldValue =
                        new EntityFieldValue( propertyOrder, entityCompositeModel, propertyMethodAccessor );
                    entityFieldValues.add( fieldValue );
                }
            }

            calculatedEntityComposite = currentEntityClass;
        }

        return entityFieldValues;
    }

    public final int size()
    {
        SortedSet<EntityFieldValue> entityValues = getEntityValues();
        return entityValues.size();
    }

    public final IModel<EntityFieldValue> model( EntityFieldValue aValue )
    {
        return new Model<EntityFieldValue>( aValue );
    }

    public void detach()
    {
        // Do nothing
    }

    public static class EntityFieldValue
        implements Serializable, Comparable<EntityFieldValue>
    {
        private static final long serialVersionUID = 1L;

        private final int propertyOrder;
        private final IModel<EntityComposite> entityCompositeModel;
        private final Method propertyAccessor;

        public EntityFieldValue(
            int aPropertyOrder,
            IModel<EntityComposite> anEntityCompositeModel,
            Method aPropertyAccessor )
        {
            propertyOrder = aPropertyOrder;
            entityCompositeModel = anEntityCompositeModel;
            propertyAccessor = aPropertyAccessor;
        }

        public Object getPropertyValue()
        {
            EntityComposite entityComposite = entityCompositeModel.getObject();
            if( entityComposite == null )
            {
                return null;
            }

            Object propertyValue = null;
            try
            {
                Property property = (Property) propertyAccessor.invoke( entityComposite );
                propertyValue = property.get();
            }
            catch( IllegalAccessException e )
            {
                // Can't happen right?
                e.printStackTrace();
            }
            catch( InvocationTargetException e )
            {
                // Can't happen right?
                e.printStackTrace();
            }

            return propertyValue;
        }

        @Override
        public final String toString()
        {
            Object propertyValue = getPropertyValue();
            if( propertyValue == null )
            {
                return null;
            }
            return propertyValue.toString();
        }

        public final int compareTo( EntityFieldValue another )
        {
            if( another == null )
            {
                return 1;
            }

            int relativeOrder = propertyOrder - another.propertyOrder;
            return ( relativeOrder == 0 ) ? -1 : relativeOrder;
        }

        @Override
        public final boolean equals( Object o )
        {
            if( this == o )
            {
                return true;
            }
            if( o == null || getClass() != o.getClass() )
            {
                return false;
            }

            EntityFieldValue that = (EntityFieldValue) o;

            if( propertyOrder != that.propertyOrder )
            {
                return false;
            }
            if( !entityCompositeModel.equals( that.entityCompositeModel ) )
            {
                return false;
            }
            if( !propertyAccessor.equals( that.propertyAccessor ) )
            {
                return false;
            }

            return true;
        }

        @Override
        public final int hashCode()
        {
            int result;
            result = propertyOrder;
            result = 31 * result + entityCompositeModel.hashCode();
            result = 31 * result + propertyAccessor.hashCode();
            return result;
        }
    }
}