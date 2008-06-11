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

package org.qi4j.runtime.object;

import org.qi4j.runtime.composite.BindingException;
import org.qi4j.runtime.composite.ConstraintsModel;
import org.qi4j.runtime.composite.ConstructorsModel;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.runtime.composite.UsesInstance;
import org.qi4j.runtime.injection.DependencyVisitor;
import org.qi4j.runtime.injection.InjectedFieldsModel;
import org.qi4j.runtime.injection.InjectedMethodsModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.structure.Binder;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.structure.ModuleModel;
import org.qi4j.structure.Visibility;
import org.qi4j.util.MetaInfo;

/**
 * TODO
 */
public class ObjectModel
    implements Binder
{
    private Class objectType;
    private Visibility visibility;
    private MetaInfo metaInfo;
    private ModuleModel moduleModel;
    private ConstraintsModel constraintsModel;
    private ConstructorsModel constructorsModel;
    private InjectedFieldsModel injectedFieldsModel;
    private InjectedMethodsModel injectedMethodsModel;

    public ObjectModel( Class objectType,
                        Visibility visibility,
                        MetaInfo metaInfo,
                        ModuleModel moduleModel
    )
    {
        this.objectType = objectType;
        this.visibility = visibility;
        this.metaInfo = metaInfo;
        this.moduleModel = moduleModel;

        constructorsModel = new ConstructorsModel( objectType );
        injectedFieldsModel = new InjectedFieldsModel( objectType );
        injectedMethodsModel = new InjectedMethodsModel( objectType );
    }

    public Class type()
    {
        return objectType;
    }

    public Visibility visibility()
    {
        return visibility;
    }

    public MetaInfo metaInfo()
    {
        return metaInfo;
    }

    public void visitDependencies( DependencyVisitor visitor )
    {
        constructorsModel.visitDependencies( visitor );
        injectedFieldsModel.visitDependencies( visitor );
        injectedMethodsModel.visitDependencies( visitor );
    }

    public void bind( Resolution resolution ) throws BindingException
    {
        constructorsModel.bind( resolution );
        injectedFieldsModel.bind( resolution );
        injectedMethodsModel.bind( resolution );
    }

    public Object newInstance( ModuleInstance moduleInstance, UsesInstance uses )
    {
        InjectionContext injectionContext = new InjectionContext( moduleInstance, uses );
        Object instance = constructorsModel.newInstance( injectionContext );
        injectedFieldsModel.inject( injectionContext, instance );
        injectedMethodsModel.inject( injectionContext, instance );
        return instance;
    }

    public void inject( ModuleInstance moduleInstance, UsesInstance uses, Object instance )
    {
        InjectionContext injectionContext = new InjectionContext( moduleInstance, uses );
        injectedFieldsModel.inject( injectionContext, instance );
        injectedMethodsModel.inject( injectionContext, instance );
    }
}
