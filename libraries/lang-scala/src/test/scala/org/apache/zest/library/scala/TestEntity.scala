/*
 * Copyright 2011 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.apache.zest.library.scala

import org.apache.zest.api.entity.EntityComposite
import org.apache.zest.api.injection.scope.Service
import org.apache.zest.api.common.UseDefaults
import org.apache.zest.api.property.Property

/**
 * Test entity
 */
 // START SNIPPET: entity
trait TestEntity
  extends EntityComposite with Commands with Events with Data

trait Commands
{
  self: Events =>
  def updateFoo(newValue: String )
  {
    // Call "injected" service
    val repeated = testService.repeat(newValue)

    // Check here if input is ok
    updatedFoo(repeated)
  }

  // Service injection - this is really a method call to the ServiceFinder of the composite
  @Service
  def testService: TestService
}

// Raw data of entity goes here
trait Data
{
  @UseDefaults
  def foo: Property[ String ]

  // Define property
  def foo_=(v: String ) { foo.set(v)  } // Operator overloading for =
}

trait Events
{
  self: Data =>
  def updatedFoo(newValue: String )
  {
    // Register change by modifying state
    foo = newValue
  }
}
// END SNIPPET: entity
