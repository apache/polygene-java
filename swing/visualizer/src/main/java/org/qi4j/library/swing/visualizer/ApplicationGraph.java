/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008, Sonny Gill. All Rights Reserved.
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.swing.visualizer;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.JComponent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.qi4j.structure.Application;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.entity.association.AbstractAssociation;
import org.qi4j.entity.association.GenericAssociationInfo;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.controls.ControlAdapter;
import prefuse.visual.VisualItem;

/**
 * TODO
 */
public class ApplicationGraph
{
    static final int TYPE_APPLICATION = 0;
    static final int TYPE_LAYER = 1;
    static final int TYPE_MODULE = 2;
    static final int TYPE_COMPOSITE = 3;
    static final int TYPE_GROUP = 4;

    static final int TYPE_EDGE_HIDDEN = 100;

    private String applicationName;
    private ApplicationGraphVisitor appGraphVisitor;
    private ApplicationPanel applicationPanel;
    private JSplitPane bottomPane;

    private Dimension methodsPaneSize = new Dimension( 300, 200 );

    public void show( Application application )
    {
        JFrame frame = new JFrame( "Qi4j Application Graph" );

        Graph graph = new Graph( true );
        appGraphVisitor = new ApplicationGraphVisitor( graph );
        ( (ApplicationSPI) application ).visitDescriptor( appGraphVisitor );
        applicationPanel = new ApplicationPanel( graph, new CompositeSelectionControl() );

//        applicationPanel.setMinimumSize( new Dimension( 400, 400 ) );
        applicationPanel.setPreferredSize( new Dimension( 800, 600 ) );
        JPanel panel = new JPanel();
        panel.setPreferredSize( methodsPaneSize );
        panel.setBackground( Color.white );
        bottomPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, panel, getDetailsPane() );

        JSplitPane mainPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, applicationPanel, bottomPane );
        mainPane.setOneTouchExpandable( true );
        frame.add( mainPane );

        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.pack();
        frame.setVisible( true );

        applicationPanel.graphShown();

    }

    private JComponent getDetailsPane()
    {
        JPanel detailsPanel = new JPanel();
        detailsPanel.setBackground( Color.white );

        StringBuilder buf = new StringBuilder();
        buf.append( "Controls - \n" );
        buf.append( "1. Zoom with mouse scroll wheel\n" );
        buf.append( "2. Pan with mouse\n" );
        buf.append( "3. Double click to zoom in, Shift + Double click to zoom out\n" );
        buf.append( "4. + key to zoom in, - key to zoom out\n" );
        JTextArea textArea = new JTextArea();
        textArea.setText( buf.toString() );

        JScrollPane pane = new JScrollPane( textArea );
        pane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );

        return pane;
    }

    private class CompositeSelectionControl extends ControlAdapter
    {
        public void itemClicked( VisualItem visualItem, MouseEvent event )
        {
            // ATM this is not fired for Services and Objects because they are not of type CompositeDescriptor
            if( GraphUtils.isComposite( visualItem ) )
            {
                Node node = (Node) visualItem.getSourceTuple();
                Object o = appGraphVisitor.getCompositeDescriptor( node );
                if( o != null && o instanceof CompositeDescriptor )
                {
                    CompositeDescriptor descriptor = (CompositeDescriptor) o;

                    final DefaultMutableTreeNode root = new DefaultMutableTreeNode( GraphUtils.getCompositeName( descriptor.type() ) );
                    Iterable<Class> mixinTypes = descriptor.mixinTypes();
                    for( Class mixinType : mixinTypes )
                    {
                        DefaultMutableTreeNode mixinNode = new DefaultMutableTreeNode( mixinType.getSimpleName() );
                        root.add( mixinNode );
                        Method[] methods = mixinType.getMethods();
                        for( Method method : methods )
                        {
                            MethodNode methodNode = new MethodNode( method );
                            mixinNode.add( methodNode );
                        }
                    }

                    final JTree tree = new JTree( root );
                    tree.addTreeSelectionListener( new TreeSelectionListener()
                    {
                        public void valueChanged( TreeSelectionEvent e )
                        {
                            Object o = tree.getLastSelectedPathComponent();
                            applicationPanel.clearCompositeSelection();
                            if( o instanceof MethodNode )
                            {
                                Method method = ( (MethodNode) o ).getMethod();

                                Class<?> returnType = method.getReturnType();
                                if( AbstractAssociation.class.isAssignableFrom( returnType ) )
                                {
                                    Type type = GenericAssociationInfo.getAssociationType( method );
                                    String name = GraphUtils.getCompositeName( (Class) type );
                                    applicationPanel.selectComposite( name );
                                }
                                //todo show link to other composites if the return type is an association

                                Annotation[] annotations = method.getAnnotations();
                                for( Annotation annotation : annotations )
                                {
                                    System.out.println( annotation.annotationType() + ", " + annotation.toString() + "\n" );
                                }

                                Map<String, List> map = appGraphVisitor.getMethodAttributes( method );

                                List constraints = map.get( "constraints" );
                                List concerns = map.get( "concerns" );
                                List sideEffects = map.get( "sideEffects" );
                                List mixins = map.get( "mixins" );
                                Class mixinClass = (Class) mixins.get( 0 );

                                JTree mixinTree = new MixinTree( mixinClass, constraints, concerns, sideEffects );
                                JScrollPane scrollPane = new JScrollPane( mixinTree );
                                bottomPane.setRightComponent( scrollPane );

                            }

                        }
                    } );
                    tree.setCellRenderer( new DefaultTreeCellRenderer()
                    {
                        private Icon compositeIcon = new ImageIcon( "composites.png" );
                        private Icon interfaceIcon = new ImageIcon( "interface.png" );
                        private Icon methodIcon = new ImageIcon( "methods.png" );
                        public Component getTreeCellRendererComponent( JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus )
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
                    });

                    JScrollPane pane = new JScrollPane( tree );
                    pane.setPreferredSize( methodsPaneSize );
                    bottomPane.setLeftComponent( pane );
                }
            }
        }

        private class MethodNode extends DefaultMutableTreeNode
        {
            private Method method;

            private MethodNode( Method method )
            {
                if( method == null )
                {
                    throw new NullPointerException( "Method is null" );
                }
                this.method = method;
            }

            public String toString()
            {
                return methodToString( method );
            }

            public Method getMethod()
            {
                return method;
            }
        }
    }

    private class MixinTree extends JTree
    {
        public MixinTree( Class mixinClass, List constraints, List concerns, List sideEffects )
        {
            final DefaultMutableTreeNode root = new DefaultMutableTreeNode( mixinClass.getName() );
            addFields( root, mixinClass );
            addConstructors( root, mixinClass );
            addMethods( root, mixinClass, constraints, concerns, sideEffects );
            DefaultTreeModel model = new DefaultTreeModel( root );
            setModel( model );

            setCellRenderer( new DefaultTreeCellRenderer()
            {
                private Icon groupIcon = new ImageIcon( "group.png" );
                private Icon itemIcon = new ImageIcon( "item.png" );
                private Icon mixinIcon = new ImageIcon( "mixin.png" );

                {
                    setOpenIcon( groupIcon );
                    setClosedIcon( groupIcon );
                    setLeafIcon( itemIcon );
                }

                public Component getTreeCellRendererComponent( JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus )
                {
                    super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf, row, hasFocus );
                    if( value == root )
                    {
                        setIcon( mixinIcon );
                    }
                    return this;
                }
            });
        }

        private void addFields( DefaultMutableTreeNode rootNode, Class mixinClass )
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode( "Fields" );
            Field[] fields = mixinClass.getDeclaredFields();
            for( Field field : fields )
            {
                node.add( new DefaultMutableTreeNode( field.getName() ) );
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
            if( node.getChildCount() > 0)
            {
                rootNode.add( node );
            }
        }

        private void addMethods( DefaultMutableTreeNode rootNode, Class mixinClass, List constraints, List concerns, List sideEffects )
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

        private MutableTreeNode getMethodNode( Method method, List constraints, List concerns, List sideEffects )
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode( methodToString( method ));
            addConstraints( method, node, constraints );
            addConcerns( method, node, concerns );
            addSideEffects( method, node, sideEffects );
            return node;
        }

        private void addConstraints( Method method, DefaultMutableTreeNode methodNode, List constraints )
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode( "Constraints" );
            for( Object constraint : constraints )
            {
                node.add( new DefaultMutableTreeNode( constraint ) );
            }
        }

        private void addConcerns( Method method, DefaultMutableTreeNode methodNode, List concerns )
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode( "Concerns" );
            for( Object concern : concerns )
            {
                Class clazz = (Class) concern;
                try
                {
                    clazz.getDeclaredMethod( method.getName(), method.getParameterTypes() );
                    // The concern applies to this method
                    node.add( new DefaultMutableTreeNode( clazz.getName() ) );
                }
                catch( NoSuchMethodException e )
                {
                    // ignore, the concern does not implement this method
                }
            }
            if( node.getChildCount() > 0 )
            {
                methodNode.add( node );
            }
        }

        private void addSideEffects( Method method, DefaultMutableTreeNode methodNode, List sideEffects )
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode( "Side Effects" );
            for( Object sideEffect : sideEffects )
            {
                Class clazz = (Class) sideEffect;
                try
                {
                    clazz.getDeclaredMethod( method.getName(), method.getParameterTypes() );
                    // The side effect applies to this method
                    node.add( new DefaultMutableTreeNode( clazz.getName() ) );
                }
                catch( NoSuchMethodException e )
                {
                    // ignore, the side effect does not implement this method
                }
            }
            if( node.getChildCount() > 0 )
            {
                methodNode.add( node );
            }
        }


    }

    private static void appendAnnoation( StringBuilder buf, Annotation annotation )
    {
        buf.append( "@" ).append( annotation.annotationType().getSimpleName() ).append( " " );
    }

    public static String methodToString( Method method )
    {
        StringBuilder buf = new StringBuilder();

        for( Annotation annotation : method.getAnnotations() )
        {
            appendAnnoation( buf, annotation );
        }
        Class<?> returnType = method.getReturnType();

        // todo add the 'Type' if the returnType is Property<T>

        buf.append( returnType.getSimpleName() ).append( " " ).append( method.getName() );

        buf.append( "( " );
        Class<?>[] paramTypes = method.getParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        for( int i = 0; i < paramTypes.length; i++ )
        {
            Annotation[] annotations = paramAnnotations[ i ];
            for( Annotation annotation : annotations )
            {
                appendAnnoation( buf, annotation );
            }
            Class<?> type = paramTypes[ i ];
            buf.append( type.getSimpleName() ).append( ", " );
        }
        if( paramTypes.length > 0 )
        {
            buf.delete( buf.length() - 2, buf.length() );
        }

        buf.append( " )" );

        return buf.toString();
    }

}
