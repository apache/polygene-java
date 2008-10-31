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

import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.library.spring.bootstrap.Qi4jApplicationBootstrap;
import org.qi4j.service.ServiceFinder;
import org.qi4j.service.ServiceReference;
import org.qi4j.structure.Application;
import org.qi4j.structure.Module;
import org.springframework.beans.BeanInstantiationException;
import static org.springframework.beans.BeanUtils.instantiateClass;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import static org.springframework.util.Assert.hasText;
import org.springframework.util.ClassUtils;
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
        registerFactoryBeanServiceAsSpringBean( anElement, aParserContext, bootstrap, application );

        AbstractBeanDefinition applicationFactoryBeanDefinition = createQi4jApplicationFactoryBean( application );
        registerBeanWithGeneratedName( aParserContext, applicationFactoryBeanDefinition );

        return applicationFactoryBeanDefinition;
    }


    private Qi4jApplicationBootstrap createQi4jApplicationBootstrap( Element anElement, ParserContext aParserContext )
    {
        String bootstrapClassString = anElement.getAttribute( CLASS );
        hasText( bootstrapClassString );
        XmlReaderContext readerContext = aParserContext.getReaderContext();

        Class bootstrapClass;
        try
        {
            bootstrapClass = ClassUtils.forName( bootstrapClassString );
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
            Application application = energy4Java.newApplication( applicationAssembly );

            String qi4jLayer = aBootstrap.layerName();
            String qi4jModule = aBootstrap.moduleName();
            application.findModule( qi4jLayer, qi4jModule );

            return application;
        }
        catch( AssemblyException e )
        {
            XmlReaderContext readerContext = aParserContext.getReaderContext();
            readerContext.error( "Fail to bootstrap qi4j application.", anElement, e );
            return null;
        }
    }

    private void registerFactoryBeanServiceAsSpringBean(
        Element anElement,
        ParserContext aParserContext,
        Qi4jApplicationBootstrap aQi4jApplicationBootstrap,
        Application aQi4jApplication )
    {
        if( aQi4jApplication == null )
        {
            return;
        }

        String layerString = aQi4jApplicationBootstrap.layerName();
        String moduleString = aQi4jApplicationBootstrap.moduleName();
        Module module = aQi4jApplication.findModule( layerString, moduleString );
        if( module == null )
        {
            aParserContext.getReaderContext().error(
                "Layer [" + layerString + "] module [" + module + "] is not found.", anElement
            );
            return;
        }

        ServiceFinder serviceFinder = module.serviceFinder();
        Iterable<ServiceReference<FactoryBean>> serviceRefs = serviceFinder.findServices( FactoryBean.class );

        BeanDefinitionRegistry registry = aParserContext.getRegistry();
        for( ServiceReference<FactoryBean> serviceRef : serviceRefs )
        {
            registerFactoryBean( registry, serviceRef );
        }
    }

    private void registerFactoryBean( BeanDefinitionRegistry aRegistry, ServiceReference<FactoryBean> aReference )
    {
        // Factory bean delegator
        BeanDefinitionBuilder builder = rootBeanDefinition( FactoryBeanDelegator.class );
        FactoryBean factory = aReference.get();
        builder.addConstructorArgValue( factory );
        AbstractBeanDefinition definition = builder.getBeanDefinition();

        // Identity
        String identity = aReference.identity();
        aRegistry.registerBeanDefinition( identity, definition );
    }

    private AbstractBeanDefinition createQi4jApplicationFactoryBean( Application anApplication )
    {
        BeanDefinitionBuilder builder = rootBeanDefinition( Qi4jApplicationFactoryBean.class );
        builder.addConstructorArgValue( anApplication );
        return builder.getBeanDefinition();
    }

    private void registerBeanWithGeneratedName( ParserContext aParserContext, BeanDefinition aBeanDefinition )
    {
        XmlReaderContext readerContext = aParserContext.getReaderContext();
        String beanName = readerContext.generateBeanName( aBeanDefinition );
        BeanDefinitionRegistry registry = aParserContext.getRegistry();
        registry.registerBeanDefinition( beanName, aBeanDefinition );
    }
}