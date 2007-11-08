package org.qi4j.dependency;

/**
 * If a dependency resolution is static, use this class as a simple holder
 * that always returns the same object.
 */
public class StaticDependencyResolution
    implements DependencyResolution
{
    private Object instance;

    public StaticDependencyResolution( Object object )
    {
        this.instance = object;
    }

    public Object getDependencyInjection( DependencyInjectionContext context )
    {
        return instance;
    }
}
