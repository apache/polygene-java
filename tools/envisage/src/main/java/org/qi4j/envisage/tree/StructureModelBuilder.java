/*
 * Copyright (c) 2009, Tony Kohar. All Rights Reserved.
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
package org.qi4j.envisage.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import org.qi4j.tools.model.descriptor.*;
import org.qi4j.tools.model.util.DescriptorNameComparator;

/**
 * Helper class to build tree model for Qi4J model as Structure Tree
 */
/* package */ final class StructureModelBuilder
{
    private final DescriptorNameComparator<Object> nameComparator = new DescriptorNameComparator<>();
    private final List<Object> tempList = new ArrayList<>();   // used for sorting

    /* package */ static MutableTreeNode build( ApplicationDetailDescriptor descriptor )
    {
        StructureModelBuilder builder = new StructureModelBuilder();
        return builder.buildApplicationNode( descriptor );
    }

    private MutableTreeNode buildApplicationNode( ApplicationDetailDescriptor descriptor )
    {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode( descriptor );
        buildLayersNode( node, descriptor.layers() );
        return node;
    }

    private void buildLayersNode( DefaultMutableTreeNode parent, Iterable<LayerDetailDescriptor> iter )
    {
        for( LayerDetailDescriptor descriptor : iter )
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode( descriptor );
            buildModulesNode( node, descriptor.modules() );
            parent.add( node );
        }
    }

    private void buildModulesNode( DefaultMutableTreeNode parent, Iterable<ModuleDetailDescriptor> iter )
    {
        for( ModuleDetailDescriptor descriptor : iter )
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode( descriptor );
            buildServicesNode( node, descriptor.services() );
            buildImportedServicesNode( node, descriptor.importedServices() );
            buildEntitiesNode( node, descriptor.entities() );
            buildTransientsNode( node, descriptor.composites() );  // This is transient type
            buildValuesNode( node, descriptor.values() );
            buildObjectsNode( node, descriptor.objects() );
            parent.add( node );
        }
    }

    private void addTypeChildren( DefaultMutableTreeNode parent, List<Object> childList )
    {
        Collections.sort( childList, nameComparator );

        for( int i = 0; i < childList.size(); i++ )
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode( childList.get( i ) );
            parent.add( node );
        }
    }

    private void buildServicesNode( DefaultMutableTreeNode parent, Iterable<ServiceDetailDescriptor> iter )
    {
        tempList.clear();
        for( ServiceDetailDescriptor descriptor : iter )
        {
            tempList.add( descriptor );
        }

        addTypeChildren( parent, tempList );
    }

    private void buildImportedServicesNode( DefaultMutableTreeNode parent,
                                            Iterable<ImportedServiceDetailDescriptor> iter
    )
    {
        tempList.clear();
        for( ImportedServiceDetailDescriptor descriptor : iter )
        {
            tempList.add( descriptor );
        }

        addTypeChildren( parent, tempList );
    }

    private void buildEntitiesNode( DefaultMutableTreeNode parent, Iterable<EntityDetailDescriptor> iter )
    {
        tempList.clear();
        for( EntityDetailDescriptor descriptor : iter )
        {
            tempList.add( descriptor );
        }

        addTypeChildren( parent, tempList );
    }

    private void buildTransientsNode( DefaultMutableTreeNode parent, Iterable<CompositeDetailDescriptor> iter )
    {
        tempList.clear();
        for( CompositeDetailDescriptor descriptor : iter )
        {
            tempList.add( descriptor );
        }

        addTypeChildren( parent, tempList );
    }

    private void buildValuesNode( DefaultMutableTreeNode parent, Iterable<ValueDetailDescriptor> iter )
    {
        tempList.clear();
        for( ValueDetailDescriptor descriptor : iter )
        {
            tempList.add( descriptor );
        }

        addTypeChildren( parent, tempList );
    }

    private void buildObjectsNode( DefaultMutableTreeNode parent, Iterable<ObjectDetailDescriptor> iter )
    {
        tempList.clear();
        for( ObjectDetailDescriptor descriptor : iter )
        {
            tempList.add( descriptor );
        }

        addTypeChildren( parent, tempList );
    }
}
