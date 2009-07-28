package org.qi4j.index.rdf.internal;

import org.openrdf.query.QueryLanguage;
import org.qi4j.index.rdf.callback.QualifiedIdentityResultCallback;
import org.qi4j.spi.query.EntityFinderException;

public interface TupleQueryExecutor
{
    long performTupleQuery( QueryLanguage language, String query, QualifiedIdentityResultCallback callback )
        throws EntityFinderException;
}
