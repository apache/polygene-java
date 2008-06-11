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

import java.lang.annotation.Annotation;
import org.qi4j.runtime.structure.ModelVisitor;

/**
 * TODO
 */
public abstract class DependencyVisitor
    extends ModelVisitor
{
    Class<? extends Annotation> scope;

    public DependencyVisitor( Class<? extends Annotation> scope )
    {
        this.scope = scope;
    }

    public DependencyVisitor()
    {
    }

    @Override public void visit( InjectedParametersModel injectedParametersModel )
    {
        for( DependencyModel dependencyModel : injectedParametersModel.dependencies() )
        {
            if( scope != null && scope.equals( dependencyModel.injectionAnnotation().annotationType() ) )
            {
                visitDependency( dependencyModel );
            }
        }
    }

    @Override public void visit( InjectedFieldModel injectedFieldModel )
    {
        DependencyModel dependencyModel = injectedFieldModel.dependency();
        if( scope != null && scope.equals( dependencyModel.injectionAnnotation().annotationType() ) )
        {
            visitDependency( dependencyModel );
        }
    }

    public abstract void visitDependency( DependencyModel dependencyModel );
}
