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

package org.qi4j.api.usecase;

import org.qi4j.api.common.MetaInfo;

/**
 * Builder for Usecases.
 */
public final class UsecaseBuilder
{
    public static UsecaseBuilder buildUsecase( String aName )
    {
        return new UsecaseBuilder( aName );
    }

    public static Usecase newUsecase( String aName )
    {
        return new UsecaseBuilder( aName ).newUsecase();
    }

    private MetaInfo metaInfo = new MetaInfo();

    private String name;

    private UsecaseBuilder( String name )
    {
        this.name = name;
    }

    public UsecaseBuilder withMetaInfo( Object metaInfo )
    {
        this.metaInfo.set( metaInfo );
        return this;
    }

    public Usecase newUsecase()
    {
        return new Usecase( name, metaInfo );
    }
}
