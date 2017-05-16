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

package org.apache.polygene.ide.plugin.idea.common.resource;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class PolygeneResourceBundle
{

    @NonNls
    private static final String RESOURCE_BUNDLE_NAME = "org.apache.polygene.ide.plugin.idea.common.resource.PolygeneResourceBundle";

    private static Reference<ResourceBundle> BUNDLE_REF;

    private PolygeneResourceBundle()
    {
    }

    public static String message( @PropertyKey( resourceBundle = RESOURCE_BUNDLE_NAME ) String key,
                                  Object... params )
    {
        ResourceBundle resourceBundle = getBundle();
        return CommonBundle.message( resourceBundle, key, params );
    }

    private static ResourceBundle getBundle()
    {
        ResourceBundle bundle = null;
        if( BUNDLE_REF != null )
        {
            bundle = BUNDLE_REF.get();
        }

        if( bundle == null )
        {
            bundle = ResourceBundle.getBundle( PolygeneResourceBundle.class.getName() );
            BUNDLE_REF = new SoftReference<ResourceBundle>( bundle );
        }

        return bundle;
    }
}
