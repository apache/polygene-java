/*
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.fileconfig;

import java.io.File;

public final class FileConfigurationOverride
{

    private File configuration;
    private File data;
    private File temporary;
    private File cache;
    private File log;

    public FileConfigurationOverride withConfiguration( File configuration )
    {
        this.configuration = configuration;
        return this;
    }

    public FileConfigurationOverride withData( File data )
    {
        this.data = data;
        return this;
    }

    public FileConfigurationOverride withTemporary( File temporary )
    {
        this.temporary = temporary;
        return this;
    }

    public FileConfigurationOverride withCache( File cache )
    {
        this.cache = cache;
        return this;
    }

    public FileConfigurationOverride withLog( File log )
    {
        this.log = log;
        return this;
    }

    public File cache()
    {
        return cache;
    }

    public File configuration()
    {
        return configuration;
    }

    public File data()
    {
        return data;
    }

    public File log()
    {
        return log;
    }

    public File temporary()
    {
        return temporary;
    }

}
