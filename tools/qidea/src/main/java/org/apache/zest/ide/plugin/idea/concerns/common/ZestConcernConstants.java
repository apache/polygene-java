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

package org.apache.polygene.ide.plugin.idea.concerns.common;

import org.jetbrains.annotations.NonNls;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class PolygeneConcernConstants
{
    public static final String QUALIFIED_NAME_CONCERNS = "org.apache.polygene.api.concern.Concerns";

    public static final String QUALIFIED_NAME_CONCERN_OF = "org.apache.polygene.api.concern.ConcernOf";
    public static final String QUALIFIED_NAME_GENERIC_CONCERN = "org.apache.polygene.api.concern.GenericConcern";

    @NonNls
    public static final String TEMPLATE_GENERIC_CONCERN_OF = "GenericConcernOf.java";

    private PolygeneConcernConstants()
    {
    }
}
