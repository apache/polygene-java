/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.spi.composite.BindingException;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.CompositeMethodBinding;
import org.qi4j.spi.composite.CompositeMethodResolution;
import org.qi4j.spi.composite.CompositeResolution;
import org.qi4j.spi.composite.ConcernBinding;
import org.qi4j.spi.composite.ConcernResolution;
import org.qi4j.spi.composite.ConstraintBinding;
import org.qi4j.spi.composite.ConstraintResolution;
import org.qi4j.spi.composite.ConstructorBinding;
import org.qi4j.spi.composite.ConstructorResolution;
import org.qi4j.spi.composite.FieldBinding;
import org.qi4j.spi.composite.FieldResolution;
import org.qi4j.spi.composite.MethodBinding;
import org.qi4j.spi.composite.MethodResolution;
import org.qi4j.spi.composite.MixinBinding;
import org.qi4j.spi.composite.MixinResolution;
import org.qi4j.spi.composite.ParameterBinding;
import org.qi4j.spi.composite.ParameterConstraintsBinding;
import org.qi4j.spi.composite.ParameterConstraintsResolution;
import org.qi4j.spi.composite.ParameterResolution;
import org.qi4j.spi.composite.SideEffectBinding;
import org.qi4j.spi.composite.SideEffectResolution;
import org.qi4j.spi.injection.BindingContext;
import org.qi4j.spi.injection.InjectionBinding;
import org.qi4j.spi.injection.InjectionProviderFactory;
import org.qi4j.spi.injection.InjectionResolution;
import org.qi4j.spi.injection.InvalidInjectionException;

/**
 * TODO
 */
public class CompositeBinder
{
    private InjectionProviderFactory injectionProviderFactory;

    public CompositeBinder( InjectionProviderFactory injectionProviderFactory )
    {
        this.injectionProviderFactory = injectionProviderFactory;
    }

    public CompositeBinding bindCompositeResolution( BindingContext bindingContext )
        throws BindingException
    {
        Set<MixinBinding> mixinBindings = new LinkedHashSet<MixinBinding>();
        CompositeResolution compositeResolution = bindingContext.getCompositeResolution();
        Iterable<MixinResolution> mixinResolutions = compositeResolution.getMixinResolutions();
        Map<MixinResolution, MixinBinding> mixinMappings = new HashMap<MixinResolution, MixinBinding>();
        try
        {
            for( MixinResolution mixinResolution : mixinResolutions )
            {
                BindingContext mixinContext = new BindingContext( null, mixinResolution, compositeResolution, bindingContext.getModuleResolution(), bindingContext.getLayerResolution(), bindingContext.getApplicationResolution() );

                // Constructor
                Iterable<ConstructorResolution> constructorResolutions = mixinResolution.getConstructorResolutions();
                ConstructorResolution constructorResolution = constructorResolutions.iterator().next(); // TODO Pick the best constructor!
                ConstructorBinding constructorBinding = bindConstructor( mixinContext, constructorResolution );

                // Fields
                Iterable<FieldResolution> fieldResolutions = mixinResolution.getFieldResolutions();
                List<FieldBinding> fieldBindings = bindFields( mixinContext, fieldResolutions );

                // Methods
                Iterable<MethodResolution> methodResolutions = mixinResolution.getMethodResolutions();
                List<MethodBinding> methodBindings = bindMethods( mixinContext, methodResolutions );

                MixinBinding mixinBinding = new MixinBinding( mixinResolution, constructorBinding, fieldBindings, methodBindings );
                mixinBindings.add( mixinBinding );
                mixinMappings.put( mixinResolution, mixinBinding );
            }

            Iterable<CompositeMethodResolution> compositeMethodResolutions = compositeResolution.getCompositeMethodResolutions();
            List<CompositeMethodBinding> compositeMethodBindings = new ArrayList<CompositeMethodBinding>();
            Map<Method, CompositeMethodBinding> methodMappings = new HashMap<Method, CompositeMethodBinding>();
            for( CompositeMethodResolution methodResolution : compositeMethodResolutions )
            {
                Iterable<ParameterResolution> parameterResolutions = methodResolution.getParameterResolutions();
                List<ParameterBinding> parameterBindings = bindParameters( bindingContext, parameterResolutions );

                List<ConcernBinding> concernBindings = new ArrayList<ConcernBinding>();
                Iterable<ConcernResolution> concernResolutions = methodResolution.getConcernResolutions();
                for( ConcernResolution concernResolution : concernResolutions )
                {
                    BindingContext concernContext = new BindingContext( concernResolution, bindingContext );

                    ConstructorResolution constructorResolution = concernResolution.getConstructorResolutions().iterator().next(); // TODO Pick the best one
                    ConstructorBinding constructorBinding = bindConstructor( concernContext, constructorResolution );
                    Iterable<FieldBinding> fieldBindings = bindFields( concernContext, concernResolution.getFieldResolutions() );
                    Iterable<MethodBinding> methodBindings = bindMethods( concernContext, concernResolution.getMethodResolutions() );
                    ConcernBinding concernBinding = new ConcernBinding( concernResolution, constructorBinding, fieldBindings, methodBindings );
                    concernBindings.add( concernBinding );
                }

                List<SideEffectBinding> sideEffectBindings = new ArrayList<SideEffectBinding>();
                Iterable<SideEffectResolution> sideEffectResolutions = methodResolution.getSideEffectResolutions();
                for( SideEffectResolution sideEffectResolution : sideEffectResolutions )
                {
                    BindingContext sideEffectContext = new BindingContext( null, sideEffectResolution, compositeResolution, bindingContext.getModuleResolution(), bindingContext.getLayerResolution(), bindingContext.getApplicationResolution() );

                    ConstructorResolution constructorResolution = sideEffectResolution.getConstructorResolutions().iterator().next(); // TODO Pick the best one
                    ConstructorBinding constructorBinding = bindConstructor( sideEffectContext, constructorResolution );
                    Iterable<FieldBinding> fieldBindings = bindFields( sideEffectContext, sideEffectResolution.getFieldResolutions() );
                    Iterable<MethodBinding> methodBindings = bindMethods( sideEffectContext, sideEffectResolution.getMethodResolutions() );
                    SideEffectBinding concernBinding = new SideEffectBinding( sideEffectResolution, constructorBinding, fieldBindings, methodBindings );
                    sideEffectBindings.add( concernBinding );
                }

                MixinBinding mixinBinding = mixinMappings.get( methodResolution.getMixinResolution() );
                CompositeMethodBinding methodBinding = new CompositeMethodBinding( methodResolution, parameterBindings, concernBindings, sideEffectBindings, mixinBinding );
                compositeMethodBindings.add( methodBinding );
                methodMappings.put( methodResolution.getCompositeMethodModel().getMethod(), methodBinding );
            }

            CompositeBinding compositeBinding = new CompositeBinding( compositeResolution, compositeMethodBindings, mixinBindings, methodMappings );
            return compositeBinding;
        }
        catch( InvalidInjectionException e )
        {
            throw new BindingException( "Could not bind injections", e );
        }
    }

    protected ConstructorBinding bindConstructor( BindingContext bindingContext, ConstructorResolution constructorResolution )
        throws InvalidInjectionException
    {
        ConstructorBinding constructorBinding;
        {
            Iterable<ParameterResolution> parameterResolutions = constructorResolution.getParameterResolutions();
            List<ParameterBinding> parameterBindings = bindParameters( bindingContext, parameterResolutions );
            constructorBinding = new ConstructorBinding( constructorResolution, parameterBindings );
        }
        return constructorBinding;
    }

    protected List<MethodBinding> bindMethods( BindingContext bindingContext, Iterable<MethodResolution> methodResolutions )
        throws InvalidInjectionException
    {
        List<MethodBinding> methodBindings = new ArrayList<MethodBinding>();
        for( MethodResolution methodResolution : methodResolutions )
        {
            Iterable<ParameterResolution> parameterResolutions = methodResolution.getParameterResolutions();
            List<ParameterBinding> parameterBindings = bindParameters( bindingContext, parameterResolutions );

            MethodBinding methodBinding = new MethodBinding( methodResolution, parameterBindings );
            methodBindings.add( methodBinding );
        }
        return methodBindings;
    }

    protected List<FieldBinding> bindFields( BindingContext bindingContext, Iterable<FieldResolution> fieldResolutions )
        throws InvalidInjectionException
    {
        List<FieldBinding> fieldBindings = new ArrayList<FieldBinding>();
        for( FieldResolution fieldResolution : fieldResolutions )
        {
            FieldBinding fieldBinding = bindField( bindingContext, fieldResolution );
            fieldBindings.add( fieldBinding );
        }
        return fieldBindings;
    }

    private FieldBinding bindField( BindingContext bindingContext, FieldResolution fieldResolution )
        throws InvalidInjectionException
    {
        InjectionResolution injectionResolution = fieldResolution.getInjectionResolution();
        InjectionBinding injectionBinding = null;
        if( injectionResolution != null )
        {
            BindingContext injectionContext = new BindingContext( injectionResolution, bindingContext );
            injectionBinding = new InjectionBinding( injectionResolution, injectionProviderFactory.newInjectionProvider( injectionContext ) );
        }
        FieldBinding fieldBinding = new FieldBinding( fieldResolution, injectionBinding );
        return fieldBinding;
    }

    private List<ParameterBinding> bindParameters( BindingContext bindingContext, Iterable<ParameterResolution> parameterResolutions )
        throws InvalidInjectionException
    {
        List<ParameterBinding> parameterBindings = new ArrayList<ParameterBinding>();
        for( ParameterResolution parameterResolution : parameterResolutions )
        {
            ParameterConstraintsResolution parameterConstraintsResolution = parameterResolution.getParameterConstraintResolution();
            ParameterConstraintsBinding parameterConstraintsBinding = null;
            if( parameterConstraintsResolution != null )
            {
                Map<Annotation, ConstraintBinding> annotationConstraintBindings = new HashMap<Annotation, ConstraintBinding>();
                for( ConstraintResolution constraintResolution : parameterConstraintsResolution.getConstraintResolutions() )
                {
                    ConstraintBinding constraintBinding = new ConstraintBinding( constraintResolution );
                    annotationConstraintBindings.put( constraintResolution.getConstraintAnnotation(), constraintBinding );
                }
                parameterConstraintsBinding = new ParameterConstraintsBinding( parameterConstraintsResolution, annotationConstraintBindings );
            }

            InjectionResolution injectionResolution = parameterResolution.getInjectionResolution();
            InjectionBinding injectionBinding = null;
            if( injectionResolution != null )
            {
                BindingContext injectionContext = new BindingContext( injectionResolution, bindingContext );
                injectionBinding = new InjectionBinding( injectionResolution, injectionProviderFactory.newInjectionProvider( injectionContext ) );
            }

            ParameterBinding parameterBinding = new ParameterBinding( parameterResolution, parameterConstraintsBinding, injectionBinding );
            parameterBindings.add( parameterBinding );
        }
        return parameterBindings;
    }
}
