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

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.entity.neo4j.Configuration;

/**
 * @author Peter Neubauer (peter@neubauer.se)
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
	public void measureUnassociatedEntitiesWritingBeforeReading() throws Exception {
		final int number = 5000;
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
				printResult("Populating UoW with " + number
						+ " entities in Qi4j't\t", before, new Date());
				before = new Date();
			}

			protected void verify() throws Exception {
				after = new Date();
				printResult("Neo4j persisting a UoW with " + number
						+ " entities\t", before, after);
				before = new Date();
				for(String identity : identities)
				{
					MakeBelieveEntity entity = getReference(identity, MakeBelieveEntity.class);
					Assert.assertNotNull(entity);
					entity.imaginaryName().get();
					entity.imaginaryNumber().get();
					entity.realNumber().get();
					//System.out.println(identity);
				}
				after = new Date();
				printResult("Reading in " + number + " entities from Neo4j:\t\t", before, after);
			}

		});
		
	}
	
	@Test
	public void measureAssociatedEntitiesWritingBeforeReading() throws Exception {
		final int number = 5000;
		final String[] identities = new String[number];
		//first, insert
		perform(new TestExecutor() {
		
			protected void setup() throws Exception {
				before = new Date();
				MakeBelieveEntity believe = newEntity(MakeBelieveEntity.class);
				int i = 0;
				identities[i++ ] = believe.identity().get();
				for (;i < number; i++) {
					// Create entity
					believe = newEntity(MakeBelieveEntity.class);
					identities[i] = believe.identity().get();
					// Set up
					believe.imaginaryName().set(NAME1);
					believe.imaginaryNumber().set(number);
					believe.realNumber().set(42.0);
					believe.archNemesis().set(getReference(identities[i-1], MakeBelieveEntity.class));
				}
				printResult("Populating UoW with " + number
						+ " entities in Qi4j't\t", before, new Date());
				before = new Date();
			}

			protected void verify() throws Exception {
				after = new Date();
				printResult("Neo4j persisting a UoW with " + number
						+ " entities\t", before, after);
				before = new Date();
				for(String identity : identities)
				{
					MakeBelieveEntity entity = getReference(identity, MakeBelieveEntity.class);
					Assert.assertNotNull(entity);
					entity.imaginaryName().get();
					entity.imaginaryNumber().get();
					entity.realNumber().get();
					entity.archNemesis().get();
					//System.out.println(identity);
				}
				after = new Date();
				printResult("Reading in " + number + " entities from Neo4j:\t\t", before, after);
			}

		});
		
	}

	private static void printResult(String name, Date before, Date after) {
	System.out.println(name
			+ (after.getTime() - before.getTime()) + "ms");
	}
}