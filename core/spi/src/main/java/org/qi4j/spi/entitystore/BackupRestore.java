/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2010, Niclas Hedhman. All Rights Reserved.
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

package org.qi4j.spi.entitystore;

import java.io.IOException;
import org.qi4j.io.Input;
import org.qi4j.io.Output;

/**
 * Allow backups and restores of data in an EntityStore to be made
 */
public interface BackupRestore
{
    /**
     * Input that allows data from the entity store to be backed up.
     *
     * @return
     */
    Input<String, IOException> backup();

    /**
     * Output that allows data to be restored from a backup.
     */
    Output<String, IOException> restore();
}