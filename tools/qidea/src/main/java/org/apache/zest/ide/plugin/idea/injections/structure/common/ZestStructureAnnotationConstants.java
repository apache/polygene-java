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
*/

package org.apache.polygene.ide.plugin.idea.injections.structure.common;

import static java.util.Arrays.sort;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class PolygeneStructureAnnotationConstants
{
    public static final String QUALIFIED_NAME_STRUCTURE_ANNOTATION = "org.apache.polygene.api.injection.scope.Structure";

    public static final String[] VALID_STRUCTURE_INJECTION_TYPE;

    static
    {
        VALID_STRUCTURE_INJECTION_TYPE = new String[]
            {
                "org.apache.polygene.composite.CompositeBuilderFactory",
                "org.apache.polygene.object.ObjectBuilderFactory",
                "org.apache.polygene.entity.UnitOfWorkFactory",
                "org.apache.polygene.service.ServiceFinder",
                "org.apache.polygene.structure.Module",
                "org.apache.polygene.structure.Layer",
                "org.apache.polygene.structure.Application",
                "org.apache.polygene.PolygeneAPI",
                "org.apache.polygene.spi.PolygeneSPI"
            };
        sort( VALID_STRUCTURE_INJECTION_TYPE );
    }

    private PolygeneStructureAnnotationConstants()
    {
    }
}
