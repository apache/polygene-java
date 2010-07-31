/*
 * Copyright (c) 2009, Rickard …berg. All Rights Reserved.
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
import java.io.Reader;

/**
 * Imports data from an external source into an EntityStore, using the JSON format.
 */
public interface ImportSupport
{
    /**
     * Import data from the Reader, with one line per object, in JSON format.
     *
     * @param in reader
     *
     * @throws java.io.IOException if there is an underlying I/O problem.
     *
     * @return The number of entities that has been
     */
    ImportResult importFrom( Reader in )
        throws IOException;

    
    public interface ImportResult
    {
        /** Returns the number of successfully imported entities.
         *
         * @return the number of successfully imported entities.
         */
        long numberOfSuccessfulImports();

        /** Returns an array of problems in human readable form.
         *
         * @return An array of descriptive messages, one for each failing line in the import, or an empty array
         * if no problems were found.
         */
        String[] failureReports();
    }
}