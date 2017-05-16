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
package org.apache.polygene.entitystore.file;

import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.library.constraints.annotation.Range;

/**
 * Configuration for the FileEntityStoreService
 */
// START SNIPPET: config
public interface FileEntityStoreConfiguration
{
    // END SNIPPET: config
    /**
     * The directory where the File Entity Store will be keep its persisted state.
     * <p>
     *     If no configuration is provided at all, then the default location is
     *     {@code System.getProperty( "user.dir" ) + "/polygene/filestore"; }.
     *     If a configuration is given, the entity store will be placed in the
     *     DATA directory, which is operating system specific.
     * </p>
     * <table summary="Default locations">
     *     <tr><th>OS</th><th>Location</th></tr>
     *     <tr><td>Linux/Unix</td><td>{user}/.{application}/data</td></tr>
     *     <tr><td>OSX</td><td>{user}/Library/Application Support/{application}</td></tr>
     *     <tr><td>Windows</td><td>{user}/Application Data/{application}/data</td></tr>
     * </table>
     * <pre><code>
     * where;
     *   {user} = Current User's home directory
     *   {application} = Application's name, as set in assembly.
     * </code></pre>
     * <p>
     * Ignored if the FileConfiguration service is found.
     * </p>
     * <p>
     * The content inside this directory should not be modified directly, and doing so may corrupt the data.
     * </p>
     *
     * @return path to data file relative to current path
     */
    // START SNIPPET: config
    @Optional
    Property<String> directory();
    // END SNIPPET: config

    /** Defines how many slice directories the store should use.
     * <p>
     * Many operating systems run into performance problems when the number of files in a directory grows. If
     * you expect a large number of entities in the file entity store, it is wise to set the number of slices
     * (default is 1) to an approximation of the square root of number of expected entities.
     * </p>
     * <p>
     * For instance, if you estimate that you will have 1 million entities in the file entity store, you should
     * set the slices to 1000.
     * </p>
     * <p>
     * There is an limit of minimum 1 slice and maximum 10,000 slices, and if more slices than that is needed, you
     * are probably pushing this entitystore beyond its capabilities.
     * </p>
     * <p>
     * Note that the slices() can not be changed once it has been set, as it would cause the entity store not to
     * find the entities anymore.
     * </p>
     * @return the number of slices for the file entity store.
     */
    // START SNIPPET: config
    @Optional @Range(min=1, max=10000)
    Property<Integer> slices();
}
// END SNIPPET: config
