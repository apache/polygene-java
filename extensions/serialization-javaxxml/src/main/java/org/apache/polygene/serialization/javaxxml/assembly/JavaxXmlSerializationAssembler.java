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
package org.apache.polygene.serialization.javaxxml.assembly;

import org.apache.polygene.api.serialization.Deserializer;
import org.apache.polygene.api.serialization.Serialization;
import org.apache.polygene.api.serialization.Serializer;
import org.apache.polygene.bootstrap.Assemblers;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.ServiceDeclaration;
import org.apache.polygene.serialization.javaxxml.JavaxXmlSerialization;
import org.apache.polygene.serialization.javaxxml.JavaxXmlSettings;
import org.apache.polygene.spi.serialization.XmlDeserializer;
import org.apache.polygene.spi.serialization.XmlSerialization;
import org.apache.polygene.spi.serialization.XmlSerializer;

public class JavaxXmlSerializationAssembler extends Assemblers.VisibilityIdentity<JavaxXmlSerializationAssembler>
{
    private JavaxXmlSettings settings;

    public JavaxXmlSerializationAssembler withXmlSettings( JavaxXmlSettings settings )
    {
        this.settings = settings;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
    {
        ServiceDeclaration declaration = module.services( JavaxXmlSerialization.class )
                                               .withTypes( Serialization.class,
                                                           Serializer.class, Deserializer.class,
                                                           XmlSerialization.class,
                                                           XmlSerializer.class, XmlDeserializer.class )
                                               .taggedWith( Serialization.Format.XML )
                                               .visibleIn( visibility() );
        if( hasIdentity() )
        {
            declaration.identifiedBy( identity() );
        }
        if( settings != null )
        {
            declaration.setMetaInfo( settings );
        }
    }
}
