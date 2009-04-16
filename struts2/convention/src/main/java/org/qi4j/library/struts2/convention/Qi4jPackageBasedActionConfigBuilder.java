package org.qi4j.library.struts2.convention;

import java.lang.reflect.Modifier;

import org.apache.struts2.convention.ActionNameBuilder;
import org.apache.struts2.convention.InterceptorMapBuilder;
import org.apache.struts2.convention.PackageBasedActionConfigBuilder;
import org.apache.struts2.convention.ResultMapBuilder;
import org.qi4j.library.struts2.ActionConfiguration;

import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.inject.Inject;

public class Qi4jPackageBasedActionConfigBuilder extends PackageBasedActionConfigBuilder {

    private final ActionConfiguration actionConfiguration;
    
    @Inject
    public Qi4jPackageBasedActionConfigBuilder(Configuration configuration, ActionNameBuilder actionNameBuilder,
            ResultMapBuilder resultMapBuilder, InterceptorMapBuilder interceptorMapBuilder, ObjectFactory objectFactory,
            @Inject("struts.convention.redirect.to.slash") String redirectToSlash,
            @Inject("struts.convention.default.parent.package") String defaultParentPackage,
            ActionConfiguration actionConfiguration) {
        super(configuration, actionNameBuilder, resultMapBuilder, interceptorMapBuilder, objectFactory, redirectToSlash,
                defaultParentPackage);
        this.actionConfiguration = actionConfiguration;
    }

    
    @Override
    public void buildActionConfigs() {
        buildConfiguration(actionConfiguration.getClasses());
    }
    
    protected boolean cannotInstantiate(Class<?> actionClass) {
        return actionClass.isAnnotation()
            || actionClass.isEnum()
            || (!actionClass.isInterface() && (actionClass.getModifiers() & Modifier.ABSTRACT) != 0);
    }
}
