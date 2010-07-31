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
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Exports the entities in an EntityStore, using the JSON format.
 * <p>
 * The output should be in JSON format, each entity independent of the others and separated by a newline.
 */
public interface ExportSupport
{
    /**
     * Export data to the writer, with one line per object, in JSON format.
     *
     * @param out The print writer to where the output will be written to.
     */
    void exportTo( PrintWriter out )
        throws IOException;
}
