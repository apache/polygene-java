package org.qi4j.runtime;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.api.model.ConstructorDependency;
import org.qi4j.api.model.FieldDependency;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.MethodDependency;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.api.model.NullArgumentException;

/**
 * TODO
 */
public final class MixinModelBuilder
    extends FragmentModelBuilder
{
    private ModifierModelBuilder modifierModelBuilder;

    public MixinModelBuilder( ModifierModelBuilder modifierModelBuilder )
    {
        this.modifierModelBuilder = modifierModelBuilder;
    }

    public <T> MixinModel<T> getMixinModel( Class<T> mixinClass, Class compositeType )
        throws NullArgumentException, InvalidCompositeException
    {
        List<ConstructorDependency> constructorDependencies = new ArrayList<ConstructorDependency>();
        getConstructorDependencies( mixinClass, compositeType, constructorDependencies );
        List<FieldDependency> fieldDependencies = new ArrayList<FieldDependency>();
        getFieldDependencies( mixinClass, compositeType, fieldDependencies );
        List<MethodDependency> methodDependencies = new ArrayList<MethodDependency>();
        getMethodDependencies( mixinClass, compositeType, methodDependencies );

        Class appliesTo = getAppliesTo( mixinClass );

        List<ModifierModel> modifiers = getModifiers( mixinClass, compositeType );

        MixinModel<T> model = new MixinModel<T>( mixinClass, constructorDependencies, fieldDependencies, methodDependencies, appliesTo, modifiers );
        return model;
    }

    private List<ModifierModel> getModifiers( Class<?> aClass, Class compositeType )
    {
        List<ModifierModel> modifiers = new ArrayList<ModifierModel>();
        ModifiedBy modifiedBy = aClass.getAnnotation( ModifiedBy.class );
        if( modifiedBy != null )
        {
            for( Class modifier : modifiedBy.value() )
            {
                modifiers.add( modifierModelBuilder.getModifierModel( modifier, compositeType ) );
            }
        }

        // Check superclass
        if( !aClass.isInterface() && aClass != Object.class )
        {
            List<ModifierModel> superModifiers = getModifiers( aClass.getSuperclass(), compositeType );
            modifiers.addAll( superModifiers );
        }

        return modifiers;
    }
}