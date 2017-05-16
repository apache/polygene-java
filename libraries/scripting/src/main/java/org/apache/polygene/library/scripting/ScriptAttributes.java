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
package org.apache.polygene.library.scripting;

/**
 * ScriptAttributes is the interface into the attributes of the underlying
 * scripting engine.
 */
public interface ScriptAttributes
{
    /**
     * Fetch the value of the named attribute.
     * <p>
     *     If the attribute name is found in ENGINE scope, it is returned. If it is not in the ENGINE scope,
     *     then the value is fetched from GLOBAL scope. If not present in either scope, NULL is returned.
     * </p>
     * @param name The name of the attribute to get, or null if not found.
     * @return The value of the attribute.
     */
    Object getAttribute( String name );

    /**
     * This interface is for accessing both ENGINE and GLOBAL scopes with precision.
     */
    interface All
    {
        Object getEngineAttribute( String name );

        Object getGlobalAttribute( String name );

        void setEngineAttribute( String name, Object value );

        void setGlobalAttribute( String name, Object value );
    }
}
