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

package org.qi4j.runtime.structure;

import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectedFieldModel;
import org.qi4j.runtime.injection.InjectedParametersModel;

/**
 * JAVADOC
 */
public abstract class DependencyVisitor
    extends ModelVisitor
{
    private final Specification<DependencyModel> specification;

    public DependencyVisitor( Specification<DependencyModel> specification )
    {
        this.specification = specification;
    }

    @Override
    public void visit( InjectedParametersModel injectedParametersModel )
    {
        for( DependencyModel dependency : injectedParametersModel.filter( specification ) )
        {
            visitDependency( dependency );
        }
    }

    @Override
    public void visit( InjectedFieldModel injectedFieldModel )
    {
        for( DependencyModel dependency : injectedFieldModel.filter( specification ) )
        {
            visitDependency( dependency );
        }
    }

    public abstract void visitDependency( DependencyModel dependencyModel );
}
