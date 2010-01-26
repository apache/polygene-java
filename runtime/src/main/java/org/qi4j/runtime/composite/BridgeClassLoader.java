/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.composite;

import net.sf.cglib.proxy.Enhancer;

/**
 * JAVADOC
 */
class BridgeClassLoader
    extends ClassLoader
{
    private static final String CGLIB_PACKAGE_NAME = "net.sf.cglib";
    private static final ClassLoader CGLIB_CLASS_LOADER = Enhancer.class.getClassLoader();

    BridgeClassLoader( ClassLoader mixinClassLoader )
    {
        super( mixinClassLoader );
    }

    @Override
    protected Class<?> loadClass( String aClassName, boolean isResolve )
        throws ClassNotFoundException
    {
        if( aClassName.startsWith( CGLIB_PACKAGE_NAME ) )
        {
            Class<?> clazz = CGLIB_CLASS_LOADER.loadClass( aClassName );
            if( isResolve )
            {
                resolveClass( clazz );
            }

            return clazz;
        }

        return super.loadClass( aClassName, isResolve );
    }
}
