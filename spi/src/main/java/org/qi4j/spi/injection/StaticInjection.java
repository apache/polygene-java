package org.qi4j.spi.injection;

/**
 * If a dependency injection is static, use this class as a simple holder
 * that always returns the same object.
 */
public class StaticInjection
    implements InjectionProvider
{
    private Object instance;

    public StaticInjection( Object object )
    {
        this.instance = object;
    }

    public Object provideInjection( InjectionContext context )
    {
        return instance;
    }
}
