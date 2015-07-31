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

import org.qi4j.library.spring.bootstrap.internal.application.Qi4jBootstrapBeanDefinitionParser;
import org.qi4j.library.spring.bootstrap.internal.service.Qi4jServiceBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public final class Qi4jNamespaceHandler extends NamespaceHandlerSupport
{
    @Override
    public final void init()
    {
        registerBeanDefinitionParser( "bootstrap", new Qi4jBootstrapBeanDefinitionParser() );
        registerBeanDefinitionParser( "service", new Qi4jServiceBeanDefinitionParser() );
    }
}
