/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.library.spring.bootstrap.internal;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;

/**
 * @author edward.yakop@gmail.com
 */
public final class FactoryBeanDelegator
    implements FactoryBean
{
    private final FactoryBean factory;

    public FactoryBeanDelegator( FactoryBean aFactory )
    {
        Assert.notNull( aFactory, "Argument [aFactory] must not be null." );
        factory = aFactory;
    }

    public final Object getObject()
        throws Exception
    {
        return factory.getObject();
    }

    public final Class getObjectType()
    {
        return factory.getObjectType();
    }

    public final boolean isSingleton()
    {
        return factory.isSingleton();
    }
}
