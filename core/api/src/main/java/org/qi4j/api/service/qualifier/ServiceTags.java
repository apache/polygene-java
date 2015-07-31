/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api.service.qualifier;

import java.io.Serializable;

/**
 * Use this as metainfo about a Service to specify tags. Easiest way to set them on a service
 * is to use the <code>ServiceDeclaration.taggedWith(String...)</code> method.
 *
 * These can be used in conjunction with the withTags() Service
 * Selector.
 */
public final class ServiceTags
    implements Serializable
{
    private String[] tags;

    public ServiceTags( String... tags )
    {
        this.tags = tags;
    }

    public String[] tags()
    {
        return tags;
    }

    public boolean hasTag( String tag )
    {
        for( String serviceTag : tags )
        {
            if( serviceTag.equals( tag ) )
            {
                return true;
            }
        }

        return false;
    }

    public boolean hasTags( String... aTags )
    {
        for( String tag : aTags )
        {
            if( !hasTag( tag ) )
            {
                return false;
            }
        }

        return true;
    }
}
