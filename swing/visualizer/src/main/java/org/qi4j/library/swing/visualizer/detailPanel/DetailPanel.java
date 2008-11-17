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
package org.qi4j.library.swing.visualizer.detailPanel;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import org.qi4j.injection.scope.Structure;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.common.ServiceDescriptorForm;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.composite.CompositeDescriptorForm;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.composite.CompositeMethodDescriptorForm;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.entity.EntityDescriptorForm;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.layer.LayerDescriptorForm;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.module.ModuleDescriptorForm;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.object.ConstructorDescriptorForm;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.object.InjectedFieldDescriptorForm;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.object.MixinDescriptorForm;
import org.qi4j.library.swing.visualizer.detailPanel.internal.tree.ApplicationTreePanel;
import org.qi4j.library.swing.visualizer.listener.SelectionListener;
import org.qi4j.library.swing.visualizer.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.CompositeMethodDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ConstructorDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.InjectedFieldDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.MixinDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ObjectDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.ServiceDetailDescriptor;
import org.qi4j.object.ObjectBuilder;
import org.qi4j.object.ObjectBuilderFactory;

/**
 * @author edward.yakop@gmail.com
 * @author Sonny Gill
 * @since 0.5
 */
public final class DetailPanel
    implements SelectionListener
{
    private ObjectBuilderFactory obf;

    private Object descriptorForm;

    private ApplicationTreePanel treePanel;
    private JScrollPane detailScrollPanel;

    private JPanel detailPanel;

    public DetailPanel( @Structure ObjectBuilderFactory anOBF )
    {
        obf = anOBF;
        $$$setupUI$$$();
    }

    private void createUIComponents()
    {
        ObjectBuilder<ApplicationTreePanel> builder = obf.newObjectBuilder( ApplicationTreePanel.class );
        builder.use( this );
        treePanel = builder.newInstance();
    }

    public final void onApplicationSelected( ApplicationDetailDescriptor aDescriptor )
    {
        descriptorForm = null;
        detailScrollPanel.setViewportView( null );

        treePanel.onApplicationSelected( aDescriptor );
    }

    public final void onLayerSelected( LayerDetailDescriptor aDescriptor )
    {
        LayerDescriptorForm layerForm;
        if( !( descriptorForm instanceof LayerDescriptorForm ) )
        {
            layerForm = obf.newObject( LayerDescriptorForm.class );
            detailScrollPanel.setViewportView( layerForm.$$$getRootComponent$$$() );
        }
        else
        {
            layerForm = (LayerDescriptorForm) descriptorForm;
        }
        descriptorForm = layerForm;
        layerForm.updateModel( aDescriptor );

        treePanel.onLayerSelected( aDescriptor );
    }

    public final void onModuleSelected( ModuleDetailDescriptor aDescriptor )
    {
        ModuleDescriptorForm moduleForm;

        if( !( descriptorForm instanceof ModuleDescriptorForm ) )
        {
            moduleForm = obf.newObject( ModuleDescriptorForm.class );
            detailScrollPanel.setViewportView( moduleForm.$$$getRootComponent$$$() );
        }
        else
        {
            moduleForm = (ModuleDescriptorForm) descriptorForm;
        }
        descriptorForm = moduleForm;
        moduleForm.updateModel( aDescriptor );

        treePanel.onModuleSelected( aDescriptor );
    }

    public final void onServiceSelected( ServiceDetailDescriptor aDescriptor )
    {
        ServiceDescriptorForm serviceForm;

        if( !( descriptorForm instanceof ServiceDescriptorForm ) )
        {
            serviceForm = obf.newObject( ServiceDescriptorForm.class );
            detailScrollPanel.setViewportView( serviceForm.$$$getRootComponent$$$() );
        }
        else
        {
            serviceForm = (ServiceDescriptorForm) descriptorForm;
        }
        descriptorForm = serviceForm;
        serviceForm.updateModel( aDescriptor );

        treePanel.onServiceSelected( aDescriptor );
    }

    public final void onEntitySelected( EntityDetailDescriptor aDescriptor )
    {
        EntityDescriptorForm entityDescriptorForm;
        if( !( descriptorForm instanceof EntityDescriptorForm ) )
        {
            entityDescriptorForm = obf.newObject( EntityDescriptorForm.class );
            detailScrollPanel.setViewportView( entityDescriptorForm.$$$getRootComponent$$$() );
        }
        else
        {
            entityDescriptorForm = (EntityDescriptorForm) descriptorForm;
        }
        descriptorForm = entityDescriptorForm;
        entityDescriptorForm.updateModel( aDescriptor );
        treePanel.onEntitySelected( aDescriptor );
    }

    @SuppressWarnings( "unchecked" )
    public final void onCompositeSelected( CompositeDetailDescriptor aDescriptor )
    {
        CompositeDescriptorForm compositeForm;
        if( !( descriptorForm instanceof CompositeDescriptorForm ) )
        {
            compositeForm = obf.newObject( CompositeDescriptorForm.class );
            detailScrollPanel.setViewportView( compositeForm.$$$getRootComponent$$$() );
        }
        else
        {
            compositeForm = (CompositeDescriptorForm) descriptorForm;
        }
        descriptorForm = compositeForm;
        compositeForm.updateModel( aDescriptor );
        treePanel.onCompositeSelected( aDescriptor );
    }

    public final void onObjectSelected( ObjectDetailDescriptor aDescriptor )
    {
        // TODO
        descriptorForm = null;
        treePanel.onObjectSelected( aDescriptor );
    }

    public void onMixinSelected( MixinDetailDescriptor aDescriptor )
    {
        MixinDescriptorForm mixinForm;
        if( !( descriptorForm instanceof MixinDescriptorForm ) )
        {
            mixinForm = obf.newObject( MixinDescriptorForm.class );
            detailScrollPanel.setViewportView( mixinForm.$$$getRootComponent$$$() );
        }
        else
        {
            mixinForm = (MixinDescriptorForm) descriptorForm;
        }
        descriptorForm = mixinForm;
        mixinForm.updateModel( aDescriptor );

        treePanel.onMixinSelected( aDescriptor );
    }

    public final void onConstructorSelected( ConstructorDetailDescriptor aDescriptor )
    {
        ConstructorDescriptorForm constructorForm;
        if( !( descriptorForm instanceof ConstructorDescriptorForm ) )
        {
            constructorForm = obf.newObject( ConstructorDescriptorForm.class );
            detailScrollPanel.setViewportView( constructorForm.$$$getRootComponent$$$() );
        }
        else
        {
            constructorForm = (ConstructorDescriptorForm) descriptorForm;
        }
        descriptorForm = constructorForm;
        constructorForm.updateModel( aDescriptor );

        treePanel.onConstructorSelected( aDescriptor );
    }

    public final void onInjectedFieldSelected( InjectedFieldDetailDescriptor aDescriptor )
    {
        InjectedFieldDescriptorForm fieldForm;
        if( !( descriptorForm instanceof InjectedFieldDescriptorForm ) )
        {
            fieldForm = obf.newObject( InjectedFieldDescriptorForm.class );
            detailScrollPanel.setViewportView( fieldForm.$$$getRootComponent$$$() );
        }
        else
        {
            fieldForm = (InjectedFieldDescriptorForm) descriptorForm;
        }
        descriptorForm = fieldForm;
        fieldForm.updateModel( aDescriptor );

        treePanel.onInjectedFieldSelected( aDescriptor );
    }

    public final void onCompositeMethodSelected( CompositeMethodDetailDescriptor aDescriptor )
    {
        CompositeMethodDescriptorForm methodForm;
        if( !( descriptorForm instanceof CompositeMethodDescriptorForm ) )
        {
            methodForm = obf.newObject( CompositeMethodDescriptorForm.class );
            detailScrollPanel.setViewportView( methodForm.$$$getRootComponent$$$() );
        }
        else
        {
            methodForm = (CompositeMethodDescriptorForm) descriptorForm;
        }
        descriptorForm = methodForm;
        methodForm.updateModel( aDescriptor );

        treePanel.onCompositeMethodSelected( aDescriptor );
    }

    public final void resetSelection()
    {
        treePanel.resetSelection();
        detailScrollPanel.setViewportView( null );
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        createUIComponents();
        detailPanel = new JPanel();
        detailPanel.setLayout( new BorderLayout( 0, 0 ) );
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation( 200 );
        splitPane1.setOneTouchExpandable( true );
        detailPanel.add( splitPane1, BorderLayout.CENTER );
        splitPane1.setLeftComponent( treePanel.$$$getRootComponent$$$() );
        detailScrollPanel = new JScrollPane();
        splitPane1.setRightComponent( detailScrollPanel );
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return detailPanel;
    }
}
