package org.qi4j.lib.struts2;

import static org.qi4j.lib.struts2.Constants.SERVLET_ATTRIBUTE;

import javax.servlet.ServletContext;

import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.object.ObjectBuilderFactory;
import org.qi4j.structure.Module;

import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.inject.ContainerBuilder;
import com.opensymphony.xwork2.inject.Context;
import com.opensymphony.xwork2.inject.Factory;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.inject.Scope;
import com.opensymphony.xwork2.util.location.LocatableProperties;

public class Qi4jContainerProvider {

    private Module module;
    private boolean registered;

    @Inject
    public void setServletContext( ServletContext aServletContext ) {
        module = (Module) aServletContext.getAttribute( SERVLET_ATTRIBUTE );
    }

    public void register(ContainerBuilder builder, LocatableProperties props) throws ConfigurationException {
        builder.factory(UnitOfWorkFactory.class, new Factory<UnitOfWorkFactory>() {
            public UnitOfWorkFactory create(Context context) throws Exception {
                return module.unitOfWorkFactory();
            }
        }, Scope.SINGLETON);
        builder.factory(CompositeBuilderFactory.class, new Factory<CompositeBuilderFactory>() {
            public CompositeBuilderFactory create(Context context) throws Exception {
                return module.compositeBuilderFactory();
            }
        }, Scope.SINGLETON);
        builder.factory(ObjectBuilderFactory.class, new Factory<ObjectBuilderFactory>() {
            public ObjectBuilderFactory create(Context context) throws Exception {
                return module.objectBuilderFactory();
            }            
        });
        registered = true;
    }

    public boolean needsReload() {
        return !registered;
    }
    
    public void init(com.opensymphony.xwork2.config.Configuration configuration) throws ConfigurationException {}
    public void destroy() {}
}
