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
package org.qi4j.library.spring.bootstrap.internal.application;

import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import static org.qi4j.library.spring.bootstrap.Constants.*;
import org.qi4j.library.spring.bootstrap.Qi4jApplicationBootstrap;
import org.qi4j.structure.Application;
import org.springframework.beans.BeanInstantiationException;
import static org.springframework.beans.BeanUtils.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.*;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import static org.springframework.util.Assert.*;
import static org.springframework.util.ClassUtils.*;
import org.w3c.dom.Element;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class Qi4jBootstrapBeanDefinitionParser
    implements BeanDefinitionParser

{
    private static final String CLASS = "class";

    public final BeanDefinition parse( Element anElement, ParserContext aParserContext )
    {
        Qi4jApplicationBootstrap bootstrap = createQi4jApplicationBootstrap( anElement, aParserContext );
        Application application = createQi4jApplication( anElement, aParserContext, bootstrap );
        AbstractBeanDefinition factoryBeanDefinition = createQi4jApplicationFactoryBeanDefinition( application );
        registerBean( aParserContext, factoryBeanDefinition );
        return factoryBeanDefinition;
    }

    private Qi4jApplicationBootstrap createQi4jApplicationBootstrap( Element anElement, ParserContext aParserContext )
    {
        String bootstrapClassString = anElement.getAttribute( CLASS );
        hasText( bootstrapClassString );
        XmlReaderContext readerContext = aParserContext.getReaderContext();

        Class bootstrapClass;
        try
        {
            bootstrapClass = forName( bootstrapClassString );
        }
        catch( ClassNotFoundException e )
        {
            readerContext.error( "Qi4j bootstrap class [" + bootstrapClassString + "] is not found.", anElement );
            return null;
        }

        if( !Qi4jApplicationBootstrap.class.isAssignableFrom( bootstrapClass ) )
        {
            readerContext.error(
                CLASS + "attribute is not an instance of [" + Qi4jApplicationBootstrap.class.getName() + "] class"
                , anElement
            );
            return null;
        }

        Qi4jApplicationBootstrap bootstrap = null;
        try
        {
            bootstrap = (Qi4jApplicationBootstrap) instantiateClass( bootstrapClass );
        }
        catch( BeanInstantiationException e )
        {
            readerContext.error(
                "Fail to instantiate qi4j bootstrap class [" + bootstrapClassString + "]", anElement, e
            );
        }
        return bootstrap;
    }


    private Application createQi4jApplication(
        Element anElement, ParserContext aParserContext, Qi4jApplicationBootstrap aBootstrap
    )
    {
        if( aBootstrap == null )
        {
            return null;
        }

        Energy4Java energy4Java = new Energy4Java();
        ApplicationAssembly applicationAssembly = energy4Java.newApplicationAssembly();

        try
        {
            aBootstrap.assemble( applicationAssembly );
            return energy4Java.newApplication( applicationAssembly );
        }
        catch( AssemblyException e )
        {
            XmlReaderContext readerContext = aParserContext.getReaderContext();
            readerContext.error( "Fail to bootstrap qi4j application.", anElement, e );
            return null;
        }
    }

    private AbstractBeanDefinition createQi4jApplicationFactoryBeanDefinition( Application anApplication )
    {
        BeanDefinitionBuilder builder = rootBeanDefinition( Qi4jApplicationFactoryBean.class );
        builder.addConstructorArgValue( anApplication );
        return builder.getBeanDefinition();
    }

    private void registerBean( ParserContext aParserContext, BeanDefinition aBeanDefinition )
    {
        BeanDefinitionRegistry registry = aParserContext.getRegistry();
        registry.registerBeanDefinition( BEAN_ID_QI4J_APPLICATION, aBeanDefinition );
    }
}