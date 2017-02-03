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

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.polygene.api.type.ValueType;

/**
 * javax.xml settings.
 *
 * Must be registered as meta-info at assembly time.
 */
// TODO javax.xml properties
public class JavaxXmlSettings
{
    public static final JavaxXmlSettings DEFAULT = new JavaxXmlSettings();

    public static JavaxXmlSettings orDefault( JavaxXmlSettings settings )
    {
        return settings != null ? settings : DEFAULT;
    }

    private String rootTagName;
    private String collectionTagName;
    private String collectionElementTagName;
    private String mapTagName;
    private String mapEntryTagName;
    private String valueTagName;
    private String typeInfoTagName;
    private Map<ValueType, JavaxXmlAdapter<?>> adapters;

    public JavaxXmlSettings()
    {
        rootTagName = "state";
        collectionTagName = "collection";
        collectionElementTagName = "element";
        mapTagName = "map";
        mapEntryTagName = "entry";
        valueTagName = "value";
        typeInfoTagName = "_type";
        adapters = new LinkedHashMap<>();
    }

    public String getRootTagName()
    {
        return rootTagName;
    }

    public void setRootTagName( final String rootTagName )
    {
        this.rootTagName = rootTagName;
    }

    public String getCollectionTagName()
    {
        return collectionTagName;
    }

    public void setCollectionTagName( final String collectionTagName )
    {
        this.collectionTagName = collectionTagName;
    }

    public String getCollectionElementTagName()
    {
        return collectionElementTagName;
    }

    public void setCollectionElementTagName( final String collectionElementTagName )
    {
        this.collectionElementTagName = collectionElementTagName;
    }

    public String getMapTagName()
    {
        return mapTagName;
    }

    public void setMapTagName( final String mapTagName )
    {
        this.mapTagName = mapTagName;
    }

    public String getMapEntryTagName()
    {
        return mapEntryTagName;
    }

    public void setMapEntryTagName( final String mapEntryTagName )
    {
        this.mapEntryTagName = mapEntryTagName;
    }

    public String getValueTagName()
    {
        return valueTagName;
    }

    public void setValueTagName( final String valueTagName )
    {
        this.valueTagName = valueTagName;
    }

    public String getTypeInfoTagName()
    {
        return typeInfoTagName;
    }

    public void setTypeInfoTagName( final String typeInfoTagName )
    {
        this.typeInfoTagName = typeInfoTagName;
    }

    public Map<ValueType, JavaxXmlAdapter<?>> getAdapters()
    {
        return adapters;
    }
}
