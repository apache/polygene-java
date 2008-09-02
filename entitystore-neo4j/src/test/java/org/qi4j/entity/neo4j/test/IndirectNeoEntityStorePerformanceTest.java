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
package org.qi4j.entity.neo4j.test;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.entity.neo4j.Configuration;
import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilder;
import org.qi4j.query.QueryBuilderFactory;

import java.util.Date;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class IndirectNeoEntityStorePerformanceTest extends TestBase {

	protected static final String NAME1 = "Name1";
	Date before, after;

	public IndirectNeoEntityStorePerformanceTest() {
		this(Configuration.INDIRECT);
	}

	protected IndirectNeoEntityStorePerformanceTest(Configuration config) {
		super(config, config == Configuration.INDIRECT, true,
				MakeBelieveEntity.class);
	}

	@Test
	public void measure5000EntitiesInOneUoW() throws Exception {
		perform(new TestExecutor() {
			String identity;
			int number = 500;

			protected void setup() throws Exception {
				before = new Date();
				for (int i = 0; i < number; i++) {
					// Create entity
					MakeBelieveEntity believe = newEntity(MakeBelieveEntity.class);
					// identity = believe.identity().get();
					// Set up
					believe.imaginaryName().set("George Lucas");
					believe.imaginaryNumber().set(17);
					believe.realNumber().set(42.0);
				}
				printResult("Populating up UoW with " + number
						+ " entities in Qi4j't\t", before, new Date());
				before = new Date();
			}

			protected void verify() throws Exception {
				after = new Date();
				printResult("Neo4j persisting a UoW with " + number
						+ " entities without reading\t", before, after);
			}

			private void printResult(String name, Date before, Date after) {
				System.out.println(name + ": "
						+ (after.getTime() - before.getTime()) + "ms");
			}
		});
	}
	
	@Test
	public void measure5000EntitiesReading() throws Exception {
		final int number = 500;
		final String[] identities = new String[number];
		//first, insert
		perform(new TestExecutor() {
		
			protected void setup() throws Exception {
				before = new Date();
				for (int i = 0; i < number; i++) {
					// Create entity
					MakeBelieveEntity believe = newEntity(MakeBelieveEntity.class);
					identities[i] = believe.identity().get();
					// Set up
					believe.imaginaryName().set(NAME1);
					believe.imaginaryNumber().set(number);
					believe.realNumber().set(42.0);
				}
				printResult("Populating up UoW with " + number
						+ " entities in Qi4j't\t", before, new Date());
				before = new Date();
			}

			protected void verify() throws Exception {
				after = new Date();
				printResult("Neo4j persisting a UoW with " + number
						+ " entities without reading\t", before, after);
			}

		});
		perform(new TestExecutor() {

			@Override
			protected void setup() throws Exception {
				before = new Date();
				for(String identity : identities)
				{
					MakeBelieveEntity entity = getReference(identity, MakeBelieveEntity.class);
					Assert.assertNotNull(entity);
					//System.out.println(identity);
				}
				after = new Date();
				printResult("Reading in " + number + " entities:", before, after);
			}

			@Override
			protected void verify() throws Exception {
				// TODO Auto-generated method stub
				
			}
			});
	}

	private static void printResult(String name, Date before, Date after) {
	System.out.println(name
			+ (after.getTime() - before.getTime()) + "ms");
	}
}