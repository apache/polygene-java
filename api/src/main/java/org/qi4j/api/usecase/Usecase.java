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

import java.io.Serializable;
import org.qi4j.api.common.MetaInfo;

/**
 * A Usecase. A Usecase is used as a model for UnitOfWork, and helps
 * implementations decide what to do in certain circumstances.
 */
public final class Usecase
    implements Serializable
{
    public static final Usecase DEFAULT = new Usecase( "Default", new MetaInfo() );

    private String name;
    private MetaInfo metaInfo = new MetaInfo();

    Usecase( String name, MetaInfo metaInfo )
    {
        this.name = name;
        this.metaInfo = metaInfo;
    }

    /**
     * Name of the usecase.
     *
     * @return the name
     */
    public String name()
    {
        return name;
    }

    /**
     * Meta-info for the usecase. This can be of any type, and is typically set when creating the usecase
     * and read during the execution of the usecase.
     *
     * @return
     */
    public MetaInfo metaInfo()
    {
        return metaInfo;
    }

    @Override public String toString()
    {
        return name + ", meta info:" + metaInfo;
    }
}
