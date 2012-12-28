/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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
package org.qi4j.library.rest.admin;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.restlet.Component;
import org.restlet.data.Protocol;

public abstract class RestServerMixin
    implements RestServerComposite
{
    @Structure
    private Module module;

    private Component component;

    @Override
    public void startServer()
        throws Exception
    {
        component = new Component();
        component.getServers().add( Protocol.HTTP, 8182 );
        RestApplication application = module.newObject( RestApplication.class, component.getContext() );
        component.getDefaultHost().attach( application );
        component.start();
    }

    @Override
    public void stopServer()
        throws Exception
    {
        component.stop();
    }
}
