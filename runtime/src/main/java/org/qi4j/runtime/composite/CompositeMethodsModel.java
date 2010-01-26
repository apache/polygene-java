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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.MissingMethodException;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.util.MethodKeyMap;

/**
 * Model for Composite methods. This includes both private and public methods.
 */
public final class CompositeMethodsModel
    implements Binder, Serializable
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
                                  AbstractMixinsModel mixinsModel
    )
    {
        methods = new MethodKeyMap<CompositeMethodModel>();
        this.type = type;
        this.constraintsModel = constraintsModel;
        this.concernsModel = concernsModel;
        this.sideEffectsModel = sideEffectsModel;
        this.mixinsModel = mixinsModel;
        implementMixinType( type );
    }

    // Binding

    public void bind( Resolution resolution )
        throws BindingException
    {
        for( CompositeMethodModel compositeMethodComposite : methods.values() )
        {
            compositeMethodComposite.bind( resolution );
        }
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
                        HashMap<Method, CompositeMethodModel> newMethods = new MethodKeyMap<CompositeMethodModel>();
                        newMethods.putAll( methods );
                        newMethods.put( method, compositeMethod ); // Map mixin method to interface method
                        methods = newMethods; // Replace old map
                        break;
                    }
                    catch( NoSuchMethodException e )
                    {
                    }
                    catch( SecurityException e )
                    {
                    }
                }
                if( compositeMethod != null )
                {
                    return compositeMethod.invoke( proxy, args, mixins, moduleInstance );
                }
            }
//            return mixins.invokeObject( proxy, args, method );
            throw new MissingMethodException( "Method '" + method + "' is not present in composite " + type.getName(), method );
        }
        else
        {
            return compositeMethod.invoke( proxy, args, mixins, moduleInstance );
        }
    }

    public void implementMixinType( Class mixinType )
    {
        final Set<Class> thisDependencies = new HashSet<Class>();
        for( Method method : mixinType.getMethods() )
        {
            if( methods.get( method ) == null )
            {
                MixinModel mixinModel = mixinsModel.implementMethod( method );
                MethodConcernsModel methodConcernsModel1 = concernsModel.concernsFor( method, type );
                MethodSideEffectsModel methodSideEffectsModel1 = sideEffectsModel.sideEffectsFor( method, type );
                Method mixinMethod = null;
                try
                {
                    mixinMethod = mixinModel.mixinClass().getMethod( method.getName(), method.getParameterTypes() );
                    MethodConcernsModel methodConcernsModel2 = concernsModel.concernsFor( mixinMethod, type );
                    methodConcernsModel1 = methodConcernsModel1.combineWith( methodConcernsModel2 );
                    MethodSideEffectsModel methodSideEffectsModel2 = sideEffectsModel.sideEffectsFor( mixinMethod, type );
                    methodSideEffectsModel1 = methodSideEffectsModel1.combineWith( methodSideEffectsModel2 );
                }
                catch( NoSuchMethodException e )
                {
                    // Ignore...
                }

                MethodConcernsModel mixinMethodConcernsModel1 = mixinModel.concernsFor( method, type );
                methodConcernsModel1 = methodConcernsModel1.combineWith( mixinMethodConcernsModel1 );
                MethodSideEffectsModel mixinMethodSideEffectsModel1 = mixinModel.sideEffectsFor( method, type );
                methodSideEffectsModel1 = methodSideEffectsModel1.combineWith( mixinMethodSideEffectsModel1 );
                if( mixinMethod != null )
                {
                    MethodConcernsModel mixinMethodConcernsModel2 = mixinModel.concernsFor( mixinMethod, type );
                    methodConcernsModel1 = methodConcernsModel1.combineWith( mixinMethodConcernsModel2 );
                    MethodSideEffectsModel mixinMethodSideEffectsModel2 = mixinModel.sideEffectsFor( mixinMethod, type );
                    methodSideEffectsModel1 = methodSideEffectsModel1.combineWith( mixinMethodSideEffectsModel2 );
                }
                method.setAccessible( true );
                CompositeMethodModel methodComposite = new CompositeMethodModel( method,
                                                                                 new MethodConstraintsModel( method, constraintsModel ),
                                                                                 methodConcernsModel1,
                                                                                 methodSideEffectsModel1,
                                                                                 mixinsModel );

                // Implement @This references
                methodComposite.addThisInjections( thisDependencies );
                mixinModel.addThisInjections( thisDependencies );
                methods.put( method, methodComposite );
            }
        }

        // Add type to set of mixin types
        mixinsModel.addMixinType( mixinType );

        // Implement all @This dependencies that were found
        for( Class thisDependency : thisDependencies )
        {
            implementMixinType( thisDependency );
        }
    }

    public Iterable<Method> methods()
    {
        return methods.keySet();
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        for( CompositeMethodModel compositeMethodModel : methods.values() )
        {
            compositeMethodModel.visitModel( modelVisitor );
        }
    }

    public String toString()
    {
        return type.getName();
    }
}
