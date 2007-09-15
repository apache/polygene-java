/*  Copyright 2007 Niclas Hedhman.
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
package org.qi4j.runtime;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.Composite;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.api.model.NullArgumentException;
import org.qi4j.api.persistence.EntityComposite;
import org.qi4j.api.persistence.Identity;
import org.qi4j.runtime.persistence.EntityImpl;

public class CompositeModelFactory
{
    private ModifierModelFactory modifierModelFactory;
    private MixinModelFactory mixinModelFactory;

    public CompositeModelFactory()
    {
        modifierModelFactory = new ModifierModelFactory();
        mixinModelFactory = new MixinModelFactory( modifierModelFactory );
    }

    public CompositeModelFactory( ModifierModelFactory modifierModelFactory, MixinModelFactory mixinModelFactory )
    {
        this.modifierModelFactory = modifierModelFactory;
        this.mixinModelFactory = mixinModelFactory;
    }

    public <T extends Composite> CompositeModel<T> newCompositeModel( Class<T> compositeClass )
        throws NullArgumentException, InvalidCompositeException
    {
        validateClass( compositeClass );

        // Find mixins
        List<MixinModel> mixins = findMixins( compositeClass, compositeClass );

        // Standard mixins
        mixins.add( getMixinModel( CompositeImpl.class, compositeClass ) );
        mixins.add( getMixinModel( LifecycleImpl.class, compositeClass ) );

        if( Identity.class.isAssignableFrom( compositeClass ) )
        {
            mixins.add( getMixinModel( IdentityImpl.class, compositeClass ) );
        }
        if( EntityComposite.class.isAssignableFrom( compositeClass ) )
        {
            mixins.add( getMixinModel( EntityImpl.class, compositeClass ) );
        }

        // Find modifiers
        List<ModifierModel> modifiers = findModifiers( compositeClass, compositeClass );
        modifiers.add( getModifierModel( CompositeServicesModifier.class, compositeClass ) );

        CompositeModel model = new CompositeModel<T>( compositeClass, mixins, modifiers );
        return model;
    }

    private void validateClass( Class compositeClass )
        throws NullArgumentException, InvalidCompositeException
    {
        if( !compositeClass.isInterface() )
        {
            String message = compositeClass.getName() + " is not an interface.";
            throw new InvalidCompositeException( message, compositeClass );
        }

        if( !Composite.class.isAssignableFrom( compositeClass ) )
        {
            String message = compositeClass.getName() + " does not extend from " + Composite.class.getName();
            throw new InvalidCompositeException( message, compositeClass );
        }
    }

    private List<MixinModel> findMixins( Class aType, Class compositeType )
    {
        List<MixinModel> mixinModels = new ArrayList<MixinModel>();

        ImplementedBy impls = (ImplementedBy) aType.getAnnotation( ImplementedBy.class );
        if( impls != null )
        {
            for( Class impl : impls.value() )
            {
                mixinModels.add( getMixinModel( impl, compositeType ) );
            }
        }

        // Check subinterfaces
        Class[] subTypes = aType.getInterfaces();
        for( Class subType : subTypes )
        {
            mixinModels.addAll( findMixins( subType, compositeType ) );
        }

        return mixinModels;
    }

    private List<ModifierModel> findModifiers( Class aClass, Class compositeType )
    {
        List<ModifierModel> modifierModels = new ArrayList<ModifierModel>();

        ModifiedBy modifiedBy = (ModifiedBy) aClass.getAnnotation( ModifiedBy.class );
        if( modifiedBy != null )
        {
            for( Class<? extends Object> modifier : modifiedBy.value() )
            {
                modifierModels.add( modifierModelFactory.newModifierModel( modifier, compositeType ) );
            }
        }

        // Check subinterfaces
        Class[] subTypes = aClass.getInterfaces();
        for( Class subType : subTypes )
        {
            modifierModels.addAll( findModifiers( subType, compositeType ) );
        }

        return modifierModels;
    }

    private <T> MixinModel<T> getMixinModel( Class<T> aClass, Class compositeType )
    {
        return mixinModelFactory.getMixinModel( aClass, compositeType );
    }

    private <T> ModifierModel<T> getModifierModel( Class<T> aClass, Class compositeType )
    {
        return modifierModelFactory.newModifierModel( aClass, compositeType );
    }
}
