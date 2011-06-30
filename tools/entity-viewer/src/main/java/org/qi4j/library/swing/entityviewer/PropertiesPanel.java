/*  Copyright 2009 Tonny Kohar.
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
package org.qi4j.library.swing.entityviewer;

import org.qi4j.api.Qi4j;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.property.PersistentPropertyDescriptor;
import org.qi4j.api.query.Query;
import org.qi4j.bootstrap.Energy4Java;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * Entity Properties Viewer as Swing Component.
 */
public class PropertiesPanel
    extends JPanel
{
    protected JTable propertiesTable;
    protected Energy4Java qi4j;

    public PropertiesPanel()
    {
        this.setLayout( new BorderLayout() );
        JScrollPane scrollPane = new JScrollPane();
        this.add( scrollPane, BorderLayout.CENTER );

        propertiesTable = new JTable();
        scrollPane.setViewportView( propertiesTable );
    }

    public void initializeQi4J( Energy4Java qi4j )
    {
        this.qi4j = qi4j;
    }

    /**
     * Reload the table data with query based on the supplied query
     *
     * @param query the query to generate table data
     */
    public void reload( Query query )
    {
        TableModel tableModel = createData( query );
        propertiesTable.setModel( tableModel );
    }

    /**
     * Create table table or properties using the supplied query
     *
     * @param query the Query
     *
     * @return TableModel
     */
    protected TableModel createData( Query query )
    {
        DefaultTableModel model = new DefaultTableModel();

        Qi4j api = qi4j.api();

        for( Object qObj : query )
        {
            EntityStateHolder state = api.getState( (EntityComposite) qObj );
            EntityDescriptor descriptor = api.getEntityDescriptor( (EntityComposite) qObj );
            // genereate column, first time only
            if( model.getColumnCount() < 1 )
            {
                for( PersistentPropertyDescriptor persistentPropertyDescriptor : descriptor.state()
                    .<PersistentPropertyDescriptor>properties() )
                {
                    model.addColumn( persistentPropertyDescriptor.qualifiedName().name() );
                }
            }

            Object[] rowData = new Object[model.getColumnCount()];
            int i = 0;
            for( PersistentPropertyDescriptor persistentPropertyDescriptor : descriptor.state().<PersistentPropertyDescriptor>properties() )
            {
                rowData[ i++ ] = state.getProperty( persistentPropertyDescriptor.accessor() );
            }
            model.addRow( rowData );
        }

        return model;
    }
}
