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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.visualizer.detailPanel.internal.common.Util;
import org.qi4j.library.swing.visualizer.model.CompositeMethodDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.MethodConcernDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.MethodConstraintsDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.MethodSideEffectDetailDescriptor;
import org.qi4j.spi.composite.MethodConcernDescriptor;
import org.qi4j.spi.composite.MethodSideEffectDescriptor;

/**
 * @author Sony Gill
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class MixinTree extends JTree
{
    private final CompositeMethodDetailDescriptor methodDetailDescriptor;

    MixinTree( CompositeMethodDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        methodDetailDescriptor = aDescriptor;

        Class mixinClass = aDescriptor.descriptor().mixin().mixinClass();
        Iterable<MethodConstraintsDetailDescriptor> constraints = aDescriptor.constraints();
        Iterable<MethodConcernDetailDescriptor> concerns = aDescriptor.concerns();
        Iterable<MethodSideEffectDetailDescriptor> sideEffects = aDescriptor.sideEffects();

        final DefaultMutableTreeNode root = new DefaultMutableTreeNode( mixinClass.getName() );
        addFields( root, mixinClass );
        addConstructors( root, mixinClass );
        addMethods( root, mixinClass, constraints, concerns, sideEffects );
        DefaultTreeModel model = new DefaultTreeModel( root );
        setModel( model );

        setCellRenderer( new MixinCellRenderer( root ) );
    }


    private void addFields( DefaultMutableTreeNode rootNode, Class mixinClass )
    {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode( "Fields" );
        Field[] fields = mixinClass.getDeclaredFields();
        for( Field field : fields )
        {
            String fieldStr = "";
            for( Annotation annotation : field.getAnnotations() )
            {
                fieldStr += annotation.toString() + " ";
            }

            fieldStr += field.getType().getSimpleName() + " " + field.getName();

            node.add( new DefaultMutableTreeNode( fieldStr ) );
        }
        if( node.getChildCount() > 0 )
        {
            rootNode.add( node );
        }
    }

    private void addConstructors( DefaultMutableTreeNode rootNode, Class mixinClass )
    {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode( "Constructors" );
        Constructor[] allConstructors = mixinClass.getDeclaredConstructors();
        for( Constructor constructor : allConstructors )
        {
            StringBuilder buf = new StringBuilder();
            buf.append( constructor.toGenericString() );
            node.add( new DefaultMutableTreeNode( buf.toString() ) );
        }
        if( node.getChildCount() > 0 )
        {
            rootNode.add( node );
        }
    }

    private void addMethods(
        DefaultMutableTreeNode rootNode,
        Class mixinClass,
        Iterable<MethodConstraintsDetailDescriptor> constraints,
        Iterable<MethodConcernDetailDescriptor> concerns,
        Iterable<MethodSideEffectDetailDescriptor> sideEffects )
    {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode( "Methods" );
        Method[] methods = mixinClass.getDeclaredMethods();
        for( Method method : methods )
        {
            // access$000 is a synthetic method generated to allow access to private fields from anon inner classes
            if( !method.getName().equals( "access$000" ) )
            {
                node.add( getMethodNode( method, constraints, concerns, sideEffects ) );
            }
        }

        if( node.getChildCount() > 0 )
        {
            rootNode.add( node );
        }
    }

    private MutableTreeNode getMethodNode(
        Method method,
        Iterable<MethodConstraintsDetailDescriptor> constraints,
        Iterable<MethodConcernDetailDescriptor> concerns,
        Iterable<MethodSideEffectDetailDescriptor> sideEffects )
    {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode( Util.methodToString( method ) );
        addConstraints( method, node, constraints );
        addConcerns( method, node, concerns );
        addSideEffects( method, node, sideEffects );
        return node;
    }

    private void addConstraints(
        Method method,
        DefaultMutableTreeNode methodNode,
        Iterable<MethodConstraintsDetailDescriptor> constraints )
    {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode( "Constraints" );
        for( MethodConstraintsDetailDescriptor constraint : constraints )
        {
            node.add( new DefaultMutableTreeNode( constraint ) );
        }

        // TODO
//        if( node.getChildCount() > 0 )
//        {
//            methodNode.add( node );
//        }
    }

    private void addConcerns(
        Method method,
        DefaultMutableTreeNode methodNode,
        Iterable<MethodConcernDetailDescriptor> concerns )
    {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode( "Concerns" );
        for( MethodConcernDetailDescriptor concern : concerns )
        {
            MethodConcernDescriptor descriptor = concern.descriptor();
            Class clazz = descriptor.modifierClass();
            String className = clazz.getName();
            node.add( new DefaultMutableTreeNode( className ) );
        }

        if( node.getChildCount() > 0 )
        {
            methodNode.add( node );
        }
    }

    private void addSideEffects(
        Method method,
        DefaultMutableTreeNode methodNode,
        Iterable<MethodSideEffectDetailDescriptor> sideEffects )
    {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode( "Side Effects" );
        for( MethodSideEffectDetailDescriptor sideEffect : sideEffects )
        {
            MethodSideEffectDescriptor descriptor = sideEffect.descriptor();
            Class clazz = descriptor.modifierClass();
            String className = clazz.getName();
            node.add( new DefaultMutableTreeNode( className ) );
        }

        if( node.getChildCount() > 0 )
        {
            methodNode.add( node );
        }
    }

    private static class MixinCellRenderer
        extends DefaultTreeCellRenderer
    {
        private static final Icon groupIcon = new ImageIcon( "group.png" );
        private static final Icon itemIcon = new ImageIcon( "item.png" );
        private static final Icon mixinIcon = new ImageIcon( "mixin.png" );

        private final DefaultMutableTreeNode root;

        public MixinCellRenderer( DefaultMutableTreeNode aRootNode )
        {
            root = aRootNode;
            setOpenIcon( groupIcon );
            setClosedIcon( groupIcon );
            setLeafIcon( itemIcon );
        }

        public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus )
        {
            super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf, row, hasFocus );
            if( value == root )
            {
                setIcon( mixinIcon );
            }
            return this;
        }
    }
}