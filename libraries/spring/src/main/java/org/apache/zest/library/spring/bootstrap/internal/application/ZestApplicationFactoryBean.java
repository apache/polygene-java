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
package org.apache.zest.library.spring.bootstrap.internal.application;

import org.apache.zest.api.structure.Application;
import org.apache.zest.bootstrap.*;
import org.apache.zest.library.spring.bootstrap.ZestApplicationBootstrap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

/**
 * This class responsible to handle the lifecycle of Zest application.
 */
public final class ZestApplicationFactoryBean
        implements FactoryBean, DisposableBean, InitializingBean, ApplicationContextAware
{

    private final ZestApplicationBootstrap applicationBootstrap;

    private Application application;

    public ZestApplicationFactoryBean( final ZestApplicationBootstrap applicationBootstrap )
    {
        Assert.notNull( applicationBootstrap, "'applicationBootstrap' must not be null" );
        this.applicationBootstrap = applicationBootstrap;
    }

    @Override
    public final Application getObject() throws Exception
    {
        if ( this.application == null )
        {
            this.application = this.createApplication();
        }
        return this.application;
    }

    @Override
    public final Class<Application> getObjectType()
    {
        return Application.class;
    }

    @Override
    public final boolean isSingleton()
    {
        return true;
    }

    @Override
    public final void destroy() throws Exception
    {
        this.getObject().passivate();
    }

    @Override
    public final void afterPropertiesSet() throws Exception
    {
        this.getObject().activate();
    }

    private Application createApplication()
    {
        Energy4Java energy4Java = new Energy4Java();
        try
        {
            return energy4Java.newApplication( new ApplicationAssembler()
            {

                @Override
                public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                        throws AssemblyException
                {
                    final ApplicationAssembly applicationAssembly = applicationFactory.newApplicationAssembly();
                    ZestApplicationFactoryBean.this.applicationBootstrap.assemble( applicationAssembly );
                    return applicationAssembly;
                }
            } );
        } catch ( AssemblyException e )
        {
            throw new BeanInitializationException( "Fail to bootstrap Zest application.", e );
        }

    }

    @Override
    public void setApplicationContext( final ApplicationContext applicationContext ) throws BeansException
    {
        if ( this.applicationBootstrap instanceof ApplicationContextAware )
        {
            // propagate application context to the application bootstrap
            ApplicationContextAware aware = (ApplicationContextAware) this.applicationBootstrap;
            aware.setApplicationContext( applicationContext );
        }
    }
}
