/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.library.swing.binding.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import javax.swing.event.TableModelListener;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.entity.association.ListAssociation;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;

public class ManyAssociationTableModelMixin
    implements ManyAssociationTableModel
{
    private ArrayList<TableModelListener> listeners;
    private ManyAssociation association;
    private Qi4jSPI spi;
    private ArrayList<PropertyDescriptor> columns;
    private boolean isList;

    public ManyAssociationTableModelMixin( @Uses ManyAssociation association,
                                           @Optional @Uses Comparator<PropertyDescriptor> comparator,
                                           @Structure Module module,
                                           @Structure Qi4jSPI spi )
    {
        this.association = association;
        this.spi = spi;
        isList = association instanceof ListAssociation;
        EntityDescriptor descriptor = spi.getEntityDescriptor( (Class) association.type(), module );
        List<? extends PropertyDescriptor> properties = descriptor.state().properties();
        columns = new ArrayList<PropertyDescriptor>();
        for( PropertyDescriptor propertyType : properties )
        {
            columns.add( propertyType );
        }
        if( comparator != null )
        {
            TreeSet<PropertyDescriptor> sorted = new TreeSet<PropertyDescriptor>( comparator );
            sorted.addAll( columns );
            columns.clear();
            columns.addAll( sorted );
        }
    }


    public int getRowCount()
    {
        return association.size();
    }

    public int getColumnCount()
    {
        return columns.size();
    }

    public String getColumnName( int index )
    {
        LocalizedDisplayInfo info = columns.get( index ).metaInfo( LocalizedDisplayInfo.class );
        return info.caption();
    }

    public Class<?> getColumnClass( int index )
    {
        PropertyDescriptor propertyDescriptor = columns.get( index );
        return (Class<?>) propertyDescriptor.type();
    }

    public boolean isCellEditable( int i, int i1 )
    {
        return false;
    }

    public Object getValueAt( int row, int column )
    {
        EntityComposite entity = getEntity( row, column );
        if( entity != null )
        {
            Property<?> property = getProperty( column, entity );
            return property.get();
        }
        else
        {
            return null;
        }
    }

    public void setValueAt( Object value, int row, int column )
    {
        EntityComposite entity = getEntity( row, column );
        if( entity != null )
        {
            Property<Object> property = (Property<Object>) getProperty( column, entity );
            property.set( value );
        }
    }

    public void addTableModelListener( TableModelListener tableModelListener )
    {
        synchronized( this )
        {
            ArrayList<TableModelListener> clone = new ArrayList<TableModelListener>();
            if( listeners != null )
            {
                clone.addAll( listeners );
            }
            clone.add( tableModelListener );
            listeners = clone;
        }
    }

    public void removeTableModelListener( TableModelListener tableModelListener )
    {
        synchronized( this )
        {
            if( listeners == null )
            {
                return;
            }
            if( listeners.size() == 1 )
            {
                if( listeners.contains( tableModelListener ) )
                {
                    listeners = null;
                }
                return;
            }
            ArrayList<TableModelListener> clone = new ArrayList<TableModelListener>();
            clone.addAll( listeners );
            clone.remove( tableModelListener );
            listeners = clone;
        }
    }

    private Property<?> getProperty( int column, EntityComposite entity )
    {
        PropertyDescriptor propertyDescriptor = columns.get( column );
        EntityStateHolder stateHolder = spi.getState( entity );
        Property<?> property = stateHolder.getProperty( propertyDescriptor.accessor() );
        return property;
    }

    private EntityComposite getEntity( int row, int column )
    {
        EntityComposite entity = null;
        if( isList )
        {
            ListAssociation list = (ListAssociation) association;
            entity = (EntityComposite) list.get( row );

        }
        else
        {
            Iterator loop = association.iterator();
            for( int i = 0; i < column && loop.hasNext(); i++ )
            {
                // skip
                loop.next();
            }
            if( loop.hasNext() )
            {
                entity = (EntityComposite) loop.next();
            }
        }
        return entity;
    }
}
