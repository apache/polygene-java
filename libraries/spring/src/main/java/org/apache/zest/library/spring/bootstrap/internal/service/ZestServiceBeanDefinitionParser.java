/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.library.spring.bootstrap.internal.service;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import static org.apache.zest.library.spring.bootstrap.Constants.BEAN_ID_ZEST_APPLICATION;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

public final class PolygeneServiceBeanDefinitionParser
    implements BeanDefinitionParser
{
    private static final String SERVICE_ID = "id";

    @Override
    public final BeanDefinition parse( Element anElement, ParserContext aParserContext )
    {
        String serviceId = anElement.getAttribute( SERVICE_ID );

        // Service factory bean
        BeanDefinitionBuilder builder = rootBeanDefinition( ServiceFactoryBean.class );
        builder.addConstructorArgReference( BEAN_ID_ZEST_APPLICATION );
        builder.addConstructorArgValue( serviceId );
        AbstractBeanDefinition definition = builder.getBeanDefinition();

        // Register service factory bean
        BeanDefinitionRegistry definitionRegistry = aParserContext.getRegistry();
        definitionRegistry.registerBeanDefinition( serviceId, definition );

        return definition;
    }
}
