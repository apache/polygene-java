/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.association;

import java.util.ArrayList;
import java.util.HashSet;
import org.qi4j.association.AbstractAssociation;
import org.qi4j.association.ListAssociation;
import org.qi4j.association.ManyAssociation;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.association.AssociationBinding;
import org.qi4j.spi.association.AssociationInstance;
import org.qi4j.spi.association.ListAssociationInstance;
import org.qi4j.spi.association.SetAssociationInstance;

/**
 * TODO
 */
public final class AssociationContext
{
    private AssociationBinding associationBinding;

    public AssociationContext( AssociationBinding associationBinding )
    {
        this.associationBinding = associationBinding;
    }

    public AssociationBinding getAssociationBinding()
    {
        return associationBinding;
    }

    public <T> AbstractAssociation newInstance( ModuleInstance moduleInstance, Object value )
    {
        try
        {
            Class associationType = associationBinding.getAssociationResolution().getAssociationModel().getAccessor().getReturnType();

            if( Composite.class.isAssignableFrom( associationType ) )
            {
                Class<? extends Composite> associationCompositeType = (Class<? extends Composite>) associationType;
                CompositeBuilder<? extends Composite> cb = moduleInstance.getStructureContext().getCompositeBuilderFactory().newCompositeBuilder( associationCompositeType );
                cb.use( value );
                cb.use( associationBinding );
                return AbstractAssociation.class.cast( cb.newInstance() );
            }
            else
            {
                AbstractAssociation instance;
                if( ListAssociation.class.isAssignableFrom( associationType ) )
                {
                    instance = new ListAssociationInstance<Object>( new ArrayList<Object>(), associationBinding );
                }
                else if( ManyAssociation.class.isAssignableFrom( associationType ) )
                {
                    instance = new SetAssociationInstance<Object>( new HashSet<Object>(), associationBinding );
                }
                else
                {
                    instance = new AssociationInstance<Object>( associationBinding, value );
                }
                return instance;
            }
        }
        catch( Exception e )
        {
            throw new InvalidAssociationException( "Could not instantiate association", e );
        }
    }
}
