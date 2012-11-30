/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.api.object;

import org.qi4j.api.common.InvalidApplicationException;

/**
 * This exception is thrown if no visible Object of the requested type can be found.
 */
public class NoSuchObjectException
    extends InvalidApplicationException
{
    private static final long serialVersionUID = -1121690536365682511L;

    private final String objectType;
    private final String moduleName;

    public NoSuchObjectException( String type, String moduleName )
    {
        super( "Could not find any visible Object of type [" + type + "] in module [" +
               moduleName + "]." );
        this.objectType = type;
        this.moduleName = moduleName;
    }

    public String objectType()
    {
        return objectType;
    }

    public String moduleName()
    {
        return moduleName;
    }
}