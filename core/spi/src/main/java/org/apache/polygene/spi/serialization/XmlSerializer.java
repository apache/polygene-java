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
 */
package org.apache.polygene.spi.serialization;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.serialization.SerializationException;
import org.apache.polygene.api.serialization.Serializer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * {@literal javax.xml} serializer.
 */
public interface XmlSerializer extends Serializer
{
    <T> Function<T, Document> toXmlFunction( Options options );

    default <T> Function<T, Document> toXmlFunction()
    {
        return object -> toXmlFunction( Options.DEFAULT ).apply( object );
    }

    default Document toXml( Options options, @Optional Object object )
    {
        return toXmlFunction( options ).apply( object );
    }

    default Document toXml( @Optional Object object )
    {
        return toXmlFunction( Options.DEFAULT ).apply( object );
    }

    default <T> Stream<Document> toXmlEach( Options options, Stream<T> objects )
    {
        return objects.map( toXmlFunction( options ) );
    }

    default <T> Stream<Document> toXmlEach( Options options, Iterable<T> objects )
    {
        return toXmlEach( options, StreamSupport.stream( objects.spliterator(), false ) );
    }

    default <T> Stream<Document> toXmlEach( Options options, Object... objects )
    {
        return toXmlEach( options, Stream.of( objects ) );
    }

    default <T> Stream<Document> toXmlEach( Stream<T> objects )
    {
        return objects.map( toXmlFunction( Options.DEFAULT ) );
    }

    default <T> Stream<Document> toXmlEach( Iterable<T> objects )
    {
        return toXmlEach( Options.DEFAULT, StreamSupport.stream( objects.spliterator(), false ) );
    }

    default <T> Stream<Document> toXmlEach( Object... objects )
    {
        return toXmlEach( Options.DEFAULT, Stream.of( objects ) );
    }

    default void serialize( Options options, Writer writer, @Optional Object object )
    {
        Document xmlDocument = toXml( options, object );
        if( xmlDocument == null )
        {
            return;
        }
        try
        {
            // We want plain text nodes to be serialized without surrounding elements
            if( xmlDocument.getNodeType() == Node.TEXT_NODE )
            {
                writer.write( xmlDocument.getNodeValue() );
            }
            else
            {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
                transformer.setOutputProperty( OutputKeys.VERSION, "1.1" );
                transformer.setOutputProperty( OutputKeys.STANDALONE, "yes" );
                transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
                transformer.transform( new DOMSource( xmlDocument ), new StreamResult( writer ) );
            }
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
        catch( TransformerException ex )
        {
            throw new SerializationException( "Unable to transform XML Document to String", ex );
        }
    }
}
