/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.library.framework.rdf.parse;

import org.openrdf.model.Graph;
import org.openrdf.model.impl.GraphImpl;
import org.qi4j.library.framework.rdf.Parser;
import org.qi4j.library.framework.rdf.parse.model.ApplicationParser;
import org.qi4j.spi.structure.ApplicationBinding;
import org.qi4j.spi.structure.ApplicationModel;
import org.qi4j.spi.structure.ApplicationResolution;

public final class StructureParser
    implements Parser
{
    public Graph parse( ApplicationBinding binding, String applicationURI )
    {
        Graph graph = new GraphImpl();
        ParserFactoryImpl factory = new ParserFactoryImpl();
        ParseContext context = new ParseContext( graph, factory, applicationURI );
        factory.setParseContext( context );
        ApplicationParser applicationParser = new ApplicationParser( context );
        ApplicationResolution resolution = binding.getApplicationResolution();
        ApplicationModel model = resolution.getApplicationModel();
        applicationParser.parseModel( model );
        return graph;
    }
}
