package org.qi4j.bootstrap;

public interface RuntimeFactory
{
    Qi4jRuntime createRuntime();

    public final class StandaloneApplicationRuntimeFactory
        implements RuntimeFactory
    {
        public Qi4jRuntime createRuntime()
        {
            ClassLoader loader = getClass().getClassLoader();
            try
            {
                Class<? extends Qi4jRuntime> runtimeClass = loadRuntimeClass( loader );
                return runtimeClass.newInstance();
            }
            catch( ClassNotFoundException e )
            {
                System.err.println( "Qi4j Runtime jar is not present in the classpath." );
            }
            catch( InstantiationException e )
            {
                System.err.println( "Invalid Qi4j Runtime class. If you are providing your own Qi4j Runtime, please " +
                                    "contact qi4j-dev@lists.ops4j.org for assistance." );
            }
            catch( IllegalAccessException e )
            {
                System.err.println( "Invalid Qi4j Runtime class. If you are providing your own Qi4j Runtime, please " +
                                    "contact qi4j-dev@lists.ops4j.org for assistance." );
            }
            return null;
        }

        @SuppressWarnings( { "unchecked" } )
        private Class<? extends Qi4jRuntime> loadRuntimeClass( ClassLoader loader )
            throws ClassNotFoundException
        {
            return (Class<? extends Qi4jRuntime>) loader.loadClass( "org.qi4j.runtime.Qi4jRuntimeImpl" );
        }
    }
}
