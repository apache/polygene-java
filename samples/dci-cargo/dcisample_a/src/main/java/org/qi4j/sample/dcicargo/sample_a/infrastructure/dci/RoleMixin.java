/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_a.infrastructure.dci;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;

/**
 * Methodful Role implementation base class
 *
 * Helps "inject" the Context object into the Role Player.
 */
public abstract class RoleMixin<T extends Context>
{
    public T context;

    // Other common role services/methods could be added here...

    @Structure
    public UnitOfWorkFactory uowf;

    @Structure
    public QueryBuilderFactory qbf;

    @Structure
    public ValueBuilderFactory vbf;

    /**
     * setContext is called with method invocation in {@link Context#setContext(Object, Context)}
     * (therefore "never used" according to IDE)
     */
    public void setContext( T context )
    {
        this.context = context;
    }
}
