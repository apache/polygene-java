/*
 * Copyright 2007 Niclas Hedhman.
 * Copyright 2008 Alin Dreghiciu.
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
package org.qi4j.runtime.query;

import org.qi4j.query.QueryBuilderFactory;
import org.qi4j.query.QueryBuilder;

/**
 * Default implementation of {@link QueryBuilderFactory}
 *
 * @author Alin Dreghiciu
 * @since March 25, 2008
 */
public final class QueryBuilderFactoryImpl
    implements QueryBuilderFactory
{

    /**
     * @see QueryBuilderFactory#newQueryBuilder(Class) 
     */
    public <T> QueryBuilder<T> newQueryBuilder( final Class<T> compositeType )
    {
        return new QueryBuilderImpl<T>( );
    }

}