/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.envisage.tree;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import org.apache.polygene.tools.model.descriptor.*;
import org.apache.polygene.tools.model.util.DescriptorNameComparator;

/**
 * Helper class to build tree model for Polygene model as Type Tree
 */
/* package */ final class TypeModelBuilder
{
    private final List<ServiceDetailDescriptor> serviceList = new ArrayList<>();
    private final List<ImportedServiceDetailDescriptor> importedServiceList = new ArrayList<>();
    private final List<EntityDetailDescriptor> entityList = new ArrayList<>();
    private final List<TransientDetailDescriptor> transientList = new ArrayList<>();
    private final List<ValueDetailDescriptor> valueList = new ArrayList<>();
    private final List<ObjectDetailDescriptor> objectList = new ArrayList<>();

    /* package */ static MutableTreeNode build( ApplicationDetailDescriptor descriptor )
    {
        TypeModelBuilder builder = new TypeModelBuilder();
        return builder.buildNode( descriptor );
    }

    private TypeModelBuilder()
    {
    }

    private MutableTreeNode buildNode( ApplicationDetailDescriptor descriptor )
    {
        traverseLayers( descriptor.layers() );

        DescriptorNameComparator<Object> nameComparator = new DescriptorNameComparator<>();

        // sort based on name order
        serviceList.sort( nameComparator );
        importedServiceList.sort( nameComparator );
        entityList.sort( nameComparator );
        transientList.sort( nameComparator );
        valueList.sort( nameComparator );
        objectList.sort( nameComparator );

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
        for( Object obj : list )
        {
            node.add( new DefaultMutableTreeNode( obj ) );
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
            for( TransientDetailDescriptor child : descriptor.transients() )
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
