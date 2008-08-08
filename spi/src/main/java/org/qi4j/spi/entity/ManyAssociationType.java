/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.entity;

import java.io.Serializable;

/**
 * TODO
 */
public class ManyAssociationType
    implements Serializable
{
    public enum ManyAssociationTypeEnum
    {
        MANY, LIST, SET
    }

    public ManyAssociationType( String qualifiedName, ManyAssociationTypeEnum associationType, String type )
    {
        this.qualifiedName = qualifiedName;
        this.associationType = associationType;
        this.type = type.toLowerCase();
    }

    private String qualifiedName;
    private ManyAssociationTypeEnum associationType;
    private String type;

    public String qualifiedName()
    {
        return qualifiedName;
    }

    public ManyAssociationTypeEnum associationType()
    {
        return associationType;
    }

    public String type()
    {
        return type;
    }

    public String toURI()
    {
        return "urn:qi4j:association:" + qualifiedName;

    }

    @Override public String toString()
    {
        return qualifiedName + "(" + type + ")";
    }

}
