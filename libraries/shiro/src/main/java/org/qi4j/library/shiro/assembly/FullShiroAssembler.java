/*
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.shiro.assembly;

import org.apache.shiro.env.Environment;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.util.Initializable;
import org.apache.shiro.util.ThreadContext;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.shiro.full.Qi4jDefaultSecurityManager;
import org.qi4j.library.shiro.full.Qi4jEnvironment;

/**
 * Assembly for full Qi4j enabled Shiro use.
 */
public abstract class FullShiroAssembler
        implements Assembler
{

    private Visibility visibility = Visibility.module;

    public FullShiroAssembler withVisibility( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    public static class ShiroInitializableActivator
            extends ActivatorAdapter<ServiceReference<Object>>
    {

        @Override
        public void afterActivation( ServiceReference<Object> activated )
                throws Exception
        {
            Object service = activated.get();
            if ( service instanceof Initializable ) {
                ( ( Initializable ) service ).init();
            }
        }

    }

    public static class SecurityManagerActivator
            extends ActivatorAdapter<ServiceReference<SecurityManager>>
    {

        @Override
        public void afterActivation( ServiceReference<SecurityManager> activated )
                throws Exception
        {
            ThreadContext.bind( activated.get() );
        }

        @Override
        public void afterPassivation( ServiceReference<SecurityManager> passivated )
                throws Exception
        {
            ThreadContext.unbindSubject();
            ThreadContext.unbindSecurityManager();
        }

    }

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        // SecurityManager
        module.services( SecurityManager.class ).
                withMixins( securityManagerMixin() ).
                withActivators( ShiroInitializableActivator.class,
                                SecurityManagerActivator.class ).
                instantiateOnStartup().
                visibleIn( visibility );

        //module.services( SessionManager.class ).withMixins( sessionManagerMixin() );
        //module.services( Environment.class ).withMixins( environmentManagerMixin() );

        // User assembly
        assembleShiro( module );

        // TODO Handle Shiro Initializable interface
    }

    protected Class<? extends SecurityManager> securityManagerMixin()
    {
        return Qi4jDefaultSecurityManager.class;
    }

    private Class<? extends SessionManager> sessionManagerMixin()
    {
        return DefaultSessionManager.class;
    }

    private Class<? extends Environment> environmentManagerMixin()
    {
        return Qi4jEnvironment.class;
    }

    /**
     * Implement this method in order to assemble your realms and any other Shiro customization you may need.
     */
    protected abstract void assembleShiro( ModuleAssembly module );

}
