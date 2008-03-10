/*
 * Copyright 2007 Alin Dreghiciu. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.qi4j.library.auth.tests;

import java.security.PrivilegedAction;
import javax.security.auth.Subject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Concerns;
import org.qi4j.composite.scope.Adapt;
import org.qi4j.library.auth.AuthorizationConcern;
import org.qi4j.library.auth.AuthorizationContextComposite;
import org.qi4j.library.auth.AuthorizationService;
import org.qi4j.library.auth.NamedPermissionComposite;
import org.qi4j.library.auth.ProtectedResource;
import org.qi4j.library.auth.RequiresPermission;
import org.qi4j.library.auth.UserPrincipal;
import org.qi4j.service.ActivationStatusChange;
import org.qi4j.spi.service.ServiceInstance;
import org.qi4j.spi.service.ServiceInstanceProvider;
import org.qi4j.spi.service.ServiceProviderException;
import org.qi4j.spi.structure.ServiceDescriptor;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.Qi4jTestComposite;

public class AuthorizationConcernTest extends AbstractQi4jTest
{

    private static AuthorizationService authorizationService;

    public void configure( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites(
            RoomComposite.class,
            NamedPermissionComposite.class,
            AuthorizationContextComposite.class
        );
        module.addServices( TestServiceProvider.class, AuthorizationService.class );
    }
    
    public void testScript01()
    {
        authorizationService = createMock( AuthorizationService.class );

        replay( authorizationService );

        final RoomComposite room = compositeBuilderFactory.newComposite( RoomComposite.class );
        final Subject subject = new Subject();
        subject.getPrincipals().add( new UserPrincipal() );
        Subject.doAs(
            subject,
            new PrivilegedAction<Object>()
            {
                public Object run()
                {
                    room.enter();
                    return null;
                }
            }
        );

        verify( authorizationService );
    }

    @Concerns( AuthorizationConcern.class )
    private static interface RoomComposite extends Qi4jTestComposite, ProtectedResource
    {
        @RequiresPermission( "Enter" ) void enter();

        void exit();
    }

    public static class TestServiceProvider
        implements ServiceInstanceProvider
    {

        ServiceDescriptor descriptor;

        public void init( @Adapt ServiceDescriptor serviceDescriptor )
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public ServiceInstance getInstance() throws ServiceProviderException
        {
            return new ServiceInstance(
                authorizationService,
                this,
                null
            );
        }

        public void releaseInstance( ServiceInstance instance ) throws Exception
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void onActivationStatusChange( ActivationStatusChange change ) throws Exception
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

}