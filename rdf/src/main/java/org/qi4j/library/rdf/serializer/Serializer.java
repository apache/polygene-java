/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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
package org.qi4j.library.rdf.serializer;

import java.io.Writer;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;

public interface Serializer
{
    void serialize( Iterable<Statement> graph, Writer out )
        throws RDFHandlerException;

    void serialize( Iterable<Statement> graph, Writer out, String[] namespacePrefixes, String[] namespaces )
        throws RDFHandlerException;

}
