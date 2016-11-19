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
package org.apache.zest.library.fileconfig;

import java.io.File;

/**
 * Assembly time FileConfiguration override.
 */
public final class FileConfigurationOverride
{
    /**
     * {@literal configuration}.
     */
    public static final String CONVENTIONAL_CONFIGURATION = "configuration";
    /**
     * {@literal data}.
     */
    public static final String CONVENTIONAL_DATA = "data";
    /**
     * {@literal temporary}.
     */
    public static final String CONVENTIONAL_TEMPORARY = "temporary";
    /**
     * {@literal cache}.
     */
    public static final String CONVENTIONAL_CACHE = "cache";
    /**
     * {@literal log}.
     */
    public static final String CONVENTIONAL_LOG = "log";

    private File configuration;
    private File data;
    private File temporary;
    private File cache;
    private File log;

    /**
     * With all directories under the given root using conventional names.
     * @param root Root file
     * @return This
     */
    public FileConfigurationOverride withConventionalRoot( File root )
    {
        this.configuration = new File( root, CONVENTIONAL_CONFIGURATION );
        this.data = new File( root, CONVENTIONAL_DATA );
        this.temporary = new File( root, CONVENTIONAL_TEMPORARY );
        this.cache = new File( root, CONVENTIONAL_CACHE );
        this.log = new File( root, CONVENTIONAL_LOG );
        return this;
    }

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
