/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.shiro.bootstrap;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.object.ObjectFactory;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;

@Mixins( ShiroLifecycleService.Mixin.class )
@Activators( ShiroLifecycleService.Activator.class )
public interface ShiroLifecycleService
        extends ServiceComposite
{

    void initialize()
            throws Exception;

    class Activator
            extends ActivatorAdapter<ServiceReference<ShiroLifecycleService>>
    {

        @Override
        public void afterActivation( ServiceReference<ShiroLifecycleService> activated )
                throws Exception
        {
            activated.get().initialize();
        }

    }

    abstract class Mixin
            implements ShiroLifecycleService
    {

        @Structure
        private ObjectFactory obf;

        @Override
        public void initialize()
                throws Exception
        {
            if ( Security.getProvider( BouncyCastleProvider.PROVIDER_NAME ) == null ) {
                Security.addProvider( new BouncyCastleProvider() );
            }
            obf.newObject( RealmActivator.class ).activateRealm();
        }

    }

}
