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
package org.qi4j.entitystore.neo4j.test;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.entitystore.neo4j.Configuration;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class DirectNeoEntityStoreTest
        extends TestBase
{
    public DirectNeoEntityStoreTest()
    {
        this(Configuration.DIRECT);
    }

    protected DirectNeoEntityStoreTest(Configuration config)
    {
        super(config, config == Configuration.INDIRECT, true, MakeBelieveEntity.class);
    }

    @Test
    public void testProperties() throws Exception
    {
        perform(new TestExecutor()
        {
            String identity;

            protected void setup() throws Exception
            {
                // Create entity
                EntityBuilder<MakeBelieveEntity> builder = newEntityBuilder(MakeBelieveEntity.class);
                MakeBelieveEntity believe = builder.prototype();
                // Set up
                believe.imaginaryName().set("George Lucas");
                believe.imaginaryNumber().set(17);
                believe.realNumber().set(42.0);
                believe = builder.newInstance();
                identity = believe.identity().get();
            }

            protected void verify() throws Exception
            {
                // Retreive entity
                MakeBelieveEntity believe = find(identity, MakeBelieveEntity.class);
                // Verify
                Assert.assertEquals("George Lucas", believe.imaginaryName().get());
                Assert.assertEquals((Object) 17, believe.imaginaryNumber().get());
                Assert.assertEquals((Object) 42.0, believe.realNumber().get());
            }
        });
    }

    @Test
    public void testAssociations() throws Exception
    {
        perform(new TestExecutor()
        {
            String one;
            String two;
            String three;

            protected void setup() throws Exception
            {
                // Create entities
                MakeBelieveEntity believeOne = newEntity(MakeBelieveEntity.class);
                one = believeOne.identity().get();
                MakeBelieveEntity believeTwo = newEntity(MakeBelieveEntity.class);
                two = believeTwo.identity().get();
                MakeBelieveEntity believeThree = newEntity(MakeBelieveEntity.class);
                three = believeThree.identity().get();
                // Set up
                believeOne.bff().set(believeTwo);
                believeOne.archNemesis().set(believeThree);
                believeTwo.bff().set(believeOne);
                believeTwo.archNemesis().set(believeOne);
                try
                {
                    believeThree.bff().set(null);
                }
                catch (IllegalArgumentException iae)
                {
                    System.out.println("Notice: Qi4j association does not support nulling.");
                }
                believeThree.archNemesis().set(believeOne);
            }

            protected void verify() throws Exception
            {
                // Retreive entities
                MakeBelieveEntity believeOne = find(one, MakeBelieveEntity.class);
                MakeBelieveEntity believeTwo = find(two, MakeBelieveEntity.class);
                MakeBelieveEntity believeThree = find(three, MakeBelieveEntity.class);
                // Verify
                Assert.assertEquals(believeTwo, believeOne.bff().get());
                Assert.assertEquals(believeThree, believeOne.archNemesis().get());
                Assert.assertEquals(believeOne, believeTwo.bff().get());
                Assert.assertEquals(believeOne, believeTwo.archNemesis().get());
                Assert.assertNull(believeThree.bff().get());
                Assert.assertEquals(believeOne, believeThree.archNemesis().get());
            }
        });
    }

    @Test
    public void testBuildEntity() throws Exception
    {
        perform(new TestExecutor()
        {
            String identity;
            String friend;

            protected void setup() throws Exception
            {
                // Create entity
                EntityBuilder<MakeBelieveEntity> builder = newEntityBuilder(MakeBelieveEntity.class);
                {
                    MakeBelieveEntity prototype = builder.prototype();
                    prototype.imaginaryName().set("Arne banan");
                    prototype.imaginaryNumber().set(17);
                    prototype.realNumber().set(42.0);
                }
                MakeBelieveEntity believe = builder.newInstance();
                identity = believe.identity().get();
                MakeBelieveEntity other = builder.newInstance();
                friend = other.identity().get();
                // Set up
                believe.bff().set(other);
            }

            protected void verify() throws Exception
            {
                // Retreive entities
                MakeBelieveEntity believe = find(identity, MakeBelieveEntity.class);
                MakeBelieveEntity other = find(friend, MakeBelieveEntity.class);
                // Verify
                Assert.assertEquals("Arne banan", believe.imaginaryName().get());
                Assert.assertEquals("Arne banan", other.imaginaryName().get());
                Assert.assertEquals((Object) 17, believe.imaginaryNumber().get());
                Assert.assertEquals((Object) 17, other.imaginaryNumber().get());
                Assert.assertEquals((Object) 42.0, believe.realNumber().get());
                Assert.assertEquals((Object) 42.0, other.realNumber().get());
                Assert.assertEquals(other, believe.bff().get());
                Assert.assertNull(other.bff().get());
                Assert.assertNull(believe.archNemesis().get());
                Assert.assertNull(other.archNemesis().get());
            }
        });
    }
}
