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

package org.apache.zest.ide.plugin.idea.injections.structure.common;

import static java.util.Arrays.sort;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class ZestStructureAnnotationConstants
{
    public static final String QUALIFIED_NAME_STRUCTURE_ANNOTATION = "org.apache.zest.api.injection.scope.Structure";

    public static final String[] VALID_STRUCTURE_INJECTION_TYPE;

    static
    {
        VALID_STRUCTURE_INJECTION_TYPE = new String[]
            {
                "org.apache.zest.composite.CompositeBuilderFactory",
                "org.apache.zest.object.ObjectBuilderFactory",
                "org.apache.zest.entity.UnitOfWorkFactory",
                "org.apache.zest.service.ServiceFinder",
                "org.apache.zest.structure.Module",
                "org.apache.zest.structure.Layer",
                "org.apache.zest.structure.Application",
                "org.apache.zest.ZestAPI",
                "org.apache.zest.spi.ZestSPI"
            };
        sort( VALID_STRUCTURE_INJECTION_TYPE );
    }

    private ZestStructureAnnotationConstants()
    {
    }
}
