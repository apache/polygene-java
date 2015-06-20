/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.bootstrap;

/**
 * Qi4j runtime factory.
 */
public interface RuntimeFactory
{
    Qi4jRuntime createRuntime();

    /**
     * Standalone application Qi4j runtime factory.
     */
    public final class StandaloneApplicationRuntimeFactory
        implements RuntimeFactory
    {
        @Override
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
            catch( InstantiationException | IllegalAccessException e )
            {
                System.err.println( "Invalid Qi4j Runtime class. If you are providing your own Qi4j Runtime, please " +
                                    "contact qi4j-dev at Google Groups for assistance." );
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
