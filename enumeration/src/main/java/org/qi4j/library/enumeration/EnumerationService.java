/*
 * Copyright 2009 Niclas Hedhman.
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
package org.qi4j.library.enumeration;

import java.util.HashMap;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/** The EnumerationService is a generic collection storage system, where client can store Iterable groups of data
 * of a particular type.
 *
 * NOTE: Work in progress.
 */
@Mixins( EnumerationService.EnumerationMixin.class )
public interface EnumerationService extends ServiceComposite
{
    <T> Enumerator<T> enumerator( Class<T> type )
        throws EnumeratorNotDefinedException;

    Iterable<Enumerator> findAll();

    static abstract class EnumerationMixin
        implements EnumerationService
    {
        private HashMap<Class, Enumerator> mapped;

        EnumerationMixin( @Structure UnitOfWorkFactory uowf )
            throws UnitOfWorkCompletionException
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                QueryBuilder<Enumerator> builder = uow.queryBuilderFactory().newQueryBuilder( Enumerator.class );
                Query<Enumerator> query = builder.newQuery();
                mapped = new HashMap<Class, Enumerator>();
                for( Enumerator enumerator : query )
                {
                    Class type = enumerator.type();
                    mapped.put( type, enumerator );
                }
                uow.complete();
            }
            finally
            {
                if( uow.isOpen() )
                {
                    uow.discard();
                }
            }
        }

        public <T> Enumerator<T> enumerator( Class<T> type )
            throws EnumeratorNotDefinedException
        {
            return mapped.get( type );
        }

        public Iterable<Enumerator> findAll()
        {
            return null;
        }
    }
}
