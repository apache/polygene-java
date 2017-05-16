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
package org.apache.polygene.serialization.javaxxml;

import java.io.InputStream;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Initializable;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.serialization.SerializationException;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.w3c.dom.Document;

import static java.nio.charset.StandardCharsets.UTF_8;

@Mixins( JavaxXmlFactories.Mixin.class )
public interface JavaxXmlFactories
{
    DocumentBuilderFactory documentBuilderFactory();

    Document newDocumentForSerialization();

    TransformerFactory transformerFactory();

    Transformer serializationTransformer();

    Transformer normalizationTransformer();

    class Mixin implements JavaxXmlFactories, Initializable
    {
        @Uses
        private ServiceDescriptor descriptor;

        private DocumentBuilderFactory documentBuilderFactory;
        private TransformerFactory transformerFactory;

        private Transformer serializationTransformer;
        private Transformer normalizationTransformer;

        @Override
        public void initialize()
        {
            JavaxXmlSettings settings = JavaxXmlSettings.orDefault( descriptor.metaInfo( JavaxXmlSettings.class ) );

            try
            {
                String documentBuilderFactoryClassName = settings.getDocumentBuilderFactoryClassName();
                documentBuilderFactory = documentBuilderFactoryClassName == null
                                         ? DocumentBuilderFactory.newInstance()
                                         : DocumentBuilderFactory.newInstance( documentBuilderFactoryClassName,
                                                                               getClass().getClassLoader() );
                documentBuilderFactory.setValidating( false );
                documentBuilderFactory.setNamespaceAware( false );
                documentBuilderFactory.setIgnoringComments( true );
                for( Map.Entry<String, Boolean> feature : settings.getDocumentBuilderFactoryFeatures().entrySet() )
                {
                    documentBuilderFactory.setFeature( feature.getKey(), feature.getValue() );
                }
                for( Map.Entry<String, Object> attributes : settings.getDocumentBuilderFactoryAttributes().entrySet() )
                {
                    documentBuilderFactory.setAttribute( attributes.getKey(), attributes.getValue() );
                }

                String transformerFactoryClassName = settings.getTransformerFactoryClassName();
                transformerFactory = transformerFactoryClassName == null
                                     ? TransformerFactory.newInstance()
                                     : TransformerFactory.newInstance( transformerFactoryClassName,
                                                                       getClass().getClassLoader() );
                for( Map.Entry<String, Boolean> feature : settings.getTransformerFactoryFeatures().entrySet() )
                {
                    transformerFactory.setFeature( feature.getKey(), feature.getValue() );
                }
                for( Map.Entry<String, Object> attributes : settings.getTransformerFactoryAttributes().entrySet() )
                {
                    transformerFactory.setAttribute( attributes.getKey(), attributes.getValue() );
                }

                serializationTransformer = transformerFactory.newTransformer();
                serializationTransformer.setOutputProperty( OutputKeys.METHOD, "xml" );
                serializationTransformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
                serializationTransformer.setOutputProperty( OutputKeys.VERSION, "1.1" );
                serializationTransformer.setOutputProperty( OutputKeys.STANDALONE, "yes" );
                serializationTransformer.setOutputProperty( OutputKeys.ENCODING, UTF_8.name() );
                serializationTransformer.setOutputProperty( OutputKeys.INDENT, "no" );

                String xslPath = "/org/apache/polygene/serialization/javaxxml/deserializer-normalization.xsl";
                InputStream xsltStream = getClass().getResourceAsStream( xslPath );
                normalizationTransformer = transformerFactory.newTransformer( new StreamSource( xsltStream ) );
                normalizationTransformer.setOutputProperty( OutputKeys.METHOD, "xml" );
                normalizationTransformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
                normalizationTransformer.setOutputProperty( OutputKeys.VERSION, "1.1" );
                normalizationTransformer.setOutputProperty( OutputKeys.STANDALONE, "yes" );
                normalizationTransformer.setOutputProperty( OutputKeys.ENCODING, UTF_8.name() );
                normalizationTransformer.setOutputProperty( OutputKeys.INDENT, "no" );
            }
            catch( ParserConfigurationException | TransformerConfigurationException ex )
            {
                throw new SerializationException( "Unable to setup the XML subsystem", ex );
            }
        }

        @Override
        public DocumentBuilderFactory documentBuilderFactory()
        {
            return documentBuilderFactory;
        }

        @Override
        public Document newDocumentForSerialization()
        {
            try
            {
                DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
                Document doc = docBuilder.newDocument();
                doc.setXmlVersion( "1.1" );
                doc.setXmlStandalone( true );
                return doc;
            }
            catch( ParserConfigurationException ex )
            {
                throw new SerializationException( "Unable to create XML document. "
                                                  + "Is your javax.xml subsystem correctly set up?", ex );
            }
        }

        @Override
        public TransformerFactory transformerFactory()
        {
            return transformerFactory;
        }

        @Override
        public Transformer serializationTransformer()
        {
            return serializationTransformer;
        }

        @Override
        public Transformer normalizationTransformer()
        {
            return normalizationTransformer;
        }
    }
}
