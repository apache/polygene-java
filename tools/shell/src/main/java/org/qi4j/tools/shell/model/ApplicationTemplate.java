package org.qi4j.tools.shell.model;

import java.util.Map;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.tools.shell.templating.TemplateEngine;

@Mixins(ApplicationTemplate.Mixin.class)
public interface ApplicationTemplate extends Nameable
{
    String evaluate( Map<String, String> variables );
    
    interface State
    {
        Property<String> template();
    }
    
    public class Support
    {
        public static ApplicationTemplate get( UnitOfWork uow, String name )
        {
            return uow.get( ApplicationTemplate.class, identity(name) );
        }

        public static String identity( String name )
        {
            return "Template:" + name;
        }
    }
    
    abstract class Mixin
        implements ApplicationTemplate
    {
        @This
        private State state;

        @Override
        public String evaluate( Map<String, String> variables )
        {
            TemplateEngine engine = new TemplateEngine( state.template().get() );
            return engine.create( variables );
        }
    }
}
