package org.qi4j.library.spring;

import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.library.spring.bootstrap.Qi4jApplicationBootstrap;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

// START SNIPPET: code
public class MyZestBootstrapper extends Qi4jApplicationBootstrap
        implements ApplicationContextAware
{
    private ApplicationContext applicationContext;

    @Override
    public void assemble(ApplicationAssembly assembly) throws AssemblyException
    {
        // Normal assembly of an application.
// END SNIPPET: code
// START SNIPPET: code
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

}
// END SNIPPET: code
