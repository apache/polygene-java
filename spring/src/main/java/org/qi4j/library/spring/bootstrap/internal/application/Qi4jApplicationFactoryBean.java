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
package org.qi4j.library.spring.bootstrap.internal.application;

import org.qi4j.api.structure.Application;
import org.qi4j.spi.structure.ApplicationSPI;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * This class responsible to handle the lifecycle of qi4j application.
 *
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class Qi4jApplicationFactoryBean
    implements FactoryBean, DisposableBean, InitializingBean
{
    private final ApplicationSPI application;

    public Qi4jApplicationFactoryBean( ApplicationSPI qi4jApplication )
    {
        application = qi4jApplication;
    }

    public final Object getObject()
        throws Exception
    {
        return application;
    }

    public final Class getObjectType()
    {
        return Application.class;
    }

    public final boolean isSingleton()
    {
        return true;
    }

    public final void destroy()
        throws Exception
    {
        application.passivate();
    }

    public final void afterPropertiesSet()
        throws Exception
    {
        application.activate();
    }
}
