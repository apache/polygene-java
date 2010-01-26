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
package org.qi4j.rest;

import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.service.Activatable;
import org.restlet.Component;
import org.restlet.data.Protocol;

public class RestServerMixin
    implements Activatable
{
    @Structure
    private TransientBuilderFactory cbf;
    @Structure
    private ObjectBuilderFactory obf;

    private Component component;

    public void activate()
        throws Exception
    {
        component = new Component();
        component.getServers().add( Protocol.HTTP, 8182 );
        ObjectBuilder<RestApplication> builder = obf.newObjectBuilder( RestApplication.class );
        builder.use( component.getContext() );
        RestApplication application = builder.newInstance();
        component.getDefaultHost().attach( application );
        component.start();
    }

    public void passivate()
        throws Exception
    {
        component.stop();
    }
}
