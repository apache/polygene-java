/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.composite;

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.property.PropertyModel;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * Model for Transient Composites
 */
public class TransientModel
    extends CompositeModel
    implements TransientDescriptor
{
    public TransientModel( Iterable<Class<?>> types, final Visibility visibility,
                           final MetaInfo metaInfo,
                           final MixinsModel mixinsModel,
                           final StateModel stateModel,
                           final CompositeMethodsModel compositeMethodsModel
    )
    {
        super( types, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel );
    }

    public TransientInstance newInstance( ModuleInstance moduleInstance,
                                          UsesInstance uses,
                                          TransientStateInstance state
    )
    {
        Object[] mixins = mixinsModel.newMixinHolder();
        TransientInstance compositeInstance = new TransientInstance( this, moduleInstance, mixins, state );

        // Instantiate all mixins
        int i = 0;
        InjectionContext injectionContext = new InjectionContext( compositeInstance, uses, state );
        for( MixinModel mixinModel : mixinsModel.mixinModels() )
        {
            mixins[ i++ ] = mixinModel.newInstance( injectionContext );
        }

        // Return
        return compositeInstance;
    }

    public void checkConstraints( TransientStateInstance instanceState )
        throws ConstraintViolationException
    {
        for( PropertyModel propertyModel : stateModel.properties() )
        {
            propertyModel.checkConstraints( instanceState.<Object>propertyFor( propertyModel.accessor() ).get() );
        }
    }
}