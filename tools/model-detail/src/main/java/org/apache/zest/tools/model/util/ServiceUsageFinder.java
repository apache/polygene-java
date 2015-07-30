/*
 * Copyright (c) 2009, Tonny Kohar. All Rights Reserved.
 * Copyright (c) 2012-2015, Paul Merlin. All Rights Reserved.
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
package org.apache.zest.tools.model.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import org.apache.zest.api.composite.DependencyDescriptor;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Uses;
import org.apache.zest.tools.model.descriptor.ApplicationDetailDescriptor;
import org.apache.zest.tools.model.descriptor.EntityDetailDescriptor;
import org.apache.zest.tools.model.descriptor.InjectedFieldDetailDescriptor;
import org.apache.zest.tools.model.descriptor.LayerDetailDescriptor;
import org.apache.zest.tools.model.descriptor.MixinDetailDescriptor;
import org.apache.zest.tools.model.descriptor.ModuleDetailDescriptor;
import org.apache.zest.tools.model.descriptor.ObjectDetailDescriptor;
import org.apache.zest.tools.model.descriptor.ServiceDetailDescriptor;
import org.apache.zest.tools.model.descriptor.ServiceUsage;
import org.apache.zest.tools.model.descriptor.TransientDetailDescriptor;
import org.apache.zest.tools.model.descriptor.ValueDetailDescriptor;

import static org.apache.zest.functional.Iterables.first;

/* package */ class ServiceUsageFinder
{
    private ServiceDetailDescriptor descriptor;
    private ArrayList<ServiceUsage> usages;

    public List<ServiceUsage> findServiceUsage( ServiceDetailDescriptor descriptor )
    {
        usages = new ArrayList<>();

        this.descriptor = descriptor;

        // traverse the appDescritor/model to find the usage
        ApplicationDetailDescriptor appDescriptor = descriptor.module().layer().application();
        collectUsage( appDescriptor );

        return usages;
    }

    private void collectUsage( ApplicationDetailDescriptor descriptor )
    {
        for( LayerDetailDescriptor childDescriptor : descriptor.layers() )
        {
            collectInModules( childDescriptor.modules() );
        }
    }

    private void collectInModules( Iterable<ModuleDetailDescriptor> iter )
    {
        for( ModuleDetailDescriptor descriptor : iter )
        {
            collectInServices( descriptor.services() );
            collectInEntities( descriptor.entities() );
            collectInValues( descriptor.values() );
            collectInTransients( descriptor.transients() );
            collectInObjects( descriptor.objects() );
        }
    }

    private void collectInServices( Iterable<ServiceDetailDescriptor> iter )
    {
        for( ServiceDetailDescriptor descriptor : iter )
        {
            if( descriptor.equals( this.descriptor ) )
            {
                continue;
            }
            collectInMixin( descriptor.mixins() );
        }
    }

    private void collectInEntities( Iterable<EntityDetailDescriptor> iter )
    {
        for( EntityDetailDescriptor descriptor : iter )
        {
            collectInMixin( descriptor.mixins() );
        }
    }

    private void collectInValues( Iterable<ValueDetailDescriptor> iter )
    {
        for( ValueDetailDescriptor descriptor : iter )
        {
            collectInMixin( descriptor.mixins() );
        }
    }

    private void collectInTransients( Iterable<TransientDetailDescriptor> iter )
    {
        for( TransientDetailDescriptor descriptor : iter )
        {
            collectInMixin( descriptor.mixins() );
        }
    }

    private void collectInObjects( Iterable<ObjectDetailDescriptor> iter )
    {
        for( ObjectDetailDescriptor descriptor : iter )
        {
            collectInInjectedField( descriptor.injectedFields(), descriptor );
        }
    }

    private void collectInMixin( Iterable<MixinDetailDescriptor> iter )
    {
        for( MixinDetailDescriptor descriptor : iter )
        {
            collectInInjectedField( descriptor.injectedFields(), descriptor );
        }
    }

    private void collectInInjectedField( Iterable<InjectedFieldDetailDescriptor> iter, Object ownerDescriptor )
    {
        for( InjectedFieldDetailDescriptor descriptorField : iter )
        {
            DependencyDescriptor dependencyDescriptor = descriptorField.descriptor().dependency();
            Annotation annotation = dependencyDescriptor.injectionAnnotation();

            Class<? extends Annotation> clazz = annotation.annotationType();
            if( Uses.class.equals( clazz ) || Service.class.equals( clazz ) )
            {
                boolean used = false;
                if( dependencyDescriptor.injectionType().equals( first( this.descriptor.descriptor().types() ) ) )
                {
                    ServiceUsage usage;
                    if( ownerDescriptor instanceof MixinDetailDescriptor )
                    {
                        MixinDetailDescriptor mixinDescriptor = (MixinDetailDescriptor) ownerDescriptor;
                        usage = new ServiceUsage( mixinDescriptor.composite(), descriptorField, mixinDescriptor.composite().module(), mixinDescriptor.composite().module().layer() );
                    }
                    else
                    {
                        // assume ObjectDetailDescriptor
                        ObjectDetailDescriptor objectDescriptor = (ObjectDetailDescriptor) ownerDescriptor;
                        usage = new ServiceUsage( objectDescriptor, descriptorField, objectDescriptor.module(), objectDescriptor.module().layer() );
                    }
                    usages.add( usage );
                }
            }
        }
    }
}
