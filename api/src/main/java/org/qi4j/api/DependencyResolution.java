package org.qi4j.api;

/**
 * Implemenatations of this interface represents a resolved depedency, where a
 * DependencyKey has been successfully mapped to some result.
 * <p/>
 * The getResolvedDependency method is called once for every fragment element to be injected
 * with the dependency.
 */
public interface DependencyResolution
{
    /**
     * Get the resolved dependency, given the actual thisAs and the thisAs context.
     * <p/>
     * The resulting iterable may return an iterator that gives different results on each
     * invocation. This allows for dynamic updates of the resolved object result during the
     * lifetime of the injected fragment.
     *
     * @param context
     * @return iterable of result. May not be null.
     */
    Iterable getDependencyInjection( DependencyInjectionContext context );
}
