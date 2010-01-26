/*
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.spi.query;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Interface for exporting the index currently held by the Indexing Engine.
 * Index Engine implementations are encouraged to implement this interface to allow for trouble-shooting index related
 * problems.
 */
public interface IndexExporter
{
    /**
     * Write the index to the provided output stream in an implementation specific, human-readable format.
     *
     * @param out The output stream that the index will be sent to.
     *
     * @throws java.io.IOException           if an IOException occurs in the underlying PrintStream.
     * @throws UnsupportedOperationException if the method is not supported by this implementation.
     */
    void exportReadableToStream( PrintStream out )
        throws IOException, UnsupportedOperationException;

    /**
     * Write the index to the provided print writer in an implementation specific, machine-readable format, preferably
     * either XML or JSON.
     *
     * @param out The print writer that the index will be sent to.
     *
     * @throws java.io.IOException           if an IOException occurs in the underlying PrintWriter.
     * @throws UnsupportedOperationException if the method is not supported by this implementation.
     */
    void exportFormalToWriter( PrintWriter out )
        throws IOException, UnsupportedOperationException;
}
