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
package org.qi4j.runtime.composite;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Concerns;
import org.qi4j.composite.Constraint;
import org.qi4j.composite.Constraints;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.NullArgumentException;
import org.qi4j.composite.SideEffects;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.association.AbstractAssociation;
import org.qi4j.property.Property;
import org.qi4j.runtime.entity.EntityMixin;
import org.qi4j.spi.composite.CompositeMethodModel;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.composite.ConcernModel;
import org.qi4j.spi.composite.ConstraintModel;
import org.qi4j.spi.composite.ConstructorModel;
import org.qi4j.spi.composite.FieldModel;
import org.qi4j.spi.composite.FragmentModel;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.composite.MethodModel;
import org.qi4j.spi.composite.MixinModel;
import org.qi4j.spi.composite.ParameterModel;
import org.qi4j.spi.composite.SideEffectModel;
import org.qi4j.spi.entity.association.AssociationModel;
import org.qi4j.spi.property.PropertyModel;
import org.qi4j.spi.util.ListMap;

public final class CompositeModelFactory
    extends AbstractModelFactory
{
    public CompositeModelFactory()
    {
    }

    public CompositeModel newCompositeModel( Class<? extends Composite> compositeType )
        throws NullArgumentException, InvalidCompositeException
    {
        validateClass( compositeType );

        // Method models
        Collection<CompositeMethodModel> methods = getCompositeMethodModels( compositeType );

        // Find mixins
        List<MixinModel> mixins = new ArrayList<MixinModel>();
        addMixinModels( compositeType, compositeType, mixins );

        // Standard mixins
        mixins.add( newMixinModel( compositeType, CompositeMixin.class ) );

        if( EntityComposite.class.isAssignableFrom( compositeType ) )
        {
            mixins.add( newMixinModel( compositeType, EntityMixin.class ) );
        }

        // Find concerns
        Set<Class> concernClasses = getModifiers( compositeType, Concerns.class );
        List<ConcernModel> concerns = new ArrayList<ConcernModel>();
        for( Class concernClass : concernClasses )
        {
            concerns.add( newConcernModel( compositeType, concernClass ) );
        }

        // Find side-effects
        Set<Class> sideEffectClasses = getModifiers( compositeType, SideEffects.class );
        List<SideEffectModel> sideEffects = new ArrayList<SideEffectModel>();
        for( Class sideEffectClass : sideEffectClasses )
        {
            sideEffects.add( newSideEffectModel( compositeType, sideEffectClass ) );
        }

        // Create proxy class
        ClassLoader proxyClassloader = compositeType.getClassLoader();
        Class[] interfaces = new Class[]{ compositeType };
        Class proxyClass = Proxy.getProxyClass( proxyClassloader, interfaces );

        List<FragmentModel> fragmentModels = new ArrayList<FragmentModel>();
        fragmentModels.addAll( mixins );
        fragmentModels.addAll( concerns );
        fragmentModels.addAll( sideEffects );
        Map<Method, CompositeMethodModel> ThisModels = new HashMap<Method, CompositeMethodModel>();
        addThisModels( fragmentModels, methods, ThisModels );

        // Add mixins declared on interfaces injected with @This
        addThisInjectionMixinModels( compositeType, ThisModels.values(), mixins );

        Iterable<ConstraintModel> constraintModels = getConstraintDeclarations( compositeType );

        // Compute mapping annotation-><list of constraint implementations for different parameter types>
        ListMap<Class<? extends Annotation>, ConstraintModel> constraintModelMappings = new ListMap<Class<? extends Annotation>, ConstraintModel>();
        for( ConstraintModel constraintModel : constraintModels )
        {
            constraintModelMappings.add( constraintModel.getAnnotationType(), constraintModel );
        }

        List<PropertyModel> properties = new ArrayList<PropertyModel>();
        List<AssociationModel> associations = new ArrayList<AssociationModel>();
        for( CompositeMethodModel method : methods )
        {
            if( method.getPropertyModel() != null )
            {
                properties.add( method.getPropertyModel() );
            }
            if( method.getAssociationModel() != null )
            {
                associations.add( method.getAssociationModel() );
            }
        }
        for( CompositeMethodModel ThisModel : ThisModels.values() )
        {
            if( ThisModel.getPropertyModel() != null )
            {
                properties.add( ThisModel.getPropertyModel() );
            }
            if( ThisModel.getAssociationModel() != null )
            {
                associations.add( ThisModel.getAssociationModel() );
            }
        }

        CompositeModel model = new CompositeModel( compositeType, proxyClass, methods, mixins, constraintModels, concerns, sideEffects, ThisModels.values(), constraintModelMappings, properties, associations );
        return model;
    }

    private void validateClass( Class compositeClass )
        throws NullArgumentException, InvalidCompositeException
    {
        NullArgumentException.validateNotNull( "compositeClass", compositeClass );
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

    private MixinModel newMixinModel( Class compositeType, Class mixinClass ) throws NullArgumentException, InvalidCompositeException
    {
        mixinClass = getFragmentClass( mixinClass );

        List<ConstructorModel> constructorModels = new ArrayList<ConstructorModel>();
        getConstructorModels( mixinClass, compositeType, constructorModels );
        List<FieldModel> fieldModels = new ArrayList<FieldModel>();
        getFieldModels( mixinClass, mixinClass, compositeType, fieldModels );
        Iterable<MethodModel> methodModels = getMethodModels( mixinClass );

        Class[] appliesTo = getAppliesTo( mixinClass );

        Iterable<ConstraintModel> constraints = getConstraintDeclarations( mixinClass );

        // Find concerns
        Set<Class> concernClasses = getModifiers( mixinClass, Concerns.class );
        List<ConcernModel> concerns = new ArrayList<ConcernModel>();
        for( Class concernClass : concernClasses )
        {
            concerns.add( newConcernModel( compositeType, concernClass ) );
        }

        // Find side-effects
        Set<Class> sideEffectClasses = getModifiers( mixinClass, SideEffects.class );
        List<SideEffectModel> sideEffects = new ArrayList<SideEffectModel>();
        for( Class sideEffectClass : sideEffectClasses )
        {
            sideEffects.add( newSideEffectModel( compositeType, sideEffectClass ) );
        }

        MixinModel model = new MixinModel( mixinClass, constructorModels, fieldModels, methodModels, appliesTo, constraints, concerns, sideEffects );
        return model;
    }

    private ConcernModel newConcernModel( Class compositeType, Class modifierClass ) throws NullArgumentException, InvalidCompositeException
    {
        modifierClass = getFragmentClass( modifierClass );

        List<ConstructorModel> constructorModels = new ArrayList<ConstructorModel>();
        getConstructorModels( modifierClass, compositeType, constructorModels );
        List<FieldModel> fieldModels = new ArrayList<FieldModel>();
        getFieldModels( modifierClass, modifierClass, compositeType, fieldModels );
        Iterable<MethodModel> methodModels = getMethodModels( modifierClass );

        Class[] appliesTo = getAppliesTo( modifierClass );

        ConcernModel model = new ConcernModel( modifierClass, constructorModels, fieldModels, methodModels, appliesTo );
        return model;
    }


    private SideEffectModel newSideEffectModel( Class compositeType, Class modifierClass ) throws NullArgumentException, InvalidCompositeException
    {
        modifierClass = getFragmentClass( modifierClass );

        List<ConstructorModel> constructorModels = new ArrayList<ConstructorModel>();
        getConstructorModels( modifierClass, compositeType, constructorModels );
        List<FieldModel> fieldModels = new ArrayList<FieldModel>();
        getFieldModels( modifierClass, modifierClass, compositeType, fieldModels );
        Iterable<MethodModel> methodModels = getMethodModels( modifierClass );

        Class[] appliesTo = getAppliesTo( modifierClass );

        SideEffectModel model = new SideEffectModel( modifierClass, constructorModels, fieldModels, methodModels, appliesTo );
        return model;

    }

    private Set<Class> getModifiers( Class<?> aClass, Class annotationClass )
    {
        Set<Class> modifiers = new LinkedHashSet<Class>();
        Annotation modifierAnnotation = aClass.getAnnotation( annotationClass );
        if( modifierAnnotation != null )
        {
            Class[] modifierClasses = null;
            try
            {
                modifierClasses = (Class[]) annotationClass.getMethod( "value" ).invoke( modifierAnnotation );
            }
            catch( Exception e )
            {
                // Should not happen
                e.printStackTrace();
            }
            modifiers.addAll( Arrays.asList( modifierClasses ) );
        }

        if( !aClass.isInterface() )
        {
            if( aClass != Object.class )
            {
                // Check superclass
                Set<Class> superModifiers = getModifiers( aClass.getSuperclass(), annotationClass );
                modifiers.addAll( superModifiers );
            }
        }
        else
        {
            // Check superinterfaces
            for( Class superInterface : aClass.getInterfaces() )
            {
                Set<Class> superModifiers = getModifiers( superInterface, annotationClass );
                modifiers.addAll( superModifiers );
            }
        }

        return modifiers;
    }

    private Iterable<ConstraintModel> getConstraintDeclarations( Class compositeClass )
    {
        Constraints constraintsAnnotation = (Constraints) compositeClass.getAnnotation( Constraints.class );

        List<ConstraintModel> constraintModels = new ArrayList<ConstraintModel>();

        if( constraintsAnnotation != null )
        {
            Class<? extends Constraint>[] constraintImplementations = constraintsAnnotation.value();
            for( Class<? extends Constraint> constraintImplementation : constraintImplementations )
            {
                constraintModels.add( new ConstraintModel( constraintImplementation, compositeClass ) );
            }

        }

        // Check superinterfaces
        Class[] classes = compositeClass.getInterfaces();
        for( Class superInterface : classes )
        {
            Iterable<ConstraintModel> iterable = getConstraintDeclarations( superInterface );
            for( ConstraintModel constraintModel : iterable )
            {
                constraintModels.add( constraintModel );
            }
        }

        return constraintModels;
    }

    private Collection<CompositeMethodModel> getCompositeMethodModels( Class compositeClass )
    {
        List<CompositeMethodModel> models = new ArrayList<CompositeMethodModel>();
        Method[] methods = compositeClass.getMethods();
        for( Method method : methods )
        {
            CompositeMethodModel methodModel = newCompositeMethodModel( method, compositeClass );
            models.add( methodModel );
        }

        return models;
    }

    private CompositeMethodModel newCompositeMethodModel( Method method, Class compositeClass )
    {
        Type[] parameterTypes = method.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        List<ParameterModel> parameterModels = new ArrayList<ParameterModel>();
        int idx = 0;
        for( Type parameterType : parameterTypes )
        {
            Annotation[] parameterAnnotation = parameterAnnotations[ idx ];
            ParameterModel parameterModel = getParameterModel( parameterAnnotation, compositeClass, parameterType );
            parameterModels.add( parameterModel );
        }

        // AbstractProperty model, if any
        PropertyModel propertyModel = null;
        if( Property.class.isAssignableFrom( method.getReturnType() ) )
        {
            propertyModel = getPropertyModel( method );
        }

        // AbstractAssociation model, if any
        AssociationModel associationModel = null;
        if( AbstractAssociation.class.isAssignableFrom( method.getReturnType() ) )
        {
            Type returnType = method.getGenericReturnType();
            Type associationType = ( (ParameterizedType) returnType ).getActualTypeArguments()[ 0 ];
            associationModel = new AssociationModel( associationType, method );
        }

        CompositeMethodModel methodModel = new CompositeMethodModel( method, parameterModels, propertyModel, associationModel );
        return methodModel;
    }

    private void addMixinModels( Class compositeType, Class aType, List<MixinModel> mixins )
    {
        Mixins mixinClasses = (Mixins) aType.getAnnotation( Mixins.class );
        if( mixinClasses != null )
        {
            nextMixin:
            for( Class mixinClass : mixinClasses.value() )
            {
                for( MixinModel mixin : mixins )
                {
                    if( mixin.getModelClass().equals( mixinClass ) )
                    {
                        continue nextMixin;
                    }
                }

                mixins.add( newMixinModel( compositeType, mixinClass ) );
            }
        }

        // Check subinterfaces
        Class[] subTypes = aType.getInterfaces();
        for( Class subType : subTypes )
        {
            addMixinModels( compositeType, subType, mixins );
        }
    }

    private void addThisModels( Iterable<FragmentModel> fragmentModels, Collection<CompositeMethodModel> methods, Map<Method, CompositeMethodModel> methodModels )
    {
        for( FragmentModel fragmentModel : fragmentModels )
        {
            Set<Method> thisAsmethods = fragmentModel.getThisMethods();
            for( Method thisAsMethod : thisAsmethods )
            {
                CompositeMethodModel methodModel = methodModels.get( thisAsMethod );
                if( methodModel == null )
                {
                    methodModel = newCompositeMethodModel( thisAsMethod, fragmentModel.getModelClass() );

                    if( !methods.contains( methodModel ) ) // Don't include methods from the Composite type
                    {
                        methodModels.put( thisAsMethod, methodModel );
                    }
                }
            }
        }
    }

    private void addThisInjectionMixinModels( Class compositeType, Collection<CompositeMethodModel> compositeMethodModels, List<MixinModel> mixins )
    {
        for( CompositeMethodModel compositeMethodModel : compositeMethodModels )
        {
            Class thisMethodMixinType = compositeMethodModel.getMethod().getDeclaringClass();
            addMixinModels( compositeType, thisMethodMixinType, mixins );
        }
    }

    private Class getFragmentClass( Class mixinClass )
    {
        if( Modifier.isAbstract( mixinClass.getModifiers() ) )
        {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass( mixinClass );
            enhancer.setCallbackTypes( new Class[]{ NoOp.class } );
            enhancer.setCallbackFilter( new CallbackFilter()
            {

                public int accept( Method method )
                {
                    return 0;
                }
            } );
            mixinClass = enhancer.createClass();
            Enhancer.registerStaticCallbacks( mixinClass, new Callback[]{ NoOp.INSTANCE } );
        }
        return mixinClass;
    }

    private Class[] getAppliesTo( Class<? extends Object> aModifierClass )
    {
        AppliesTo appliesTo = aModifierClass.getAnnotation( AppliesTo.class );
        if( appliesTo != null )
        {
            return appliesTo.value();
        }

        Class<?> parent = aModifierClass.getSuperclass();
        if( parent != null && parent != Object.class )
        {
            return getAppliesTo( parent );
        }
        else
        {
            return null;
        }

    }
}
