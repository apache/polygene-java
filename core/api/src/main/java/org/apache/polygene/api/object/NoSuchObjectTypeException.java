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
package org.apache.polygene.api.object;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.polygene.api.common.InvalidApplicationException;

/**
 * This exception is thrown if no visible Object of the requested type can be found.
 */
public class NoSuchObjectTypeException
    extends InvalidApplicationException
{
    private static final long serialVersionUID = -1121690536365682511L;
    private static final String NL = System.getProperty( "line.separator" );

    private final String objectType;
    private final String moduleName;

    public NoSuchObjectTypeException( String type, String moduleName, Stream<Class<?>> visible )
    {
        super( "Could not find any visible Object of type [" + type + "] in module ["
               + moduleName
               + "]. The visible types are: " + NL
               + visible.map( Class::getName ).collect( Collectors.joining( NL ) )
        );
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