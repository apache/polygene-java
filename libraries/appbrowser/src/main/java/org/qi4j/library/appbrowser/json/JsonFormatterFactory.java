/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.library.appbrowser.json;

import java.io.Writer;
import org.json.JSONWriter;
import org.qi4j.library.appbrowser.Formatter;
import org.qi4j.library.appbrowser.FormatterFactory;

public class JsonFormatterFactory
    implements FormatterFactory
{
    private static final NullFormatter NULL_FORMATTER = new NullFormatter();
    private final JSONWriter writer;

    public JsonFormatterFactory(Writer destination)
    {
        writer = new JSONWriter( destination );
    }

    @Override
    public Formatter create( String componentType )
    {
        if( componentType.equalsIgnoreCase( "ApplicationModel" ))
            return new ApplicationModelFormatter( writer );
        if( componentType.equalsIgnoreCase( "LayerModel" ))
            return new LayerModelFormatter( writer );
        if( componentType.equalsIgnoreCase( "ModuleModel" ))
            return new ModuleModelFormatter( writer );
        if( componentType.equalsIgnoreCase( "TransientsModel" ))
            return new ArrayFormatter(writer, "transients");
        if( componentType.equalsIgnoreCase( "EntitiesModel" ))
            return new ArrayFormatter(writer, "entities");
        if( componentType.equalsIgnoreCase( "ServicesModel" ))
            return new ArrayFormatter(writer, "services");
        if( componentType.equalsIgnoreCase( "ServiceModel" ))
            return new ServiceModelFormatter
                (writer);
        if( componentType.equalsIgnoreCase( "ValuesModel" ))
            return new ArrayFormatter(writer, "values");
        if( componentType.equalsIgnoreCase( "ValueModel" ))
            return new ValueModelFormatter(writer);
        if( componentType.equalsIgnoreCase( "ValueStateModel" ))
            return NULL_FORMATTER;
        if( componentType.equalsIgnoreCase( "EntityModel" ))
            return new EntityModelFormatter( writer );
        if( componentType.equalsIgnoreCase( "CompositeMethodsModel" ))
            return new ArrayFormatter( writer, "methods" );
        if( componentType.equalsIgnoreCase( "CompositeMethodModel" ))
            return new CompositeMethodModelFormatter(writer);
        if( componentType.equalsIgnoreCase( "ObjectsModel" ))
            return new ArrayFormatter( writer, "objects" );
        if( componentType.equalsIgnoreCase( "ConstraintsModel" ))
            return new ArrayFormatter( writer, "constraints" );
        if( componentType.equalsIgnoreCase( "SideEffectsModel" ))
            return new ArrayFormatter( writer, "sideeffects" );
        if( componentType.equalsIgnoreCase( "ConcernsModel" ))
            return new ArrayFormatter( writer, "concerns" );
        if( componentType.equalsIgnoreCase( "PropertiesModel" ))
            return new ArrayFormatter( writer, "properties" );
        if( componentType.equalsIgnoreCase( "ConstructorsModel" ))
            return new ArrayFormatter( writer, "constructors" );
        if( componentType.equalsIgnoreCase( "ConstructorModel" ))
            return new ConstructorModelFormatter( writer );
        if( componentType.equalsIgnoreCase( "EntityMixinsModel" ))
            return new ArrayFormatter( writer, "mixins" );
        if( componentType.equalsIgnoreCase( "MixinsModel" ))
            return new ArrayFormatter( writer, "mixins" );
        if( componentType.equalsIgnoreCase( "MixinModel" ))
            return new MixinModelFormatter( writer );
        if( componentType.equalsIgnoreCase( "AssociationsModel" ))
            return new ArrayFormatter( writer, "associations" );
        if( componentType.equalsIgnoreCase( "ManyAssociationsModel" ))
            return new ArrayFormatter( writer, "manyassociations" );
        if( componentType.equalsIgnoreCase( "InjectedFieldsModel" ))
            return new ArrayFormatter( writer, "injectedfields" );
        if( componentType.equalsIgnoreCase( "InjectedFieldModel" ))
            return new InjectedFieldModelFormatter(writer);
        if( componentType.equalsIgnoreCase( "InjectedMethodsModel" ))
            return new ArrayFormatter( writer, "injectedmethods" );
        if( componentType.equalsIgnoreCase( "InjectedParametersModel" ))
            return new ArrayFormatter( writer, "injectedparameters" );
        if( componentType.equalsIgnoreCase( "EntityStateModel" ))
            return NULL_FORMATTER;
        if( componentType.equalsIgnoreCase( "ObjectModel" ))
            return new ObjectModelFormatter(writer);
        if( componentType.equalsIgnoreCase( "ImportedServicesModel" ))
            return NULL_FORMATTER;
        return null;
    }
}
