/**
 *
 * Copyright 2009-2010 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.fileconfig;

import java.io.File;

/**
 * Service for accessing application-specific directories.
 * 
 * <p>
 * These will default to the platform settings, but can be overridden manually, either one-by-one or as a whole.
 * </p>
 * <p>
 * You can override defaults by adding org.qi4j.library.fileconfig.FileConfiguration_OS.properties files to your
 * classpath where OS is one of win, mac or unix.
 * <br/>
 * You can also override all properties definitions at assembly time by setting a FileConfigurationOverride object
 * as meta info of this service.
 * </p>
 * <p>
 * Services will most likely want to create their own subdirectories in the directories accessed
 * from here.
 * </p>
 */
// START SNIPPET: fileconfig
public interface FileConfiguration
{
// END SNIPPET: fileconfig

    public enum OS
    {
        windows, unix, mac
    }

    OS os();

    File user();

    // START SNIPPET: fileconfig
    File configurationDirectory();

    File dataDirectory();

    File temporaryDirectory();

    File cacheDirectory();

    File logDirectory();

}
// END SNIPPET: fileconfig

