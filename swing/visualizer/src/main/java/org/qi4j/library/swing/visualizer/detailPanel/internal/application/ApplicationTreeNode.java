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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.visualizer.model.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ModuleDetailDescriptor;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.object.ObjectDescriptor;
import org.qi4j.spi.structure.ApplicationDescriptor;
import org.qi4j.structure.Visibility;
import static org.qi4j.structure.Visibility.application;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class ApplicationTreeNode extends DefaultMutableTreeNode
{
    private static final long serialVersionUID = 1L;

    ApplicationTreeNode( ApplicationDetailDescriptor aDetailDescriptor )
        throws IllegalArgumentException
    {
        super( aDetailDescriptor );

        validateNotNull( "aDetailDescriptor", aDetailDescriptor );

        ApplicationDescriptor descriptor = aDetailDescriptor.descriptor();
        setUserObject( descriptor.name() );

        populate( aDetailDescriptor );
    }

    private void populate( ApplicationDetailDescriptor aDetailDescriptor )
    {
        DefaultMutableTreeNode servicesNode = new DefaultMutableTreeNode( "services" );
        DefaultMutableTreeNode entities = new DefaultMutableTreeNode( "entities" );
        DefaultMutableTreeNode composites = new DefaultMutableTreeNode( "composites" );
        DefaultMutableTreeNode objects = new DefaultMutableTreeNode( "objects" );

        Iterable<LayerDetailDescriptor> layers = aDetailDescriptor.layers();
        for( LayerDetailDescriptor layer : layers )
        {
            Iterable<ModuleDetailDescriptor> modules = layer.modules();
            for( ModuleDetailDescriptor module : modules )
            {
                addServiceNodes( servicesNode, module );
                addEntityNodes( entities, module );
                addCompositeNodes( composites, module );
                addObjectNodes( objects, module );
            }
        }

        addIfNotEmpty( servicesNode );
        addIfNotEmpty( entities );
        addIfNotEmpty( composites );
        addIfNotEmpty( objects );
    }

    private void addServiceNodes( DefaultMutableTreeNode aServicesNode, ModuleDetailDescriptor aModule )
    {
        Iterable<ServiceDescriptor> services = aModule.services();
        for( ServiceDescriptor service : services )
        {
            Visibility visibility = service.visibility();
            if( visibility == application )
            {
                aServicesNode.add( new DefaultMutableTreeNode( service ) );
            }
        }
    }

    private void addEntityNodes( DefaultMutableTreeNode aEntitiesNode, ModuleDetailDescriptor aModule )
    {
        Iterable<EntityDetailDescriptor> entities = aModule.entities();
        for( EntityDetailDescriptor entity : entities )
        {
            EntityDescriptor entityDesc = entity.descriptor();
            Visibility visibility = entityDesc.visibility();
            if( visibility == application )
            {
                aEntitiesNode.add( new DefaultMutableTreeNode( entity ) );
            }
        }
    }

    private void addCompositeNodes( DefaultMutableTreeNode aCompositesNode, ModuleDetailDescriptor aModule )
    {
        Iterable<CompositeDetailDescriptor> composites = aModule.composites();
        for( CompositeDetailDescriptor composite : composites )
        {
            CompositeDescriptor compDesc = composite.descriptor();
            Visibility visibility = compDesc.visibility();
            if( visibility == application )
            {
                aCompositesNode.add( new DefaultMutableTreeNode( composite ) );
            }
        }
    }

    private void addObjectNodes( DefaultMutableTreeNode aObjectsNode, ModuleDetailDescriptor aModule )
    {
        Iterable<ObjectDescriptor> objects = aModule.objects();
        for( ObjectDescriptor object : objects )
        {
            Visibility visibility = object.visibility();
            if( visibility == application )
            {
                aObjectsNode.add( new DefaultMutableTreeNode( object ) );
            }
        }
    }

    private void addIfNotEmpty( MutableTreeNode aNode )
    {
        int numberOfChildren = aNode.getChildCount();
        if( numberOfChildren > 0 )
        {
            add( aNode );
        }
    }
}
