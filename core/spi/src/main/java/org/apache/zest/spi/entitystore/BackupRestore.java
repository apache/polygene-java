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

package org.apache.zest.spi.entitystore;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Allow backups and restores of data in an EntityStore to be made
 */
public interface BackupRestore
{
    /**
     * Backup as a stream of serialized entity states, must be closed.
     */
    Stream<String> backup();

    /**
     * Restore from a stream of serialized entity states.
     */
    void restore( Stream<String> states );

    /**
     * Restore from streams of serialized entity states.
     *
     * @return A consumer of streams of serialized entity states
     */
    default Consumer<Stream<String>> restore()
    {
        return this::restore;
    }
}