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

package org.qi4j.runtime.injection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.Specification;
import org.qi4j.spi.composite.InjectedParametersDescriptor;

import static org.qi4j.runtime.structure.Specification.CollectionFilter.*;

/**
 * JAVADOC
 */
public final class InjectedParametersModel
    implements Binder, InjectedParametersDescriptor, Serializable
{
    private final List<DependencyModel> parameterDependencies;

    public InjectedParametersModel()
    {
        parameterDependencies = new ArrayList<DependencyModel>();
    }

    public List<DependencyModel> dependencies()
    {
        return parameterDependencies;
    }

    // Binding

    public void bind( Resolution resolution )
        throws BindingException
    {
        for( DependencyModel parameterDependency : parameterDependencies )
        {
            parameterDependency.bind( resolution );
        }
    }

    // Context

    public Object[] newParametersInstance( InjectionContext context )
    {
        Object[] parametersInstance = new Object[parameterDependencies.size()];

        // Inject parameterDependencies
        for( int j = 0; j < parameterDependencies.size(); j++ )
        {
            DependencyModel dependencyModel = parameterDependencies.get( j );
            Object parameter = dependencyModel.inject( context );
            parametersInstance[ j ] = parameter;
        }

        return parametersInstance;
    }

    public void addDependency( DependencyModel dependency )
    {
        parameterDependencies.add( dependency );
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );
    }

    public Collection<DependencyModel> filter( Specification<DependencyModel> specification )
    {
        return filterBy( parameterDependencies, specification );
    }

    @Override
    public String toString()
    {
        return "InjectedParametersModel{" +
               "parameterDependencies=" + parameterDependencies +
               '}';
    }
}

