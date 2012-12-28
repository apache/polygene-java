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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.qi4j.api.composite.InjectedParametersDescriptor;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.Specification;
import org.qi4j.functional.VisitableHierarchy;

/**
 * JAVADOC
 */
public final class InjectedParametersModel
    implements InjectedParametersDescriptor, Dependencies, VisitableHierarchy<Object, Object>
{
    private final List<DependencyModel> parameterDependencies;

    public InjectedParametersModel()
    {
        parameterDependencies = new ArrayList<DependencyModel>();
    }

    @Override
    public Iterable<DependencyModel> dependencies()
    {
        return parameterDependencies;
    }

    // Context
    public Object[] newParametersInstance( InjectionContext context )
    {
        Object[] parametersInstance = new Object[ parameterDependencies.size() ];

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

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            for( DependencyModel parameterDependency : parameterDependencies )
            {
                if( !visitor.visit( parameterDependency ) )
                {
                    break;
                }
            }
        }
        return visitor.visitLeave( this );
    }

    public Collection<DependencyModel> filter( Specification<DependencyModel> specification )
    {
        ArrayList<DependencyModel> result = new ArrayList<DependencyModel>();
        for( DependencyModel model : parameterDependencies )
        {
            if( specification.satisfiedBy( model ) )
            {
                result.add( model );
            }
        }
        return result;
    }

    @Override
    public String toString()
    {
        return "InjectedParametersModel{" +
               "parameterDependencies=" + parameterDependencies +
               '}';
    }
}

