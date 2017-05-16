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

package org.apache.polygene.library.rdf.model;

import java.io.PrintWriter;
import org.openrdf.model.Graph;
import org.openrdf.model.impl.GraphImpl;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.library.rdf.serializer.RdfXmlSerializer;
import org.apache.polygene.library.rdf.serializer.SerializerContext;

public class ApplicationSerializer
{
    public Graph serialize( Application app )
    {
        Graph graph = new GraphImpl();
        SerializerContext context = new SerializerContext( graph );
        ApplicationVisitor applicationVisitor = new ApplicationVisitor( context );
        app.descriptor().accept( applicationVisitor );
        return graph;
    }

    public void outputMetadata( Graph rdf, PrintWriter writer )
        throws Exception
    {
        new RdfXmlSerializer().serialize( rdf, writer );
    }
}
