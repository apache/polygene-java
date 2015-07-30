/*
 * Copyright (c) 2009, Tonny Kohar. All Rights Reserved.
 * Copyright (c) 2014-2015, Paul Merlin. All Rights Reserved.
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
package org.apache.zest.tools.model.descriptor;

import java.util.LinkedList;
import java.util.List;
import org.apache.zest.functional.HierarchicalVisitor;
import org.apache.zest.functional.VisitableHierarchy;

import static org.apache.zest.api.util.NullArgumentException.validateNotNull;

/**
 * Imported Service Detail Descriptor.
 * <p>
 * Visitable hierarchy with Activators children.
 */
// TODO need to refactor later, but wait until Qi4J core/spi have proper and consistent API for ImportedService.
public class ImportedServiceDetailDescriptor
    extends CompositeDetailDescriptor<ImportedServiceCompositeDescriptor>
    implements ActivateeDetailDescriptor, VisitableHierarchy<Object, Object>
{
    private final List<ActivatorDetailDescriptor> activators = new LinkedList<>();

    ImportedServiceDetailDescriptor( ImportedServiceCompositeDescriptor descriptor )
    {
        super( descriptor );
    }

    @Override
    public Iterable<ActivatorDetailDescriptor> activators()
    {
        return activators;
    }

    final void addActivator( ActivatorDetailDescriptor descriptor )
    {
        validateNotNull( "ActivatorDetailDescriptor", descriptor );
        descriptor.setImportedService( this );
        activators.add( descriptor );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            for( ActivatorDetailDescriptor activator : activators )
            {
                if( !activator.accept( visitor ) )
                {
                    break;
                }
            }
        }
        return visitor.visitLeave( this );
    }
}
