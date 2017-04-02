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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Initializable;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceDescriptor;

@Mixins( JavaxXmlFactories.Mixin.class )
public interface JavaxXmlFactories
{
    DocumentBuilderFactory documentBuilderFactory();

    TransformerFactory transformerFactory();

    class Mixin implements JavaxXmlFactories, Initializable
    {
        @Uses
        private ServiceDescriptor descriptor;

        private DocumentBuilderFactory documentBuilderFactory;
        private TransformerFactory transformerFactory;

        @Override
        public void initialize()
        {
            JavaxXmlSettings settings = JavaxXmlSettings.orDefault( descriptor.metaInfo( JavaxXmlSettings.class ) );

            String documentBuilderFactoryClassName = settings.getDocumentBuilderFactoryClassName();
            documentBuilderFactory = documentBuilderFactoryClassName == null
                                     ? DocumentBuilderFactory.newInstance()
                                     : DocumentBuilderFactory.newInstance( documentBuilderFactoryClassName,
                                                                           getClass().getClassLoader() );
            documentBuilderFactory.setNamespaceAware( false );
            documentBuilderFactory.setIgnoringComments( true );

            String transformerFactoryClassName = settings.getTransformerFactoryClassName();
            transformerFactory = transformerFactoryClassName == null
                                 ? TransformerFactory.newInstance()
                                 : TransformerFactory.newInstance( transformerFactoryClassName,
                                                                   getClass().getClassLoader() );
        }

        @Override
        public DocumentBuilderFactory documentBuilderFactory()
        {
            return documentBuilderFactory;
        }

        @Override
        public TransformerFactory transformerFactory()
        {
            return transformerFactory;
        }
    }
}
