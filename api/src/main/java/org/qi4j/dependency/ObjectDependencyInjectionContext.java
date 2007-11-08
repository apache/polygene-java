package org.qi4j.dependency;

import java.util.Map;
import org.qi4j.PropertyValue;

/**
 * TODO
 */
public class ObjectDependencyInjectionContext
    extends DependencyInjectionContext
{
    private Map<InjectionKey, PropertyValue> properties;
    private Map<InjectionKey, Object> adapt;
    private Map<InjectionKey, Object> decorate;

    public ObjectDependencyInjectionContext( Map<InjectionKey, PropertyValue> properties, Map<InjectionKey, Object> adapt, Map<InjectionKey, Object> decorate )
    {
        this.properties = properties;
        this.adapt = adapt;
        this.decorate = decorate;
    }

    public Map<InjectionKey, PropertyValue> getProperties()
    {
        return properties;
    }

    public Map<InjectionKey, Object> getAdapt()
    {
        return adapt;
    }

    public Map<InjectionKey, Object> getDecorate()
    {
        return decorate;
    }
}