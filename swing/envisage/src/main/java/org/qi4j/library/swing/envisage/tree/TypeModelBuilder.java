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
package org.qi4j.library.swing.envisage.tree;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import org.qi4j.library.swing.envisage.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.LayerDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ServiceDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ModuleDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.EntityDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ObjectDetailDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Helper class to build tree model as type
 *
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class TypeModelBuilder
{

    private List<ServiceDetailDescriptor> serviceList;
    private List<EntityDetailDescriptor> entityList;
    private List<ObjectDetailDescriptor> objectList;

    public static MutableTreeNode build( ApplicationDetailDescriptor descriptor )
    {
        TypeModelBuilder builder = new TypeModelBuilder();
        return builder.buildNode( descriptor );
    }

    private TypeModelBuilder() {
        serviceList = new ArrayList<ServiceDetailDescriptor>();
        entityList = new ArrayList<EntityDetailDescriptor>();
        objectList = new ArrayList<ObjectDetailDescriptor>();
    }

    private MutableTreeNode buildNode( ApplicationDetailDescriptor descriptor )
    {
        traverseLayers( descriptor.layers() );

        // sort based on name order
        // TODO sort on name order not done yet
        //Collections.sort( serviceList );
        //Collections.sort( serviceList );

        DefaultMutableTreeNode root = new DefaultMutableTreeNode( descriptor );
        DefaultMutableTreeNode child;        

        child = new DefaultMutableTreeNode ("Services");
        addChild(child, serviceList);
        root.add( child );

        child = new DefaultMutableTreeNode ("Entities");
        addChild(child, entityList);
        root.add( child );

        // TODO not done yet
        child = new DefaultMutableTreeNode ("Values");
        //addChild(child, objectList);
        root.add( child );

        // TODO not done yet
        child = new DefaultMutableTreeNode ("Transients");
        //addChild(child, objectList);
        root.add( child );

        child = new DefaultMutableTreeNode ("Objects");
        addChild(child, objectList);
        root.add( child );

        return root;
    }

    private void addChild(DefaultMutableTreeNode node, List list)
    {
        for (int i=0; i<list.size(); i++)
        {
            node.add( new DefaultMutableTreeNode( list.get(i) ) );
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
                serviceList.add(child);
            }

            // Entities
            for( EntityDetailDescriptor child : descriptor.entities() )
            {
                entityList.add(child);
            }

            //Values
            
            // Transient

            // Objects
            for( ObjectDetailDescriptor child : descriptor.objects() )
            {
                objectList.add(child);
            }

        }
    }
}
