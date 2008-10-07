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
package org.qi4j.library.swing.visualizer.detailPanel.internal.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import org.qi4j.library.swing.visualizer.model.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.CompositeMethodDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.MixinDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ObjectDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ServiceDetailDescriptor;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.object.ObjectDescriptor;
import org.qi4j.structure.Visibility;

/**
 * TODO: localization
 *
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class TreeModelBuilder
{
    public final DefaultMutableTreeNode build( ApplicationDetailDescriptor aDetailDescriptor )
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode( aDetailDescriptor );

        if( aDetailDescriptor != null )
        {
            addLayersNode( root, aDetailDescriptor );
        }

        return root;
    }

    private void addLayersNode( DefaultMutableTreeNode root, ApplicationDetailDescriptor aDetailDescriptor )
    {
        Iterable<LayerDetailDescriptor> layers = aDetailDescriptor.layers();
        for( LayerDetailDescriptor layer : layers )
        {
            DefaultMutableTreeNode layerNode = new DefaultMutableTreeNode( layer );
            root.add( layerNode );

            addModulesNode( layerNode, layer );
        }
    }

    private void addIfNotEmpty( DefaultMutableTreeNode parent, MutableTreeNode children )
    {
        int numberOfChildren = children.getChildCount();
        if( numberOfChildren > 0 )
        {
            parent.add( children );
        }
    }

    private void addModulesNode( DefaultMutableTreeNode layerNode, LayerDetailDescriptor aLayer )
    {
        Iterable<ModuleDetailDescriptor> modules = aLayer.modules();
        for( ModuleDetailDescriptor module : modules )
        {
            addModuleNode( layerNode, module );
        }
    }

    private void addModuleNode( DefaultMutableTreeNode layerNode, ModuleDetailDescriptor aModule )
    {
        DefaultMutableTreeNode moduleNode = new DefaultMutableTreeNode( aModule );
        layerNode.add( moduleNode );

        DefaultMutableTreeNode servicesNode = new DefaultMutableTreeNode( "services" );
        addServiceNodes( servicesNode, aModule, null );
        addIfNotEmpty( moduleNode, servicesNode );

        DefaultMutableTreeNode entities = new DefaultMutableTreeNode( "entities" );
        addEntityNodes( entities, aModule, null );
        addIfNotEmpty( moduleNode, entities );

        DefaultMutableTreeNode composites = new DefaultMutableTreeNode( "composites" );
        addCompositeNodes( composites, aModule, null );
        addIfNotEmpty( moduleNode, composites );

        DefaultMutableTreeNode objects = new DefaultMutableTreeNode( "objects" );
        addObjectNodes( objects, aModule, null );
        addIfNotEmpty( moduleNode, objects );
    }

    private void addServiceNodes(
        DefaultMutableTreeNode aServicesNode,
        ModuleDetailDescriptor aModule,
        Visibility aVisibilityFilter )
    {
        Iterable<ServiceDetailDescriptor> services = aModule.services();
        for( ServiceDetailDescriptor service : services )
        {
            ServiceDescriptor descriptor = service.descriptor();
            Visibility visibility = descriptor.visibility();
            if( aVisibilityFilter == null || visibility == aVisibilityFilter )
            {
                aServicesNode.add( new DefaultMutableTreeNode( service ) );
            }
        }
    }

    private void addEntityNodes(
        DefaultMutableTreeNode aEntitiesNode,
        ModuleDetailDescriptor aModule,
        Visibility aVisibilityFilter )
    {
        Iterable<EntityDetailDescriptor> entities = aModule.entities();
        for( EntityDetailDescriptor entity : entities )
        {
            EntityDescriptor entityDesc = entity.descriptor();
            Visibility visibility = entityDesc.visibility();
            if( aVisibilityFilter == null || visibility == aVisibilityFilter )
            {
                DefaultMutableTreeNode entityNode = new DefaultMutableTreeNode( entity );
                aEntitiesNode.add( entityNode );
            }
        }
    }

    @SuppressWarnings( "unchecked" )
    private void addCompositeNodes(
        DefaultMutableTreeNode aCompositesNode,
        ModuleDetailDescriptor aModule,
        Visibility aVisibilityFilter )
    {
        Iterable<CompositeDetailDescriptor> composites = aModule.composites();
        for( CompositeDetailDescriptor composite : composites )
        {
            CompositeDescriptor compDesc = composite.descriptor();
            Visibility visibility = compDesc.visibility();
            if( aVisibilityFilter == null || visibility == aVisibilityFilter )
            {
                addCompositeNode( aCompositesNode, composite );
            }
        }
    }

    private void addCompositeNode(
        DefaultMutableTreeNode aCompositesNode,
        CompositeDetailDescriptor<CompositeDescriptor> aCompositeDetailDescriptor )
    {
        DefaultMutableTreeNode compositeNode = new DefaultMutableTreeNode( aCompositeDetailDescriptor );
        aCompositesNode.add( compositeNode );

        addMixinsNode( compositeNode, aCompositeDetailDescriptor );
        addMethodsNode( compositeNode, aCompositeDetailDescriptor );
    }

    private void addMixinsNode(
        DefaultMutableTreeNode aCompositeNode,
        CompositeDetailDescriptor<CompositeDescriptor> aCompositeDetailDescriptor )
    {
        DefaultMutableTreeNode mixinsNode = new DefaultMutableTreeNode( "mixins" );
        Iterable<MixinDetailDescriptor> mixins = aCompositeDetailDescriptor.mixins();
        for( MixinDetailDescriptor mixin : mixins )
        {
            DefaultMutableTreeNode mixinNode = new DefaultMutableTreeNode( mixin );
            mixinsNode.add( mixinNode );
        }
        addIfNotEmpty( aCompositeNode, mixinsNode );
    }

    private void addMethodsNode(
        DefaultMutableTreeNode aCompositeNode,
        CompositeDetailDescriptor<CompositeDescriptor> aCompositeDetailDescriptor )
    {
        DefaultMutableTreeNode methodsNode = new DefaultMutableTreeNode( "methods" );
        Iterable<CompositeMethodDetailDescriptor> methods = aCompositeDetailDescriptor.methods();
        for( CompositeMethodDetailDescriptor method : methods )
        {
            DefaultMutableTreeNode methodNode = new DefaultMutableTreeNode( method );
            methodsNode.add( methodNode );
        }
        addIfNotEmpty( aCompositeNode, methodsNode );
    }


    private void addObjectNodes(
        DefaultMutableTreeNode aObjectsNode,
        ModuleDetailDescriptor aModule,
        Visibility aVisibilityFilter )
    {
        Iterable<ObjectDetailDescriptor> objects = aModule.objects();
        for( ObjectDetailDescriptor object : objects )
        {
            ObjectDescriptor descriptor = object.descriptor();
            Visibility visibility = descriptor.visibility();
            if( aVisibilityFilter == null || visibility == aVisibilityFilter )
            {
                aObjectsNode.add( new DefaultMutableTreeNode( object ) );
            }
        }
    }
}
