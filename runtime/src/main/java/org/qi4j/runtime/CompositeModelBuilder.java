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

/**
 * TODO
 */
public final class CompositeModelBuilder
{
    ModifierModelBuilder modifierModelBuilder;
    MixinModelBuilder mixinModelBuilder;

    public CompositeModelBuilder( ModifierModelBuilder modifierModelBuilder, MixinModelBuilder mixinModelBuilder )
    {
        this.modifierModelBuilder = modifierModelBuilder;
        this.mixinModelBuilder = mixinModelBuilder;
    }

    public <T extends Composite> CompositeModel<T> getCompositeModel( Class<T> compositeClass )
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

        return new CompositeModel<T>( compositeClass, mixins, modifiers );
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
                modifierModels.add( modifierModelBuilder.getModifierModel( modifier, compositeType ) );
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
        return mixinModelBuilder.getMixinModel( aClass, compositeType );
    }

    private <T> ModifierModel<T> getModifierModel( Class<T> aClass, Class compositeType )
    {
        return modifierModelBuilder.getModifierModel( aClass, compositeType );
    }
}
