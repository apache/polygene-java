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
package org.qi4j.envisage.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import org.qi4j.tools.model.descriptor.*;
import org.qi4j.tools.model.util.DescriptorNameComparator;

/**
 * Helper class to build tree model for Qi4J model as Type Tree
 */
public class TypeModelBuilder
{
    private List<ServiceDetailDescriptor> serviceList;
    private List<ImportedServiceDetailDescriptor> importedServiceList;
    private List<EntityDetailDescriptor> entityList;
    private List<CompositeDetailDescriptor> transientList;
    private List<ValueDetailDescriptor> valueList;
    private List<ObjectDetailDescriptor> objectList;

    public static MutableTreeNode build( ApplicationDetailDescriptor descriptor )
    {
        TypeModelBuilder builder = new TypeModelBuilder();
        return builder.buildNode( descriptor );
    }

    private TypeModelBuilder()
    {
        serviceList = new ArrayList<ServiceDetailDescriptor>();
        importedServiceList = new ArrayList<ImportedServiceDetailDescriptor>();
        entityList = new ArrayList<EntityDetailDescriptor>();
        transientList = new ArrayList<CompositeDetailDescriptor>();
        valueList = new ArrayList<ValueDetailDescriptor>();
        objectList = new ArrayList<ObjectDetailDescriptor>();
    }

    private MutableTreeNode buildNode( ApplicationDetailDescriptor descriptor )
    {
        traverseLayers( descriptor.layers() );

        DescriptorNameComparator<Object> nameComparator = new DescriptorNameComparator<Object>();

        // sort based on name order
        Collections.sort( serviceList, nameComparator );
        Collections.sort( importedServiceList, nameComparator );
        Collections.sort( entityList, nameComparator );
        Collections.sort( transientList, nameComparator );
        Collections.sort( valueList, nameComparator );
        Collections.sort( objectList, nameComparator );

        DefaultMutableTreeNode root = new DefaultMutableTreeNode( descriptor );
        DefaultMutableTreeNode child;

        child = new DefaultMutableTreeNode( "Services" );
        addChild( child, serviceList );
        root.add( child );

        child = new DefaultMutableTreeNode( "Imported Services" );
        addChild( child, importedServiceList );
        root.add( child );

        child = new DefaultMutableTreeNode( "Entities" );
        addChild( child, entityList );
        root.add( child );

        child = new DefaultMutableTreeNode( "Transients" );
        addChild( child, transientList );
        root.add( child );

        child = new DefaultMutableTreeNode( "Values" );
        addChild( child, valueList );
        root.add( child );

        child = new DefaultMutableTreeNode( "Objects" );
        addChild( child, objectList );
        root.add( child );

        return root;
    }

    private void addChild( DefaultMutableTreeNode node, List list )
    {
        for( int i = 0; i < list.size(); i++ )
        {
            node.add( new DefaultMutableTreeNode( list.get( i ) ) );
        }
    }

    private void traverseLayers( Iterable<LayerDetailDescriptor> iter )
    {
        for( LayerDetailDescriptor descriptor : iter )
        {
            traverseModules( descriptor.modules() );
        }
    }

    private void traverseModules( Iterable<ModuleDetailDescriptor> iter )
    {
        for( ModuleDetailDescriptor descriptor : iter )
        {

            // Services
            for( ServiceDetailDescriptor child : descriptor.services() )
            {
                serviceList.add( child );
            }

            // Imported Services
            for( ImportedServiceDetailDescriptor child : descriptor.importedServices() )
            {
                importedServiceList.add( child );
            }

            // Entities
            for( EntityDetailDescriptor child : descriptor.entities() )
            {
                entityList.add( child );
            }

            // Transient
            for( CompositeDetailDescriptor child : descriptor.composites() )
            {
                transientList.add( child );
            }

            //Values
            for( ValueDetailDescriptor child : descriptor.values() )
            {
                valueList.add( child );
            }

            // Objects
            for( ObjectDetailDescriptor child : descriptor.objects() )
            {
                objectList.add( child );
            }
        }
    }
}
