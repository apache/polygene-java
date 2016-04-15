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
package org.apache.zest.api.value;

import java.util.stream.Collectors;
import org.apache.zest.api.composite.NoSuchCompositeException;
import org.apache.zest.api.structure.TypeLookup;

/**
 * Thrown when no visible value of the requested type is found.
 */
public class NoSuchValueException
    extends NoSuchCompositeException
{
    public NoSuchValueException( String valueType, String moduleName, TypeLookup typeLookup )
    {
        super( "ValueComposite", valueType, moduleName, formatVisibleTypes(typeLookup) );
    }

    private static String formatVisibleTypes( TypeLookup typeLookup )
    {
        return typeLookup.allValues()
            .map(descriptor -> descriptor.primaryType().getName())
            .collect( Collectors.joining( "\n", "Visible value types are:\n", "" ) );
    }
}