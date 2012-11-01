/*
 * Copyright 2010 Niclas Hedhman.
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

package org.qi4j.entitystore.gae;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import java.util.Iterator;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;

@Mixins( GaeIdGeneratorService.Mixin.class )
public interface GaeIdGeneratorService
    extends IdentityGenerator, ServiceComposite
{
    public class Mixin
        implements IdentityGenerator
    {
        private DatastoreService datastore;
        private ThreadLocal<Iterator<Key>> range;

        public Mixin()
        {
            datastore = DatastoreServiceFactory.getDatastoreService();
            range = new ThreadLocal<Iterator<Key>>()
            {
                @Override
                protected Iterator<Key> initialValue()
                {
                    return datastore.allocateIds( "qi4j", 100 ).iterator();
                }
            };
        }

        @Override
        public String generate( Class<?> compositeType )
        {
            if( !range.get().hasNext() )
            {
                range.set( datastore.allocateIds( "qi4j", 100 ).iterator() );
            }
            return "" + range.get().next().getId();
        }
    }
}
