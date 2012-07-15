package org.qi4j.sample.scala

import org.qi4j.api.entity.EntityComposite
import org.qi4j.api.injection.scope.Service
import org.qi4j.api.common.UseDefaults
import org.qi4j.api.property.Property

/**
 * Test entity
 */
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