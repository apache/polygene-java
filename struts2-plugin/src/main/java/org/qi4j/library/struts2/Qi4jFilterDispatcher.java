package org.qi4j.library.struts2;

import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.ContainerProvider;
import com.opensymphony.xwork2.inject.ContainerBuilder;
import com.opensymphony.xwork2.inject.Context;
import com.opensymphony.xwork2.inject.Factory;
import com.opensymphony.xwork2.util.location.LocatableProperties;
import javax.servlet.FilterConfig;
import org.apache.struts2.config.BeanSelectionProvider;
import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.FilterDispatcher;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import static org.qi4j.library.struts2.Constants.*;

public class Qi4jFilterDispatcher
    extends FilterDispatcher
{
    @Override
    protected Dispatcher createDispatcher( final FilterConfig filterConfig )
    {
        Dispatcher dispatcher = super.createDispatcher( filterConfig );
        ConfigurationManager configurationManager = createConfigurationManager( filterConfig );
        dispatcher.setConfigurationManager( configurationManager );
        return dispatcher;
    }

    protected ConfigurationManager createConfigurationManager( FilterConfig filterConfig )
    {
        ConfigurationManager configurationManager = new ConfigurationManager( BeanSelectionProvider.DEFAULT_BEAN_NAME );
        configurationManager.addContainerProvider( new Qi4jContainerProvider( module( filterConfig ) ) );
        return configurationManager;
    }

    private Module module( FilterConfig filterConfig )
    {
        return (Module) filterConfig.getServletContext().getAttribute( SERVLET_ATTRIBUTE );
    }

    class Qi4jContainerProvider
        implements ContainerProvider
    {
        private final Module module;
        private boolean registered = false;

        Qi4jContainerProvider( Module aModule )
        {
            module = aModule;
        }

        public void register( ContainerBuilder builder, LocatableProperties props )
            throws ConfigurationException
        {
            factory( builder, UnitOfWorkFactory.class, module.unitOfWorkFactory() );
            factory( builder, ObjectBuilderFactory.class, module.objectBuilderFactory() );
            factory( builder, TransientBuilderFactory.class, module.transientBuilderFactory() );
            factory( builder, ActionConfiguration.class, actionConfiguration() );
            registered = true;
        }

        public boolean needsReload()
        {
            return !registered;
        }

        public void init( Configuration configuration )
            throws ConfigurationException
        {
        }

        public void destroy()
        {
        }

        private <T> void factory( ContainerBuilder builder, Class<T> type, final T value )
        {
            builder.factory( type, new Factory<T>()
            {
                public T create( Context context )
                {
                    return value;
                }
            } );
        }

        private ActionConfiguration actionConfiguration()
        {
            return module.serviceFinder().findService( ActionService.class ).metaInfo( ActionConfiguration.class );
        }
    }
}
