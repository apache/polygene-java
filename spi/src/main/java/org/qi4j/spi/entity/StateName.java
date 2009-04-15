/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

import org.qi4j.api.common.QualifiedName;

import java.io.Serializable;

/**
 * A state name. State can be either a property or an association..
 * This includes three versions of the name:
 * 1) The {@link QualifiedName}
 * 2) The RDF name
 * 3) The hashed version of the name
 *
 * The QualifiedName is the most shortlived name. It changes whenever the package
 * or name of the state changes. The RDF name is probably more longlived as it is
 * declared in an ontology that is not strongly coupled to the implementation detail
 * of the property. The hashed version of the name assures that we can look at a property/association
 * in an entity store and see where it came from originally.
 */
public class StateName
    implements Serializable
{
    static final long serialVersionUID = 42L;

    private QualifiedName name;
    private String rdf;
    private String version;

    public StateName(String stateName)
    {
        String[] parts = stateName.split("/", 3);
        version = parts[0];
        name = QualifiedName.fromQN(parts[1]);
        rdf = parts[2];
    }

    public StateName(QualifiedName name, String rdf, String version)
    {
        this.name = name;
        this.rdf = rdf;
        this.version = version;
    }

    public QualifiedName qualifiedName()
    {
        return name;
    }

    public String rdf()
    {
        return rdf;
    }

    public String version()
    {
        return version;
    }

    @Override
    public String toString()
    {
        return version+"/"+name.toString()+"/"+rdf;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StateName stateName = (StateName) o;

        if (!version.equals(stateName.version)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return version.hashCode();
    }
}
