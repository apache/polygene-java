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
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.Composite;
import org.qi4j.api.annotation.Assertions;
import org.qi4j.api.annotation.Mixins;
import org.qi4j.api.annotation.SideEffects;
import org.qi4j.api.model.AssertionModel;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.FragmentModel;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.MethodModel;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.NullArgumentException;
import org.qi4j.api.model.SideEffectModel;
import org.qi4j.api.persistence.EntityComposite;
import org.qi4j.runtime.persistence.EntityImpl;

public class CompositeModelFactory
{
    private AssertionModelFactory assertionModelFactory;
    private SideEffectModelFactory sideEffectModelFactory;
    private MixinModelFactory mixinModelFactory;

    public CompositeModelFactory()
    {
        assertionModelFactory = new AssertionModelFactory();
        sideEffectModelFactory = new SideEffectModelFactory();
        mixinModelFactory = new MixinModelFactory( assertionModelFactory, sideEffectModelFactory );
    }

    public CompositeModelFactory( AssertionModelFactory assertionModelFactory, SideEffectModelFactory sideEffectModelFactory, MixinModelFactory mixinModelFactory )
    {
        this.assertionModelFactory = assertionModelFactory;
        this.sideEffectModelFactory = sideEffectModelFactory;
        this.mixinModelFactory = mixinModelFactory;
    }

    public <T extends Composite> CompositeModel<T> newCompositeModel( Class<T> compositeClass )
        throws NullArgumentException, InvalidCompositeException
    {
        validateClass( compositeClass );

        // Method models
        Iterable<MethodModel> methods = findMethods( compositeClass );

        // Find mixins
        List<MixinModel> mixins = findMixins( compositeClass, compositeClass );

        // Standard mixins
        mixins.add( mixinModelFactory.newFragmentModel( CompositeMixin.class, compositeClass ) );
        mixins.add( mixinModelFactory.newFragmentModel( LifecycleImpl.class, compositeClass ) );

        if( EntityComposite.class.isAssignableFrom( compositeClass ) )
        {
            mixins.add( mixinModelFactory.newFragmentModel( EntityImpl.class, compositeClass ) );
        }

        // Find assertions
        List<AssertionModel> assertions = getModifiers( compositeClass, compositeClass, Assertions.class, assertionModelFactory );

        // Find side-effects
        List<SideEffectModel> sideEffects = getModifiers( compositeClass, compositeClass, SideEffects.class, sideEffectModelFactory );

        // Create proxy class
        ClassLoader proxyClassloader = compositeClass.getClassLoader();
        Class[] interfaces = new Class[]{ compositeClass };
        Class<? extends T> proxyClass = (Class<? extends T>) Proxy.getProxyClass( proxyClassloader, interfaces );

        CompositeModel model = new CompositeModel<T>( compositeClass, proxyClass, methods, mixins, assertions, sideEffects );
        return model;
    }

    private <T extends Composite> Iterable<MethodModel> findMethods( Class<T> compositeClass )
    {
        List<MethodModel> models = new ArrayList<MethodModel>();
        Method[] methods = compositeClass.getMethods();
        for( Method method : methods )
        {
            models.add( new MethodModel( method ) );
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
                mixinModels.add( mixinModelFactory.newFragmentModel( impl, compositeType ) );
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
                K assertionModel = (K) modelFactory.newFragmentModel( modifier, compositeType );
                modifiers.add( assertionModel );
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
}
