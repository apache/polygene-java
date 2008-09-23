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
package org.qi4j.library.swing.visualizer.detailPanel.internal.composite;

import java.awt.Component;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.qi4j.composite.Composite;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.CompositeMethodDetailDescriptor;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.composite.CompositeMethodDescriptor;

/**
 * @author Sonny Gill
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class CompositeDetailPanel extends JSplitPane
{
    public CompositeDetailPanel( CompositeDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        super( HORIZONTAL_SPLIT );

        validateNotNull( "aDescriptor", aDescriptor );

        CompositeDescriptor descriptor = aDescriptor.descriptor();
        Class<? extends Composite> compositeType = descriptor.type();
        String compositeName = compositeType.getSimpleName();

        final DefaultMutableTreeNode root = new DefaultMutableTreeNode( compositeName );

        Iterable<Class> mixinTypes = descriptor.mixinTypes();
        for( Class mixinType : mixinTypes )
        {
            String mixinNodeName = mixinType.getSimpleName();
            boolean isInvocationHandler = InvocationHandler.class.isAssignableFrom( mixinType );
            if( isInvocationHandler )
            {
                mixinNodeName += " [IH]";
            }
            DefaultMutableTreeNode mixinNode = new DefaultMutableTreeNode( mixinNodeName );
            root.add( mixinNode );

            // TODO: This is probably wrong
            if( !isInvocationHandler )
            {
                Method[] methods = mixinType.getDeclaredMethods();
                for( Method method : methods )
                {
                    CompositeMethodDetailDescriptor methodDescriptor = aDescriptor.getMethodDescriptor( method );
                    MethodNode methodNode = new MethodNode( method, methodDescriptor );
                    mixinNode.add( methodNode );
                }
            }
        }

        JTree tree = new JTree( root );
        tree.addTreeSelectionListener( new CompositeTreeSelectionListener( tree ) );
        tree.setCellRenderer( new CompositeCellRenderer( root ) );

        JScrollPane scrollPane = new JScrollPane( tree );
        setLeftComponent( scrollPane );
    }


    private class CompositeTreeSelectionListener
        implements TreeSelectionListener
    {
        private final JTree tree;

        private CompositeTreeSelectionListener( JTree aTree )
        {
            tree = aTree;
        }

        public void valueChanged( TreeSelectionEvent e )
        {
            Object node = tree.getLastSelectedPathComponent();

            if( node instanceof MethodNode )
            {
                MethodNode methodNode = (MethodNode) node;

                CompositeMethodDetailDescriptor methodDetailDescriptor = methodNode.descriptor();
                if( methodDetailDescriptor == null )
                {
                    // TODO: This means, Invocation handler implements the method
                    return;
                }

                CompositeMethodDescriptor methodDescriptor = methodDetailDescriptor.descriptor();
                Method method = methodDescriptor.method();

//                Class<?> returnType = method.getReturnType();
//                if( AbstractAssociation.class.isAssignableFrom( returnType ) )
//                {
//                    Type type = GenericAssociationInfo.getAssociationType( method );
//                    String name = ( (Class) type ).getSimpleName();
//                        overviewPanel.selectComposite( name );
//                }

                // TODO: show link to other composites if the return type is an association
                Annotation[] annotations = method.getAnnotations();
                for( Annotation annotation : annotations )
                {
                    System.out.println( annotation.annotationType() + ", " + annotation.toString() + "\n" );
                }

                JTree mixinTree = new MixinTree( methodDetailDescriptor );
                JScrollPane scrollPane = new JScrollPane( mixinTree );
                setRightComponent( scrollPane );
            }
        }
    }

    private static class CompositeCellRenderer extends DefaultTreeCellRenderer
    {
        private Icon compositeIcon;
        private Icon interfaceIcon;
        private Icon methodIcon;
        private final DefaultMutableTreeNode root;

        public CompositeCellRenderer( DefaultMutableTreeNode aRoot )
        {
            root = aRoot;

            compositeIcon = new ImageIcon( "composites.png" );
            interfaceIcon = new ImageIcon( "interface.png" );
            methodIcon = new ImageIcon( "methods.png" );
        }

        public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus )
        {
            super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf, row, hasFocus );

            if( value == root )
            {
                setIcon( compositeIcon );
            }
            else if( value instanceof MethodNode )
            {
                setIcon( methodIcon );
            }
            else
            {
                setIcon( interfaceIcon );
            }

            return this;
        }
    }
}
