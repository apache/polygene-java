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

import org.qi4j.api.composite.MissingMethodException;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.injection.Dependencies;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.structure.ModuleInstance;

import java.lang.reflect.Method;
import java.util.HashMap;

import static org.qi4j.functional.Iterables.*;
import static org.qi4j.functional.Specifications.in;

/**
 * Model for Composite methods. This includes both private and public methods.
 */
public final class CompositeMethodsModel
        implements VisitableHierarchy<Object, Object>
{
    private HashMap<Method, CompositeMethodModel> methods;
    private final MixinsModel mixinsModel;

    public CompositeMethodsModel(MixinsModel mixinsModel
    )
    {
        methods = new HashMap<Method, CompositeMethodModel>();
        this.mixinsModel = mixinsModel;
    }


    public Iterable<DependencyModel> dependencies()
    {
        return Iterables.flattenIterables( map( Dependencies.DEPENDENCIES_FUNCTION, methods.values() ) );
    }

    // Context
    public Object invoke( MixinsInstance mixins,
                          Object proxy,
                          Method method,
                          Object[] args,
                          ModuleInstance moduleInstance
    )
            throws Throwable
    {
        CompositeMethodModel compositeMethod = methods.get( method );

        if( compositeMethod == null )
        {
            if( method.getDeclaringClass().equals( Object.class ) )
            {
                return mixins.invokeObject( proxy, args, method );
            }

            if( !method.getDeclaringClass().isInterface() )
            {
                Iterable<Class<?>> types = mixinsModel.mixinTypes();
                for( Class<?> aClass : types )
                {
                    try
                    {
                        Method realMethod = aClass.getMethod( method.getName(), method.getParameterTypes() );
                        compositeMethod = methods.get( realMethod );
                        break;
                    } catch( NoSuchMethodException e )
                    {
                    } catch( SecurityException e )
                    {
                    }
                }
            }
//            return mixins.invokeObject( proxy, args, method );
            throw new MissingMethodException( "Method '" + method + "' is not implemented" );
        } else
        {
            return compositeMethod.invoke( proxy, args, mixins, moduleInstance );
        }
    }

    /*
    public void implementMixinType( Class mixinType, AssemblyHelper helper )
    {
        Set<Class<?>> thisDependencies = new HashSet<Class<?>>(  );
        for( Method method : mixinType.getMethods() )
        {
            if( !isImplemented( method )  &&
                    !Proxy.class.equals( method.getDeclaringClass().getSuperclass()) &&
                    !Proxy.class.equals( method.getDeclaringClass() ))
            {
                MixinModel mixinModel = mixinsModel.implementMethod( method, helper );
                MethodConcernsModel methodConcernsModel1 = concernsModel.concernsFor( method, types, helper );
                MethodSideEffectsModel methodSideEffectsModel1 = sideEffectsModel.sideEffectsFor( method, types, helper );
                Method mixinMethod = null;
                try
                {
                    mixinMethod = mixinModel.mixinClass().getMethod( method.getName(), method.getParameterTypes() );
                    MethodConcernsModel methodConcernsModel2 = concernsModel.concernsFor( mixinMethod, types, helper );
                    methodConcernsModel1 = methodConcernsModel1.combineWith( methodConcernsModel2 );
                    MethodSideEffectsModel methodSideEffectsModel2 = sideEffectsModel.sideEffectsFor( mixinMethod, types, helper );
                    methodSideEffectsModel1 = methodSideEffectsModel1.combineWith( methodSideEffectsModel2 );
                } catch( NoSuchMethodException e )
                {
                    // Ignore...
                }

                MethodConcernsModel mixinMethodConcernsModel1 = mixinModel.concernsFor( method, types, helper );
                methodConcernsModel1 = methodConcernsModel1.combineWith( mixinMethodConcernsModel1 );
                MethodSideEffectsModel mixinMethodSideEffectsModel1 = mixinModel.sideEffectsFor( method, types, helper );
                methodSideEffectsModel1 = methodSideEffectsModel1.combineWith( mixinMethodSideEffectsModel1 );
                if( mixinMethod != null )
                {
                    MethodConcernsModel mixinMethodConcernsModel2 = mixinModel.concernsFor( mixinMethod, types, helper );
                    methodConcernsModel1 = methodConcernsModel1.combineWith( mixinMethodConcernsModel2 );
                    MethodSideEffectsModel mixinMethodSideEffectsModel2 = mixinModel.sideEffectsFor( mixinMethod, types, helper );
                    methodSideEffectsModel1 = methodSideEffectsModel1.combineWith( mixinMethodSideEffectsModel2 );
                }
                method.setAccessible( true );

                if( !methodConcernsModel1.hasConcerns() )
                {
                    methodConcernsModel1 = null; // Don't store reference to something meaningless
                }
                if( !methodSideEffectsModel1.hasSideEffects() )
                {
                    methodSideEffectsModel1 = null; // Same here
                }

                CompositeMethodModel methodComposite = new CompositeMethodModel( method,
                        new MethodConstraintsModel( method, constraintsModel ),
                        methodConcernsModel1,
                        methodSideEffectsModel1,
                        mixinsModel );

                // Implement @This references
                Iterable<Class<?>> map = map( new DependencyModel.InjectionTypeFunction(), filter( new DependencyModel.ScopeSpecification( This.class ), methodComposite.dependencies() ) );
                Iterable<Class<?>> map1 = map( new DependencyModel.InjectionTypeFunction(), filter( new DependencyModel.ScopeSpecification( This.class ), mixinModel.dependencies() ) );
                Iterable<Class<?>> filter = filter( not( in( Activatable.class, Initializable.class, Lifecycle.class, InvocationHandler.class ) ), map(Classes.RAW_CLASS, Classes.INTERFACES_OF.map( mixinModel.mixinClass() ) ));
                Iterables.addAll( thisDependencies, (Iterable<? extends Class<?>>) flatten( map,
                        map1,
                        filter ) );

                addMethod( methodComposite );
            }
        }

        // Add type to set of mixin types
        mixinsModel.addMixinType( mixinType );

        // Implement all @This dependencies that were found
        for( Class<?> thisDependency : thisDependencies )
        {
            implementMixinType( thisDependency, helper );
        }
    }
    */

    public void addMethod(CompositeMethodModel methodModel)
    {
        methods.put( methodModel.method(), methodModel );
    }

    public boolean isImplemented(Method method)
    {
        return methods.containsKey( method );
    }

    public Iterable<Method> methods()
    {
        return methods.keySet();
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor ) throws ThrowableType
    {
        if (modelVisitor.visitEnter( this ))
        {
            for( CompositeMethodModel compositeMethodModel : methods.values() )
            {
                if (!compositeMethodModel.accept( modelVisitor ))
                    break;
            }
        }
        return modelVisitor.visitLeave( this );
    }

    public String toString()
    {
        return methods().toString();
    }
}
