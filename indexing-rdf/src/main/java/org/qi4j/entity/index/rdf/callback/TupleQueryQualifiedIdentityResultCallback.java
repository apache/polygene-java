/*
 * Copyright 2008 Michael Hunger.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entity.index.rdf.callback;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.qi4j.spi.entity.QualifiedIdentity;

public class TupleQueryQualifiedIdentityResultCallback implements TupleQueryResultCallback
{
    QualifiedIdentityResultCallback qualifiedIdentityResultCallback;

    public TupleQueryQualifiedIdentityResultCallback( QualifiedIdentityResultCallback qualifiedIdentityResultCallback )
    {
        this.qualifiedIdentityResultCallback = qualifiedIdentityResultCallback;
    }

    public boolean processRow( int row, BindingSet bindingSet )
    {
        if( qualifiedIdentityResultCallback == null )
        {
            return true;
        }
        final Value identifier = bindingSet.getValue( "identity" );
        //TODO Shall we throw an exception if there is no binding for identifier = query parser is not right
        if( identifier != null )
        {
            final Value entityClass = bindingSet.getValue( "entityType" );
            // todo remove
            System.out.println( entityClass.stringValue() + " -> " + identifier.stringValue() );
            final QualifiedIdentity qualifiedIdentity = new QualifiedIdentity( identifier.stringValue(), entityClass.stringValue() );
            return qualifiedIdentityResultCallback.processRow( row, qualifiedIdentity );
        }
        return true;
    }

}
