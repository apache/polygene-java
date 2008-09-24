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
package org.qi4j.library.swing.visualizer.detailPanel.internal.application;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import org.qi4j.composite.Composite;
import org.qi4j.library.swing.visualizer.model.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ObjectDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ServiceDetailDescriptor;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.object.ObjectDescriptor;
import org.qi4j.spi.structure.ApplicationDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class Qi4jDescriptorTree extends JTree
{
    public Qi4jDescriptorTree( TreeNode treeNode )
    {
        super( treeNode );
    }

    @Override
    public final String convertValueToText(
        Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus )
    {
        if( value == null )
        {
            return "";
        }

        Class<?> valueClass = value.getClass();
        if( DefaultMutableTreeNode.class.isAssignableFrom( valueClass ) )
        {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
            value = treeNode.getUserObject();
        }

        return valueToString( value );
    }

    private String valueToString( Object value )
    {
        if( value == null )
        {
            return "";
        }

        Class<?> valueClass = value.getClass();
        if( String.class.isAssignableFrom( valueClass ) )
        {
            return (String) value;
        }

        if( ApplicationDetailDescriptor.class.isAssignableFrom( valueClass ) )
        {
            ApplicationDetailDescriptor detailDescriptor = (ApplicationDetailDescriptor) value;
            ApplicationDescriptor descriptor = detailDescriptor.descriptor();
            return descriptor.name();
        }
        else if( ServiceDetailDescriptor.class.isAssignableFrom( valueClass ) )
        {
            ServiceDetailDescriptor detailDescriptor = (ServiceDetailDescriptor) value;
            ServiceDescriptor descriptor = detailDescriptor.descriptor();
            Class<?> serviceClass = descriptor.type();
            String serviceClassName = serviceClass.getSimpleName();
            return serviceClassName + ":" + descriptor.identity();
        }
        else if( EntityDetailDescriptor.class.isAssignableFrom( valueClass ) )
        {
            EntityDetailDescriptor detailDescriptor = (EntityDetailDescriptor) value;
            EntityDescriptor descriptor = detailDescriptor.descriptor();
            Class<? extends Composite> entityClass = descriptor.type();
            return entityClass.getName();
        }
        else if( CompositeDetailDescriptor.class.isAssignableFrom( valueClass ) )
        {
            CompositeDetailDescriptor detailDescriptor = (CompositeDetailDescriptor) value;
            CompositeDescriptor descriptor = detailDescriptor.descriptor();
            Class<? extends Composite> compositeClass = descriptor.type();
            return compositeClass.getName();
        }
        else if( ObjectDetailDescriptor.class.isAssignableFrom( valueClass ) )
        {
            ObjectDetailDescriptor detailDescriptor = (ObjectDetailDescriptor) value;
            ObjectDescriptor descriptor = detailDescriptor.descriptor();
            Class<? extends Composite> objectClassName = descriptor.type();
            return objectClassName.getName();
        }

        return value.toString();
    }
}
