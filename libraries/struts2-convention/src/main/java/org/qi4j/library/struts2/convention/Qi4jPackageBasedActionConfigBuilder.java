package org.qi4j.library.struts2.convention;

import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;
import java.lang.reflect.Modifier;
import org.apache.struts2.convention.PackageBasedActionConfigBuilder;
import org.qi4j.library.struts2.ActionConfiguration;

public class Qi4jPackageBasedActionConfigBuilder
    extends PackageBasedActionConfigBuilder
{

    private final ActionConfiguration actionConfiguration;

    @Inject
    public Qi4jPackageBasedActionConfigBuilder( Configuration configuration, Container container,
                                                ObjectFactory objectFactory,
                                                @Inject( "struts.convention.redirect.to.slash" ) String redirectToSlash,
                                                @Inject( "struts.convention.default.parent.package" ) String defaultParentPackage,
                                                ActionConfiguration actionConfiguration
    )
    {
        super( configuration, container, objectFactory, redirectToSlash, defaultParentPackage );
        this.actionConfiguration = actionConfiguration;
    }

    @Override
    public void buildActionConfigs()
    {
        buildConfiguration( actionConfiguration.getClasses() );
    }

    @Override
    protected boolean cannotInstantiate( Class<?> actionClass )
    {
        return actionClass.isAnnotation()
               || actionClass.isEnum()
               || ( !actionClass.isInterface() && ( actionClass.getModifiers() & Modifier.ABSTRACT ) != 0 );
    }
}
