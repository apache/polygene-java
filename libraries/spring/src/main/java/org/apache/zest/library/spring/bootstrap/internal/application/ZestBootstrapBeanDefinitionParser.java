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
package org.apache.zest.library.spring.bootstrap.internal.application;

import org.apache.zest.library.spring.bootstrap.ZestApplicationBootstrap;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.w3c.dom.Element;

import static org.apache.zest.library.spring.bootstrap.Constants.BEAN_ID_ZEST_APPLICATION;
import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.ClassUtils.forName;

public final class ZestBootstrapBeanDefinitionParser
        implements BeanDefinitionParser
{

    private static final String CLASS = "class";

    @Override
    public final BeanDefinition parse( Element anElement, ParserContext aParserContext )
    {
        ZestApplicationBootstrap bootstrap = createZestApplicationBootstrap( anElement, aParserContext );
        AbstractBeanDefinition factoryBeanDefinition = createZestApplicationFactoryBeanDefinition( bootstrap );
        registerBean( aParserContext, factoryBeanDefinition );
        return factoryBeanDefinition;
    }

    private ZestApplicationBootstrap createZestApplicationBootstrap( Element anElement, ParserContext aParserContext )
    {
        String bootstrapClassString = anElement.getAttribute( CLASS );
        hasText( bootstrapClassString );
        XmlReaderContext readerContext = aParserContext.getReaderContext();

        Class<?> bootstrapClass;
        try
        {
            bootstrapClass = forName( bootstrapClassString, getClass().getClassLoader() );
        } catch ( ClassNotFoundException e )
        {
            readerContext.error( "Zest bootstrap class [" + bootstrapClassString + "] is not found.", anElement );
            return null;
        }

        if ( !ZestApplicationBootstrap.class.isAssignableFrom( bootstrapClass ) )
        {
            readerContext.error( CLASS + "attribute is not an instance of [" + ZestApplicationBootstrap.class.getName()
                    + "] class", anElement );
            return null;
        }

        ZestApplicationBootstrap bootstrap = null;
        try
        {
            bootstrap = (ZestApplicationBootstrap) instantiateClass( bootstrapClass );
        } catch ( BeanInstantiationException e )
        {
            readerContext.error( "Fail to instantiate Zest bootstrap class [" + bootstrapClassString + "]", anElement,
                    e );
        }
        return bootstrap;
    }

    private AbstractBeanDefinition createZestApplicationFactoryBeanDefinition(
        final ZestApplicationBootstrap applicationBootstrap
    )
    {
        BeanDefinitionBuilder builder = rootBeanDefinition( ZestApplicationFactoryBean.class );
        builder.addConstructorArgValue( applicationBootstrap );
        return builder.getBeanDefinition();
    }

    private void registerBean( ParserContext aParserContext, BeanDefinition aBeanDefinition )
    {
        BeanDefinitionRegistry registry = aParserContext.getRegistry();
        registry.registerBeanDefinition( BEAN_ID_ZEST_APPLICATION, aBeanDefinition );
    }
}