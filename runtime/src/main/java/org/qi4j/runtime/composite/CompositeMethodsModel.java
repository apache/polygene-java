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

import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.MissingMethodException;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.util.Classes;
import org.qi4j.api.util.HierarchicalVisitor;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.util.VisitableHierarchy;
import org.qi4j.runtime.bootstrap.AssemblyHelper;
import org.qi4j.runtime.injection.Dependencies;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.structure.ModuleInstance;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.qi4j.api.specification.Specifications.in;
import static org.qi4j.api.specification.Specifications.not;
import static org.qi4j.api.util.Iterables.*;

/**
 * Model for Composite methods. This includes both private and public methods.
 */
public final class CompositeMethodsModel
        implements VisitableHierarchy<Object, Object>
{
    private HashMap<Method, CompositeMethodModel> methods;
    private final Class<? extends Composite> type;
    private final ConstraintsModel constraintsModel;
    private final ConcernsDeclaration concernsModel;
    private final SideEffectsDeclaration sideEffectsModel;
    private final AbstractMixinsModel mixinsModel;

    public CompositeMethodsModel( Class<? extends Composite> type,
                                  ConstraintsModel constraintsModel,
                                  ConcernsDeclaration concernsModel,
                                  SideEffectsDeclaration sideEffectsModel,
                                  AbstractMixinsModel mixinsModel,
                                  AssemblyHelper helper
    )
    {
        methods = new HashMap<Method, CompositeMethodModel>();
        this.type = type;
        this.constraintsModel = constraintsModel;
        this.concernsModel = concernsModel;
        this.sideEffectsModel = sideEffectsModel;
        this.mixinsModel = mixinsModel;

        for( Class mixinType : mixinsModel.roles() )
        {
            implementMixinType( mixinType, helper );
        }
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
                Iterable<Class> types = mixinsModel.mixinTypes();
                for( Class aClass : types )
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
            throw new MissingMethodException( "Method '" + method + "' is not present in composite " + type.getName(), method );
        } else
        {
            return compositeMethod.invoke( proxy, args, mixins, moduleInstance );
        }
    }

    public void implementMixinType( Class mixinType, AssemblyHelper helper )
    {
        Set<Class<?>> thisDependencies = new HashSet<Class<?>>(  );
        for( Method method : mixinType.getMethods() )
        {
            if( methods.get( method ) == null )
            {
                MixinModel mixinModel = mixinsModel.implementMethod( method, helper );
                MethodConcernsModel methodConcernsModel1 = concernsModel.concernsFor( method, type, helper );
                MethodSideEffectsModel methodSideEffectsModel1 = sideEffectsModel.sideEffectsFor( method, type, helper );
                Method mixinMethod = null;
                try
                {
                    mixinMethod = mixinModel.mixinClass().getMethod( method.getName(), method.getParameterTypes() );
                    MethodConcernsModel methodConcernsModel2 = concernsModel.concernsFor( mixinMethod, type, helper );
                    methodConcernsModel1 = methodConcernsModel1.combineWith( methodConcernsModel2 );
                    MethodSideEffectsModel methodSideEffectsModel2 = sideEffectsModel.sideEffectsFor( mixinMethod, type, helper );
                    methodSideEffectsModel1 = methodSideEffectsModel1.combineWith( methodSideEffectsModel2 );
                } catch( NoSuchMethodException e )
                {
                    // Ignore...
                }

                MethodConcernsModel mixinMethodConcernsModel1 = mixinModel.concernsFor( method, type, helper );
                methodConcernsModel1 = methodConcernsModel1.combineWith( mixinMethodConcernsModel1 );
                MethodSideEffectsModel mixinMethodSideEffectsModel1 = mixinModel.sideEffectsFor( method, type, helper );
                methodSideEffectsModel1 = methodSideEffectsModel1.combineWith( mixinMethodSideEffectsModel1 );
                if( mixinMethod != null )
                {
                    MethodConcernsModel mixinMethodConcernsModel2 = mixinModel.concernsFor( mixinMethod, type, helper );
                    methodConcernsModel1 = methodConcernsModel1.combineWith( mixinMethodConcernsModel2 );
                    MethodSideEffectsModel mixinMethodSideEffectsModel2 = mixinModel.sideEffectsFor( mixinMethod, type, helper );
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
                Iterables.addAll( thisDependencies, (Iterable<? extends Class<?>>) flatten(map,
                        map1,
                        filter ));

                methods.put( method, methodComposite );
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
        return type.getName();
    }
}
