/*  Copyright 2009 Tonny Kohar.
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
package org.qi4j.library.swing.envisage.model.util;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.library.swing.envisage.model.descriptor.*;
import org.qi4j.library.swing.envisage.util.TableRow;
import org.qi4j.spi.composite.DependencyDescriptor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
class ServiceUsageFinder
{
    protected ServiceDetailDescriptor descriptor;
    protected ArrayList<TableRow> rows;

    public List<TableRow> findServiceUsage( ServiceDetailDescriptor descriptor )
    {
        rows = new ArrayList<TableRow>();

        this.descriptor = descriptor;

        // traverse the appDescritor/model to find the usage
        ApplicationDetailDescriptor appDescriptor = descriptor.module().layer().application();
        collectUsage( appDescriptor );

        return rows;
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
            collectInTransients( descriptor.composites() );
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

    private void collectInTransients( Iterable<CompositeDetailDescriptor> iter )
    {
        for( CompositeDetailDescriptor descriptor : iter )
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
                if( dependencyDescriptor.injectionClass().equals( this.descriptor.descriptor().type() ) )
                {
                    used = true;
                }
                else
                {
                    // collect in injectedServices
                    for( String name : dependencyDescriptor.injectedServices() )
                    {
                        if( name.equals( this.descriptor.descriptor().identity() ) )
                        {
                            used = true;
                        }
                    }
                }

                if( used )
                {
                    TableRow row = new TableRow( 5 );
                    if( ownerDescriptor instanceof MixinDetailDescriptor )
                    {
                        MixinDetailDescriptor mixinDescriptor = (MixinDetailDescriptor) ownerDescriptor;
                        row.set( 0, mixinDescriptor.composite() );
                        row.set( 1, descriptorField );
                        row.set( 2, mixinDescriptor.composite().module() );
                        row.set( 3, mixinDescriptor.composite().module().layer() );
                    }
                    else
                    {
                        // assume ObjectDetailDescriptor
                        ObjectDetailDescriptor objectDescriptor = (ObjectDetailDescriptor) ownerDescriptor;
                        row.set( 0, objectDescriptor );
                        row.set( 1, descriptorField );
                        row.set( 2, objectDescriptor.module() );
                        row.set( 3, objectDescriptor.module().layer() );
                    }
                    rows.add( row );
                }
            }
        }
    }
}
