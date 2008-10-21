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
import org.qi4j.library.swing.visualizer.model.InjectableDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.MethodConcernDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.MethodConcernsDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.MethodConstraintDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.MethodConstraintsDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.MethodSideEffectDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.MethodSideEffectsDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.MixinDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ObjectDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ServiceDetailDescriptor;
import org.qi4j.spi.composite.CompositeDescriptor;

/**
 * TODO: localization
 *
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class TreeModelBuilder
{
    static final String NODE_NAME_SERVICES = "services";
    static final String NODE_NAME_ENTITIES = "entities";
    static final String NODE_NAME_COMPOSITES = "composites";
    static final String NODE_NAME_OBJECTS = "objects";
    static final String NODE_NAME_MIXINS = "mixins";
    static final String NODE_NAME_METHODS = "methods";
    static final String NODE_NAME_CONSTRUCTORS = "constructors";
    static final String NODE_NAME_INJECTED_FIELDS = "injected fields";
    static final String NODE_NAME_INJECTED_METHODS = "injected methods";
    static final String NODE_NAME_CONCERNS = "concerns";
    static final String NODE_NAME_CONSTRAINTS = "constraints";
    static final String NODE_NAME_SIDE_EFFECTS = "side effects";

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

        DefaultMutableTreeNode servicesNode = new DefaultMutableTreeNode( NODE_NAME_SERVICES );
        addServiceNodes( servicesNode, aModule );
        addIfNotEmpty( moduleNode, servicesNode );

        DefaultMutableTreeNode entities = new DefaultMutableTreeNode( NODE_NAME_ENTITIES );
        addEntityNodes( entities, aModule );
        addIfNotEmpty( moduleNode, entities );

        DefaultMutableTreeNode composites = new DefaultMutableTreeNode( NODE_NAME_COMPOSITES );
        addCompositeNodes( composites, aModule );
        addIfNotEmpty( moduleNode, composites );

        DefaultMutableTreeNode objects = new DefaultMutableTreeNode( NODE_NAME_OBJECTS );
        addObjectNodes( objects, aModule );
        addIfNotEmpty( moduleNode, objects );
    }

    private void addServiceNodes( DefaultMutableTreeNode aServicesNode, ModuleDetailDescriptor aModule )
    {
        Iterable<ServiceDetailDescriptor> services = aModule.services();
        for( ServiceDetailDescriptor service : services )
        {
            aServicesNode.add( new DefaultMutableTreeNode( service ) );
        }
    }

    private void addEntityNodes( DefaultMutableTreeNode aEntitiesNode, ModuleDetailDescriptor aModule )
    {
        Iterable<EntityDetailDescriptor> entities = aModule.entities();
        for( EntityDetailDescriptor entity : entities )
        {
            DefaultMutableTreeNode entityNode = new DefaultMutableTreeNode( entity );
            aEntitiesNode.add( entityNode );
        }
    }

    @SuppressWarnings( "unchecked" )
    private void addCompositeNodes( DefaultMutableTreeNode aCompositesNode, ModuleDetailDescriptor aModule )
    {
        Iterable<CompositeDetailDescriptor> composites = aModule.composites();
        for( CompositeDetailDescriptor composite : composites )
        {
            addCompositeNode( aCompositesNode, composite );
        }
    }

    private void addCompositeNode(
        DefaultMutableTreeNode aCompositesNode,
        CompositeDetailDescriptor<CompositeDescriptor> aDescriptor )
    {
        DefaultMutableTreeNode compositeNode = new DefaultMutableTreeNode( aDescriptor );
        aCompositesNode.add( compositeNode );

        DefaultMutableTreeNode mixinsNode = new DefaultMutableTreeNode( NODE_NAME_MIXINS );
        addMixinNodes( mixinsNode, aDescriptor );
        addIfNotEmpty( compositeNode, mixinsNode );

        DefaultMutableTreeNode methodsNode = new DefaultMutableTreeNode( NODE_NAME_METHODS );
        addMethodsNode( methodsNode, aDescriptor );
        addIfNotEmpty( compositeNode, methodsNode );
    }

    private void addMixinNodes(
        DefaultMutableTreeNode mixinsNode,
        CompositeDetailDescriptor<CompositeDescriptor> aCompositeDetailDescriptor )
    {
        Iterable<MixinDetailDescriptor> mixins = aCompositeDetailDescriptor.mixins();
        for( MixinDetailDescriptor mixin : mixins )
        {
            addInjectableNode( mixinsNode, mixin );
        }
    }

    private void addInjectableNode( DefaultMutableTreeNode aParentNode, InjectableDetailDescriptor aDescriptor )
    {
        DefaultMutableTreeNode injectableNode = new DefaultMutableTreeNode( aDescriptor );
        aParentNode.add( injectableNode );

        // Constructors
        // TODO: Localization
        DefaultMutableTreeNode constructorsNode = new DefaultMutableTreeNode( NODE_NAME_CONSTRUCTORS );
        addIterableItemNodes( constructorsNode, aDescriptor.constructors() );
        addIfNotEmpty( injectableNode, constructorsNode );

        // Injected fields
        // TODO: Localization
        DefaultMutableTreeNode fieldsNode = new DefaultMutableTreeNode( NODE_NAME_INJECTED_FIELDS );
        addIterableItemNodes( fieldsNode, aDescriptor.injectedFields() );
        addIfNotEmpty( injectableNode, fieldsNode );

        // Injected methods
        // TODO: Localization
        DefaultMutableTreeNode methodsNode = new DefaultMutableTreeNode( NODE_NAME_INJECTED_METHODS );
        addIterableItemNodes( methodsNode, aDescriptor.injectedMethods() );
        addIfNotEmpty( injectableNode, methodsNode );
    }

    private void addIterableItemNodes( DefaultMutableTreeNode aGroupNode, Iterable iterable )
    {
        for( Object item : iterable )
        {
            DefaultMutableTreeNode itemNode = new DefaultMutableTreeNode( item );
            aGroupNode.add( itemNode );
        }
    }

    private void addMethodsNode(
        DefaultMutableTreeNode methodsNode,
        CompositeDetailDescriptor<CompositeDescriptor> aCompositeDetailDescriptor )
    {
        Iterable<CompositeMethodDetailDescriptor> methods = aCompositeDetailDescriptor.methods();
        for( CompositeMethodDetailDescriptor method : methods )
        {
            addMethodNode( methodsNode, method );
        }
    }

    private void addMethodNode( DefaultMutableTreeNode aMethodsNode, CompositeMethodDetailDescriptor aMethod )
    {
        DefaultMutableTreeNode methodNode = new DefaultMutableTreeNode( aMethod );
        aMethodsNode.add( methodNode );

        // TODO: Localization
        DefaultMutableTreeNode concernsNode = new DefaultMutableTreeNode( NODE_NAME_CONCERNS );
        addConcernsNode( concernsNode, aMethod );
        addIfNotEmpty( methodNode, concernsNode );

        // TODO: Localization
        DefaultMutableTreeNode constraintsNode = new DefaultMutableTreeNode( NODE_NAME_CONSTRAINTS );
        addConstraintsNode( constraintsNode, aMethod );
        addIfNotEmpty( methodNode, constraintsNode );

        // TODO: Localization
        DefaultMutableTreeNode sideEffectsNode = new DefaultMutableTreeNode( NODE_NAME_SIDE_EFFECTS );
        addSideEffectsNode( sideEffectsNode, aMethod );
        addIfNotEmpty( methodNode, sideEffectsNode );
    }

    private void addConcernsNode( DefaultMutableTreeNode aConcernsNode, CompositeMethodDetailDescriptor aMethod )
    {
        MethodConcernsDetailDescriptor concernsDetailDescriptor = aMethod.concerns();
        if( concernsDetailDescriptor != null )
        {
            Iterable<MethodConcernDetailDescriptor> concerns = concernsDetailDescriptor.concerns();
            for( MethodConcernDetailDescriptor concern : concerns )
            {
                addInjectableNode( aConcernsNode, concern );
            }
        }
    }

    private void addConstraintsNode( DefaultMutableTreeNode aConstraintsNode, CompositeMethodDetailDescriptor aMethod )
    {
        MethodConstraintsDetailDescriptor constraintsDetailDescriptor = aMethod.constraints();
        if( constraintsDetailDescriptor != null )
        {
            Iterable<MethodConstraintDetailDescriptor> constraints = constraintsDetailDescriptor.constraints();
            for( MethodConstraintDetailDescriptor constraint : constraints )
            {
                DefaultMutableTreeNode constraintNode = new DefaultMutableTreeNode( constraint );

                aConstraintsNode.add( constraintNode );
            }
        }
    }

    private void addSideEffectsNode( DefaultMutableTreeNode aSideEffectsNode, CompositeMethodDetailDescriptor aMethod )
    {
        MethodSideEffectsDetailDescriptor sideEffectsDetailDescriptor = aMethod.sideEffects();
        if( sideEffectsDetailDescriptor != null )
        {
            Iterable<MethodSideEffectDetailDescriptor> sideEffects = sideEffectsDetailDescriptor.sideEffects();
            for( MethodSideEffectDetailDescriptor sideEffect : sideEffects )
            {
                addInjectableNode( aSideEffectsNode, sideEffect );
            }
        }
    }

    private void addObjectNodes( DefaultMutableTreeNode aObjectsNode, ModuleDetailDescriptor aModule )
    {
        Iterable<ObjectDetailDescriptor> objects = aModule.objects();
        for( ObjectDetailDescriptor object : objects )
        {
            addInjectableNode( aObjectsNode, object );
        }
    }
}
