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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.Composite;
import org.qi4j.Constraint;
import org.qi4j.annotation.Concerns;
import org.qi4j.annotation.Constraints;
import org.qi4j.annotation.Mixins;
import org.qi4j.annotation.SideEffects;
import org.qi4j.model.CompositeModel;
import org.qi4j.model.ConcernModel;
import org.qi4j.model.ConstraintDeclarationModel;
import org.qi4j.model.FragmentModel;
import org.qi4j.model.InvalidCompositeException;
import org.qi4j.model.MethodConstraint;
import org.qi4j.model.MethodModel;
import org.qi4j.model.MixinModel;
import org.qi4j.model.NullArgumentException;
import org.qi4j.model.SideEffectModel;
import org.qi4j.persistence.Entity;
import org.qi4j.persistence.EntityComposite;
import org.qi4j.runtime.persistence.EntityImpl;

public final class CompositeModelFactory
{
    private ConcernModelFactory concernModelFactory;
    private SideEffectModelFactory sideEffectModelFactory;
    private MixinModelFactory mixinModelFactory;
    private ConstraintModelFactory constraintModelFactory;

    public CompositeModelFactory()
    {
        constraintModelFactory = new ConstraintModelFactory();
        concernModelFactory = new ConcernModelFactory();
        sideEffectModelFactory = new SideEffectModelFactory();
        mixinModelFactory = new MixinModelFactory( concernModelFactory, sideEffectModelFactory );
    }

    public CompositeModelFactory( ConstraintModelFactory constraintModelFactory, ConcernModelFactory concernModelFactory, SideEffectModelFactory sideEffectModelFactory, MixinModelFactory mixinModelFactory )
    {
        this.constraintModelFactory = constraintModelFactory;
        this.concernModelFactory = concernModelFactory;
        this.sideEffectModelFactory = sideEffectModelFactory;
        this.mixinModelFactory = mixinModelFactory;
    }

    public <T extends Composite> CompositeModel<T> newCompositeModel( Class<T> compositeClass )
        throws NullArgumentException, InvalidCompositeException
    {
        validateClass( compositeClass );

        // Method models
        Collection<MethodModel> methods = findMethods( compositeClass );

        // Find mixins
        List<MixinModel> mixins = findMixins( compositeClass, compositeClass );

        // Standard mixins
        mixins.add( mixinModelFactory.newFragmentModel( CompositeMixin.class, compositeClass, Composite.class ) );

        if( EntityComposite.class.isAssignableFrom( compositeClass ) )
        {
            mixins.add( mixinModelFactory.newFragmentModel( EntityImpl.class, compositeClass, Entity.class ) );
        }

        // Find concerns
        List<ConcernModel> concerns = getModifiers( compositeClass, compositeClass, Concerns.class, concernModelFactory );

        // Find side-effects
        List<SideEffectModel> sideEffects = getModifiers( compositeClass, compositeClass, SideEffects.class, sideEffectModelFactory );

        // Create proxy class
        ClassLoader proxyClassloader = compositeClass.getClassLoader();
        Class[] interfaces = new Class[]{ compositeClass };
        Class<? extends T> proxyClass = (Class<? extends T>) Proxy.getProxyClass( proxyClassloader, interfaces );

        List<FragmentModel> fragmentModels = new ArrayList<FragmentModel>();
        fragmentModels.addAll( mixins );
        fragmentModels.addAll( concerns );
        fragmentModels.addAll( sideEffects );
        Iterable<MethodModel> thisCompositeAsModels = getThisCompositeAsModels( fragmentModels );

        Iterable<ConstraintDeclarationModel> constraintModels = getConstraintDeclarations( compositeClass );
        CompositeModel model = new CompositeModel<T>( compositeClass, proxyClass, methods, mixins, constraintModels, concerns, sideEffects, thisCompositeAsModels );
        return model;
    }

    private Iterable<ConstraintDeclarationModel> getConstraintDeclarations( Class compositeClass )
    {
        Constraints constraintsAnnotation = (Constraints) compositeClass.getAnnotation( Constraints.class );

        List<ConstraintDeclarationModel> constraintDeclarationModels = new ArrayList<ConstraintDeclarationModel>();

        if( constraintsAnnotation != null )
        {
            Class<? extends Constraint>[] constraintImplementations = constraintsAnnotation.value();
            for( Class<? extends Constraint> constraintImplementation : constraintImplementations )
            {
                Class annotationType = (Class) ( (ParameterizedType) constraintImplementation.getGenericInterfaces()[ 0 ] ).getActualTypeArguments()[ 0 ];
                Class parameterType = (Class) ( (ParameterizedType) constraintImplementation.getGenericInterfaces()[ 0 ] ).getActualTypeArguments()[ 1 ];

                constraintDeclarationModels.add( new ConstraintDeclarationModel( constraintImplementation, annotationType, parameterType, compositeClass ) );
            }

        }

        // Check superinterfaces
        Class[] classes = compositeClass.getInterfaces();
        for( Class superInterface : classes )
        {
            Iterable<ConstraintDeclarationModel> iterable = getConstraintDeclarations( superInterface );
            for( ConstraintDeclarationModel constraintDeclarationModel : iterable )
            {
                constraintDeclarationModels.add( constraintDeclarationModel );
            }
        }

        return constraintDeclarationModels;
    }

    private <T extends Composite> Collection<MethodModel> findMethods( Class<T> compositeClass )
    {
        List<MethodModel> models = new ArrayList<MethodModel>();
        Method[] methods = compositeClass.getMethods();
        for( Method method : methods )
        {
            MethodConstraint methodConstraint = constraintModelFactory.newMethodConstraint( method );
            models.add( new MethodModel( method, methodConstraint ) );
        }

        return models;
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

    private List<MixinModel> findMixins( Class aType, Class compositeType )
    {
        List<MixinModel> mixinModels = new ArrayList<MixinModel>();

        Mixins impls = (Mixins) aType.getAnnotation( Mixins.class );
        if( impls != null )
        {
            for( Class impl : impls.value() )
            {
                mixinModels.add( mixinModelFactory.newFragmentModel( impl, compositeType, aType ) );
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

    private <K extends FragmentModel> List<K> getModifiers( Class<?> aClass, Class compositeType, Class annotationClass, FragmentModelFactory<K> modelFactory )
    {
        List<K> modifiers = new ArrayList<K>();
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
            for( Class modifier : modifierClasses )
            {
                K concernModel = (K) modelFactory.newFragmentModel( modifier, compositeType, aClass );
                modifiers.add( concernModel );
            }
        }

        // Check subinterfaces
        Class[] subTypes = aClass.getInterfaces();
        for( Class subType : subTypes )
        {
            modifiers.addAll( getModifiers( subType, compositeType, annotationClass, modelFactory ) );
        }

        return modifiers;
    }

    private Iterable<MethodModel> getThisCompositeAsModels( Iterable<FragmentModel> fragmentModels )
    {
        Map<Method, MethodModel> methodModels = new HashMap<Method, MethodModel>();
        for( FragmentModel fragmentModel : fragmentModels )
        {
            Set<Method> thisAsmethods = fragmentModel.getThisCompositeAsMethods();
            for( Method thisAsMethod : thisAsmethods )
            {
                MethodModel methodModel = methodModels.get( thisAsMethod );
                if( methodModel == null )
                {
                    MethodConstraint methodConstraint = constraintModelFactory.newMethodConstraint( thisAsMethod );
                    methodModel = new MethodModel( thisAsMethod, methodConstraint );
                    methodModels.put( thisAsMethod, methodModel );
                }
            }
        }

        return methodModels.values();
    }
}
