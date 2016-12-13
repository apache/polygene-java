/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.bootstrap;

/**
 * Polygene runtime factory.
 */
public interface RuntimeFactory
{
    PolygeneRuntime createRuntime();

    /**
     * Standalone application Polygene runtime factory.
     */
    public final class StandaloneApplicationRuntimeFactory
        implements RuntimeFactory
    {
        @Override
        public PolygeneRuntime createRuntime()
        {
            ClassLoader loader = getClass().getClassLoader();
            try
            {
                Class<? extends PolygeneRuntime> runtimeClass = loadRuntimeClass( loader );
                return runtimeClass.newInstance();
            }
            catch( ClassNotFoundException e )
            {
                System.err.println( "Polygene Runtime jar is not present in the classpath." );
            }
            catch( InstantiationException | IllegalAccessException e )
            {
                System.err.println( "Invalid Polygene Runtime class. If you are providing your own Polygene Runtime, please " +
                                    "contact dev@zest.apache.org mailing list for assistance." );
            }
            return null;
        }

        @SuppressWarnings( { "unchecked" } )
        private Class<? extends PolygeneRuntime> loadRuntimeClass( ClassLoader loader )
            throws ClassNotFoundException
        {
            return (Class<? extends PolygeneRuntime>) loader.loadClass( "org.apache.zest.runtime.PolygeneRuntimeImpl" );
        }
    }
}
