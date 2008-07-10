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
package org.qi4j.quikit.application.jetty;

import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.injection.scope.Structure;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.service.ServiceInstanceFactory;
import org.qi4j.service.ServiceInstanceProviderException;

/**
 * @author edward.yakop@gmail.com
 */
public class HelloWorldServletServiceFactory
    implements ServiceInstanceFactory
{
    @Structure
    private CompositeBuilderFactory cbf;

    public final Object newInstance( ServiceDescriptor aDescriptor )
        throws ServiceInstanceProviderException
    {
        return cbf.newComposite( HelloWorldServletService.class );
    }

    public final void releaseInstance( Object anInstance )
        throws ServiceInstanceProviderException
    {
        HelloWorldServletService servlet = (HelloWorldServletService) anInstance;
        servlet.destroy();
    }
}
