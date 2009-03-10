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
package org.qi4j.library.swing.envisage.model.descriptor;

import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.composite.CompositeMethodDescriptor;
import org.qi4j.spi.composite.ConstructorDescriptor;
import org.qi4j.spi.composite.InjectedFieldDescriptor;
import org.qi4j.spi.composite.InjectedMethodDescriptor;
import org.qi4j.spi.composite.InjectedParametersDescriptor;
import org.qi4j.spi.concern.MethodConcernDescriptor;
import org.qi4j.spi.concern.MethodConcernsDescriptor;
import org.qi4j.spi.constraint.ConstraintDescriptor;
import org.qi4j.spi.constraint.MethodConstraintsDescriptor;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.mixin.MixinDescriptor;
import org.qi4j.spi.object.ObjectDescriptor;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.sideeffect.MethodSideEffectDescriptor;
import org.qi4j.spi.sideeffect.MethodSideEffectsDescriptor;
import org.qi4j.spi.structure.ApplicationDescriptor;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.spi.structure.DescriptorVisitor;
import org.qi4j.spi.structure.LayerDescriptor;
import org.qi4j.spi.structure.ModuleDescriptor;
import org.qi4j.spi.structure.UsedLayersDescriptor;
import org.qi4j.spi.value.ValueDescriptor;

/**
 * @author Sonny Gill
 * @author edward.yakop@gmail.com
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 * @since 0.5
 */
public final class ApplicationDetailDescriptorBuilder
{
    private ApplicationDetailDescriptorBuilder()
    {
    }

    public static ApplicationDetailDescriptor createApplicationDetailDescriptor( ApplicationSPI anApplication )
    {
        ApplicationDescriptorVisitor visitor = new ApplicationDescriptorVisitor();
        anApplication.visitDescriptor( visitor );

        return visitor.applicationDescriptor;
    }

    static final class ApplicationDescriptorVisitor extends DescriptorVisitor
    {
        // Temp: application
        private ApplicationDetailDescriptor applicationDescriptor;

        // Temp: Layer variables
        private LayerDetailDescriptor currLayerDescriptor;
        // Cache to lookup layer descriptor -> node
        private final Map<LayerDescriptor, LayerDetailDescriptor> layerDescToDetail;

        // Temp: current module
        private ModuleDetailDescriptor currModuleDescriptor;

        // Temp: current composite
        private CompositeDetailDescriptor currCompositeDescriptor;

        // Temp: curr mixin
        private MixinDetailDescriptor currMixinDescriptor;

        // Temp: current constructor
        private ConstructorDetailDescriptor currConstructorDescriptor;

        // Temp: current injected method
        private InjectedMethodDetailDescriptor currInjectedMethodDescriptor;

        // Temp: current composite method
        private CompositeMethodDetailDescriptor currMethodDesciptor;

        // Temp: current method constraints
        private MethodConstraintsDetailDescriptor currMethodConstraintsDescriptor;

        // Temp: current object
        private ObjectDetailDescriptor currObjectDescriptor;

        // Temp: current method concerns
        private MethodConcernsDetailDescriptor currMethodConcernsDescriptor;

        // Temp: current method concern
        private MethodConcernDetailDescriptor currMethodConcernDescriptor;

        // Temp: current method side effects
        private MethodSideEffectsDetailDescriptor currMethodSideEffectsDescriptor;

        // Temp: current side effect
        private MethodSideEffectDetailDescriptor currMethodSideEffectDescriptor;

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
            ServiceDetailDescriptor descriptor = new ServiceDetailDescriptor( aDescriptor );
            currModuleDescriptor.addService( descriptor );
            currCompositeDescriptor = descriptor;
        }

        @Override
        public final void visit( ImportedServiceDescriptor aDescriptor )
        {
            ImportedServiceDetailDescriptor descriptor = new ImportedServiceDetailDescriptor( new ImportedServiceCompositeDescriptor( aDescriptor ) );
            currModuleDescriptor.addImportedService( descriptor );
            currCompositeDescriptor = descriptor;
        }

        @Override
        public final void visit( EntityDescriptor aDescriptor )
        {
            EntityDetailDescriptor descriptor = new EntityDetailDescriptor( aDescriptor );
            currModuleDescriptor.addEntity( descriptor );
            currCompositeDescriptor = descriptor;
        }

        @Override
        public final void visit( ValueDescriptor aDescriptor )
        {
            ValueDetailDescriptor descriptor = new ValueDetailDescriptor( aDescriptor );
            currModuleDescriptor.addValue( descriptor );
            currCompositeDescriptor = descriptor;
        }

        @Override
        public final void visit( CompositeDescriptor aDescriptor )
        {
            // Commented out by efy: To prevent NPE
            /*if( ServiceComposite.class.isAssignableFrom( aDescriptor.type() ) )
            {
                return; // Skip services
            }*/

            currCompositeDescriptor = new CompositeDetailDescriptor<CompositeDescriptor>( aDescriptor );
            currModuleDescriptor.addComposite( currCompositeDescriptor );
        }

        @Override
        public final void visit( CompositeMethodDescriptor aDescriptor )
        {
            if( currCompositeDescriptor == null )
            {
                // Service via CompositeDescriptor in progress )
                return;
            }
            currMethodDesciptor = new CompositeMethodDetailDescriptor( aDescriptor );
            currCompositeDescriptor.addMethod( currMethodDesciptor );
        }

        @Override
        public final void visit( MethodConstraintsDescriptor aDescriptor )
        {
            if( currCompositeDescriptor == null )
            {
                // Service via CompositeDescriptor in progress )
                return;
            }
            currMethodConstraintsDescriptor =
                new MethodConstraintsDetailDescriptor( aDescriptor );
            currMethodDesciptor.setConstraints( currMethodConstraintsDescriptor );
        }

        @Override
        public final void visit( ConstraintDescriptor aDescriptor )
        {
            if( currCompositeDescriptor == null )
            {
                // Service via CompositeDescriptor in progress )
                return;
            }
            MethodConstraintDetailDescriptor detailDescriptor = new MethodConstraintDetailDescriptor( aDescriptor );
            currMethodConstraintsDescriptor.addConstraint( detailDescriptor );
        }

        @Override
        public final void visit( MethodConcernsDescriptor aDescriptor )
        {
            if( currCompositeDescriptor == null )
            {
                // Service via CompositeDescriptor in progress )
                return;
            }
            currMethodConcernsDescriptor = new MethodConcernsDetailDescriptor( aDescriptor );
            currMethodDesciptor.setConcerns( currMethodConcernsDescriptor );
        }

        @Override
        public final void visit( MethodConcernDescriptor aDescriptor )
        {
            if( currCompositeDescriptor == null )
            {
                // Service via CompositeDescriptor in progress )
                return;
            }
            resetInjectableRelatedVariables();

            currMethodConcernDescriptor = new MethodConcernDetailDescriptor( aDescriptor );
            currMethodConcernsDescriptor.addConcern( currMethodConcernDescriptor );
        }

        private void resetInjectableRelatedVariables()
        {
            currMixinDescriptor = null;
            currObjectDescriptor = null;
            currMethodConcernDescriptor = null;
            currMethodSideEffectDescriptor = null;
        }

        @Override
        public final void visit( MethodSideEffectsDescriptor aDescriptor )
        {
            if( currCompositeDescriptor == null )
            {
                // Service via CompositeDescriptor in progress )
                return;
            }
            currMethodSideEffectsDescriptor = new MethodSideEffectsDetailDescriptor( aDescriptor );
            currMethodDesciptor.setSideEffects( currMethodSideEffectsDescriptor );
        }

        @Override
        public final void visit( MethodSideEffectDescriptor aDescriptor )
        {
            if( currCompositeDescriptor == null )
            {
                // Service via CompositeDescriptor in progress )
                return;
            }
            resetInjectableRelatedVariables();

            currMethodSideEffectDescriptor = new MethodSideEffectDetailDescriptor( aDescriptor );
            currMethodSideEffectsDescriptor.addSideEffect( currMethodSideEffectDescriptor );
        }

        @Override
        public final void visit( MixinDescriptor aDescriptor )
        {
            if( currCompositeDescriptor == null )
            {
                // Service via CompositeDescriptor in progress )
                return;
            }
            resetInjectableRelatedVariables();

            currMixinDescriptor = new MixinDetailDescriptor( aDescriptor );
            currCompositeDescriptor.addMixin( currMixinDescriptor );
        }

        @Override
        public final void visit( ObjectDescriptor aDescriptor )
        {
            resetInjectableRelatedVariables();

            currObjectDescriptor = new ObjectDetailDescriptor( aDescriptor );
            currModuleDescriptor.addObject( currObjectDescriptor );
        }


        @Override
        public void visit( ConstructorDescriptor aDescriptor )
        {
            if( currCompositeDescriptor == null )
            {
                // Service via CompositeDescriptor in progress )
                return;
            }
            currConstructorDescriptor = new ConstructorDetailDescriptor( aDescriptor );
            currInjectedMethodDescriptor = null;

            // Invoked for mixin and object
            if( currMixinDescriptor != null )
            {
                currMixinDescriptor.addConstructor( currConstructorDescriptor );
            }
            else if( currObjectDescriptor != null )
            {
                currObjectDescriptor.addConstructor( currConstructorDescriptor );
            }
            else if( currMethodConcernDescriptor != null )
            {
                currMethodConcernDescriptor.addConstructor( currConstructorDescriptor );
            }
            else if( currMethodSideEffectDescriptor != null )
            {
                currMethodSideEffectDescriptor.addConstructor( currConstructorDescriptor );
            }
            else
            {
                throw new IllegalStateException(
                    "ConstructorDescriptor is only valid for mixin and object."
                );
            }
        }

        @Override
        public void visit( InjectedParametersDescriptor aDescriptor )
        {
            if( currCompositeDescriptor == null )
            {
                // Service via CompositeDescriptor in progress )
                return;
            }
            InjectedParametersDetailDescriptor detailDescriptor = new InjectedParametersDetailDescriptor( aDescriptor );

            // Invoked for constructor and injected method
            if( currConstructorDescriptor != null )
            {
                currConstructorDescriptor.setInjectedParameter( detailDescriptor );
            }
            else if( currInjectedMethodDescriptor != null )
            {
                currInjectedMethodDescriptor.setInjectedParameter( detailDescriptor );
            }
            else
            {
                throw new IllegalStateException(
                    "InjectedParametersDescriptor is only valid for constructor and injector method descriptor."
                );
            }
        }

        @Override
        public void visit( InjectedMethodDescriptor aDescriptor )
        {
            if( currCompositeDescriptor == null )
            {
                // Service via CompositeDescriptor in progress )
                return;
            }
            // Invoked for mixin and object
            currInjectedMethodDescriptor = new InjectedMethodDetailDescriptor( aDescriptor );
            currConstructorDescriptor = null;

            // Invoked for mixin and object
            if( currMixinDescriptor != null )
            {
                currMixinDescriptor.addInjectedMethod( currInjectedMethodDescriptor );
            }
            else if( currObjectDescriptor != null )
            {
                currObjectDescriptor.addInjectedMethod( currInjectedMethodDescriptor );
            }
            else if( currMethodConcernDescriptor != null )
            {
                currMethodConcernDescriptor.addInjectedMethod( currInjectedMethodDescriptor );
            }
            else if( currMethodSideEffectDescriptor != null )
            {
                currMethodSideEffectDescriptor.addInjectedMethod( currInjectedMethodDescriptor );
            }
            else
            {
                throw new IllegalStateException(
                    "InjectedMethodDescriptor is only valid for mixin and object."
                );
            }
        }

        @Override
        public void visit( InjectedFieldDescriptor aDescriptor )
        {
            if( currCompositeDescriptor == null )
            {
                // Service via CompositeDescriptor in progress )
                return;
            }
            InjectedFieldDetailDescriptor detailDescriptor = new InjectedFieldDetailDescriptor( aDescriptor );

            // Invoked for mixin and object
            if( currMixinDescriptor != null )
            {
                currMixinDescriptor.addInjectedField( detailDescriptor );
            }
            else if( currObjectDescriptor != null )
            {
                currObjectDescriptor.addInjectedField( detailDescriptor );
            }
            else if( currMethodConcernDescriptor != null )
            {
                currMethodConcernDescriptor.addInjectedField( detailDescriptor );
            }
            else if( currMethodSideEffectDescriptor != null )
            {
                currMethodSideEffectDescriptor.addInjectedField( detailDescriptor );
            }
            else
            {
                throw new IllegalStateException(
                    "InjectedFieldDescriptor is only valid for mixin and object."
                );
            }
        }
    }
}