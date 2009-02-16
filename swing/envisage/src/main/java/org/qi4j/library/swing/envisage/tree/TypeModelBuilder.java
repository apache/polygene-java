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
import org.qi4j.library.swing.visualizer.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ServiceDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ModuleDetailDescriptor;

/**
 * Helper class to build tree model as type
 *
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class TypeModelBuilder
{
    private static final int SERVICES_NODE = 0;
    private static final int ENTITIES_NODE = 1;
    private static final int VALUES_NODE = 2;
    private static final int TRANSIENTs_NODE = 3;
    private static final int OBJECTS_NODE = 4;

    public static MutableTreeNode build( ApplicationDetailDescriptor descriptor )
    {
        //TypeModelBuilder builder = new TypeModelBuilder();
        //return builder.buildApplicationNode( descriptor );

        return new DefaultMutableTreeNode("Type Root (not done yet)");
    }

    private MutableTreeNode buildNode( ApplicationDetailDescriptor descriptor )
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode( descriptor );
        root.add( new DefaultMutableTreeNode ("Services") );
        root.add( new DefaultMutableTreeNode ("Entities") );
        root.add( new DefaultMutableTreeNode ("Values") );
        root.add( new DefaultMutableTreeNode ("Transients") );
        root.add( new DefaultMutableTreeNode ("Objects") );
        traverseLayers( root, descriptor.layers() );
        return root;
    }

    private void traverseLayers( DefaultMutableTreeNode root, Iterable<LayerDetailDescriptor> iter )
    {
        for( LayerDetailDescriptor descriptor : iter )
        {
            //buildModulesNode( node, descriptor.modules() );
            traverseModules( root, descriptor.modules() );
        }
    }

    private void traverseModules( DefaultMutableTreeNode root, Iterable<ModuleDetailDescriptor> iter )
    {
        for( ModuleDetailDescriptor descriptor : iter )
        {
            //buildModulesNode( node, descriptor.modules() );
        }
    }

}
