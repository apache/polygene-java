/* Copyright 2008 Neo Technology, http://neotechnology.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.neo4j.state;

import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public enum LinkType
{
    UNQUALIFIED
        {
            String getRelationshipTypeName( String qualifiedName )
            {
                return qualifiedName;
            }
        }, START, END, INTERNAL;
    private final String suffix;

    private LinkType()
    {
        suffix = "::" + name();
    }

    private String getRelationshipTypeName( String qualifiedName )
    {
        return qualifiedName + suffix;
    }

    public boolean isInstance( Relationship relation )
    {
        return relation.getType().name().endsWith( suffix );
    }

    public RelationshipType getRelationshipType( String qualifiedName )
    {
        return new AssociationRelationshipType( getRelationshipTypeName( qualifiedName ) );
    }

    private static class AssociationRelationshipType implements RelationshipType
    {
        private final String name;

        public AssociationRelationshipType( String name )
        {
            this.name = name;
        }

        public String name()
        {
            return name;
        }
    }
}
