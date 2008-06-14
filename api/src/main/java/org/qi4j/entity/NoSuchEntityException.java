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
package org.qi4j.entity;

import org.qi4j.composite.InvalidApplicationException;

public class NoSuchEntityException extends InvalidApplicationException
{
    private static final long serialVersionUID = 7185723686654157891L;

    private String entityType;
    private String moduleName;

    public NoSuchEntityException( String entityType, String moduleName )
    {
        super( "Could not find any visible EntityComposite of type [" + entityType + "] in module [" +
               moduleName + "]." );
        this.entityType = entityType;
        this.moduleName = moduleName;
    }

    public String type()
    {
        return entityType;
    }

    public String moduleName()
    {
        return this.moduleName;
    }
}