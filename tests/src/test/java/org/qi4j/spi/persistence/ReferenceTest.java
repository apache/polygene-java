/*  Copyright 2007 Niclas Hedhman.
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
package org.qi4j.spi.persistence;

import junit.framework.TestCase;
import org.qi4j.api.CompositeBuilder;
import org.qi4j.api.CompositeFactory;
import org.qi4j.api.EntityRepository;
import org.qi4j.api.persistence.composite.PersistentStorage;
import org.qi4j.cache.CachedCompositeRepositoryComposite;
import org.qi4j.extension.persistence.quick.MapPersistenceProvider;
import org.qi4j.extension.persistence.quick.SerializablePersistence;
import org.qi4j.runtime.CompositeFactoryImpl;
import org.qi4j.runtime.EntityRepositoryImpl;
import org.qi4j.spi.serialization.SerializablePersistenceSpi;

public class ReferenceTest extends TestCase
{
    private CompositeFactory factory;
    private EntityRepository repository;
    private PersistentStorage storage;

    // TODO: Re-thinking
    public void test1()
        throws Exception
    {
//        TestComposite subject = factory.newInstance( TestComposite.class );
//        subject.setEntityRepository( storage );
//        subject.setIdentity( "1234" );
//        State1 state = new State1SerializableImpl();
//        subject.setState( state );
//        state.setState1( "niclas" );
//        subject.create();

//        TestComposite testComposite = repository.getInstance( "1234", TestComposite.class );
//        assertNotNull( testComposite );
//        State1 state1 = testComposite.getState();
//        assertNotNull( state1 );
//        assertEquals( State1SerializableImpl.class , state1.getClass() );
//        assertEquals( "niclas", state1.getState1() );
    }

    protected void setUp() throws Exception
    {

        // TODO: Re-Think the whole Persistence setup. Circular Deps at the moment.
        factory = new CompositeFactoryImpl();
        CompositeBuilder<CachedCompositeRepositoryComposite> builder = factory.newCompositeBuilder( CachedCompositeRepositoryComposite.class );
        builder.set( EntityRepository.class, new EntityRepositoryImpl( factory ) );
        CachedCompositeRepositoryComposite repo = builder.newInstance();
        SerializablePersistenceSpi subsystem = new MapPersistenceProvider();
        storage = new SerializablePersistence( subsystem, factory, repo );
        repository = repo;
    }
}
