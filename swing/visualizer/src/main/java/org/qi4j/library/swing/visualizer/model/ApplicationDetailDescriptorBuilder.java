/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008, Sonny Gill. All Rights Reserved.
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.library.swing.visualizer.model;

import java.util.HashMap;
import java.util.Map;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.composite.CompositeMethodDescriptor;
import org.qi4j.spi.composite.MethodConcernDescriptor;
import org.qi4j.spi.composite.MethodConstraintsDescriptor;
import org.qi4j.spi.composite.MethodSideEffectDescriptor;
import org.qi4j.spi.composite.MixinDescriptor;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.object.ObjectDescriptor;
import org.qi4j.spi.structure.ApplicationDescriptor;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.spi.structure.DescriptorVisitor;
import org.qi4j.spi.structure.LayerDescriptor;
import org.qi4j.spi.structure.ModuleDescriptor;
import org.qi4j.spi.structure.UsedLayersDescriptor;

/**
 * @author Sonny Gill
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class ApplicationDetailDescriptorBuilder
{
    public ApplicationDetailDescriptor createApplicationDetailDescriptor( ApplicationSPI anApplication )
    {
        ApplicationDescriptorVisitor visitor = new ApplicationDescriptorVisitor();
        anApplication.visitDescriptor( visitor );

        return visitor.applicationDescriptor;
    }

    static final class ApplicationDescriptorVisitor extends DescriptorVisitor
    {
        // Temp application descriptor
        private ApplicationDetailDescriptor applicationDescriptor;

        // Temp Layer variables
        private LayerDetailDescriptor currLayerDescriptor;
        // Cache to lookup layer descriptor -> node
        private final Map<LayerDescriptor, LayerDetailDescriptor> layerDescToDetail;

        // Module related temp variables
        private ModuleDetailDescriptor currModuleDescriptor;

        // Cache of current composite
        private CompositeDetailDescriptor currCompositeDescriptor;

        // Cache of current composite method descriptor
        private CompositeMethodDetailDescriptor currMethodDesciptor;

        private ApplicationDescriptorVisitor()
        {
            layerDescToDetail = new HashMap<LayerDescriptor, LayerDetailDescriptor>();
        }

        @Override
        public final void visit( ApplicationDescriptor aDescriptor )
        {
            applicationDescriptor = new ApplicationDetailDescriptor( aDescriptor );
        }

        @Override
        public final void visit( LayerDescriptor layerDescriptor )
        {
            currLayerDescriptor = getLayerDetailDescriptor( layerDescriptor );
            applicationDescriptor.addLayer( currLayerDescriptor );

            UsedLayersDescriptor usedLayesDescriptor = layerDescriptor.usedLayers();
            Iterable<? extends LayerDescriptor> usedLayers = usedLayesDescriptor.layers();
            for( LayerDescriptor usedLayer : usedLayers )
            {
                LayerDetailDescriptor usedLayerDetailDesc = getLayerDetailDescriptor( usedLayer );
                currLayerDescriptor.addUsedLayer( usedLayerDetailDesc );
            }
        }

        private LayerDetailDescriptor getLayerDetailDescriptor( LayerDescriptor aDescriptor )
        {
            LayerDetailDescriptor detailDescriptor = layerDescToDetail.get( aDescriptor );
            if( detailDescriptor == null )
            {
                detailDescriptor = new LayerDetailDescriptor( aDescriptor );
                layerDescToDetail.put( aDescriptor, detailDescriptor );
            }

            return detailDescriptor;
        }

        @Override
        public final void visit( ModuleDescriptor aDescriptor )
        {
            currModuleDescriptor = new ModuleDetailDescriptor( aDescriptor );
            currLayerDescriptor.addModule( currModuleDescriptor );
        }

        @Override
        public final void visit( ServiceDescriptor aDescriptor )
        {
            currModuleDescriptor.addService( aDescriptor );
        }

        @Override
        public final void visit( EntityDescriptor aDescriptor )
        {
            EntityDetailDescriptor descriptor = new EntityDetailDescriptor( aDescriptor );
            currModuleDescriptor.addEntity( descriptor );
            currCompositeDescriptor = descriptor;
        }

        @Override
        public final void visit( CompositeDescriptor aDescriptor )
        {
            currCompositeDescriptor = new CompositeDetailDescriptor<CompositeDescriptor>( aDescriptor );
            currModuleDescriptor.addComposite( currCompositeDescriptor );
        }

        @Override
        public final void visit( CompositeMethodDescriptor aDescriptor )
        {
            currMethodDesciptor = new CompositeMethodDetailDescriptor( aDescriptor );
            currCompositeDescriptor.addMethod( currMethodDesciptor );
        }

        @Override
        public final void visit( MethodConstraintsDescriptor methodConstraintsDescriptor )
        {
            currMethodDesciptor.addConstraint( methodConstraintsDescriptor );
        }

        @Override
        public final void visit( MethodConcernDescriptor methodConcernDescriptor )
        {
            currMethodDesciptor.addConcern( methodConcernDescriptor );
        }

        @Override
        public final void visit( MethodSideEffectDescriptor methodSideEffectDescriptor )
        {
            currMethodDesciptor.addSideEffect( methodSideEffectDescriptor );
        }

        @Override
        public final void visit( MixinDescriptor mixinDescriptor )
        {
            currCompositeDescriptor.addMixin( mixinDescriptor );
        }

        @Override
        public final void visit( ObjectDescriptor aDescriptor )
        {
            currModuleDescriptor.addObject( aDescriptor );
        }
    }
}
