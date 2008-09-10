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

import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.association.Qualifier;

/**
 * TODO
 */
public class QualifierQualifiedIdentity
    extends QualifiedIdentity
{
    QualifiedIdentity qualifier;

    public QualifierQualifiedIdentity( EntityComposite entityComposite, QualifiedIdentity qualifier )
    {
        super( entityComposite );
        this.qualifier = qualifier;
    }

    protected QualifierQualifiedIdentity( QualifiedIdentity qualifier, String qualifiedIdentity)
    {
        super( qualifiedIdentity );
        this.qualifier = qualifier;
    }

    public QualifierQualifiedIdentity( Qualifier associationQualifier )
    {
        super((EntityComposite) associationQualifier.entity());
        qualifier = QualifiedIdentity.getQualifiedIdentity( associationQualifier.qualifier());
    }

    public QualifiedIdentity role()
    {
        return qualifier;
    }

    @Override public int hashCode()
    {
        return super.hashCode()+role().hashCode();
    }

    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        if( !super.equals( o ) )
        {
            return false;
        }

        QualifierQualifiedIdentity that = (QualifierQualifiedIdentity) o;

        if( !qualifier.equals( that.qualifier ) )
        {
            return false;
        }

        return true;
    }

    @Override public String toString()
    {
        return super.toString()+"/"+ qualifier.toString();
    }
}
