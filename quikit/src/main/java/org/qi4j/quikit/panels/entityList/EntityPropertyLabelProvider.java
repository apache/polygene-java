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

import java.io.Serializable;
import static java.lang.Integer.MAX_VALUE;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.Uses;
import org.qi4j.quikit.DisplayInfo;
import org.qi4j.quikit.application.QuikitSession;
import static org.qi4j.quikit.panels.entityList.EntityPropertyLabelProvider.PropertyLabel;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.structure.Module;

final class EntityPropertyLabelProvider
    implements IDataProvider<PropertyLabel>
{
    private static final long serialVersionUID = 1L;

    @Structure
    private transient Module module;

    @Structure
    private transient Qi4jSPI spi;

    private final SortedSet<PropertyLabel> propertyLabels;

    private final IModel<Class> entityCompositeClassModel;
    private Class computedEntityCompositeClass;

    public EntityPropertyLabelProvider( @Uses IModel<Class> aCompositeClassModel )
    {
        entityCompositeClassModel = aCompositeClassModel;
        computedEntityCompositeClass = null;
        propertyLabels = new TreeSet<PropertyLabel>();
    }

    /**
     * Returns the property names.
     *
     * @return The property names.
     * @since 0.2.0
     */
    @SuppressWarnings( "unchecked" )
    private SortedSet<PropertyLabel> getPropertyLabels()
    {
        Class currentEntityCompositeClassModel = entityCompositeClassModel.getObject();
        if( currentEntityCompositeClassModel != computedEntityCompositeClass )
        {
            propertyLabels.clear();

            // TODO: Remove once module and spi are serializable
            QuikitSession quikitSession = QuikitSession.get();
            module = quikitSession.getModule();
            spi = quikitSession.getQi4jSpi();

            CompositeDescriptor compositeDescriptor = spi.getCompositeDescriptor( currentEntityCompositeClassModel, module );
            Iterable<PropertyDescriptor> propertyDescriptorIterable = compositeDescriptor.state().properties();
            for( PropertyDescriptor propertyDescriptor : propertyDescriptorIterable )
            {
                DisplayInfo displayInfo = propertyDescriptor.metaInfo( DisplayInfo.class );
                boolean isPropertyVisible = displayInfo == null || displayInfo.isVisible();
                if( isPropertyVisible )
                {
                    String propertyLabel = ( displayInfo != null ) ? displayInfo.getLabel() : null;
                    if( propertyLabel == null )
                    {
                        propertyLabel = propertyDescriptor.name();
                    }
                    int order = ( displayInfo != null ) ? displayInfo.order() : MAX_VALUE;
                    propertyLabels.add( new PropertyLabel( order, propertyLabel ) );
                }
            }

            computedEntityCompositeClass = currentEntityCompositeClassModel;
        }

        return propertyLabels;
    }

    public final Iterator<PropertyLabel> iterator( int first, int count )
    {
        SortedSet<PropertyLabel> propertyLabels = getPropertyLabels();
        return propertyLabels.iterator();
    }

    public final int size()
    {
        SortedSet<PropertyLabel> propertyLabels = getPropertyLabels();
        return propertyLabels.size();
    }

    public final IModel<PropertyLabel> model( PropertyLabel aLabel )
    {
        return new Model<PropertyLabel>( aLabel );
    }

    public final void detach()
    {
        // Do nothing
    }

    public static class PropertyLabel
        implements Comparable<PropertyLabel>, Serializable
    {
        private static final long serialVersionUID = 1L;

        private final int propertyOrder;
        private final String propertyLabel;

        private PropertyLabel( int aPropertyOrder, String aPropertyLabel )
        {
            propertyOrder = aPropertyOrder;
            propertyLabel = aPropertyLabel;
        }

        @Override
        public String toString()
        {
            return propertyLabel;
        }

        public final int compareTo( PropertyLabel another )
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

            PropertyLabel that = (PropertyLabel) o;

            if( propertyOrder != that.propertyOrder )
            {
                return false;
            }
            if( !propertyLabel.equals( that.propertyLabel ) )
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
            result = 31 * result + propertyLabel.hashCode();
            return result;
        }
    }
}
