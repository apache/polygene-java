package org.qi4j.sample.scala

import org.qi4j.api.entity.EntityComposite
import org.qi4j.api.property.Property
import org.qi4j.api.common.UseDefaults

/**
 * TODO
 */

trait TestEntity
  extends EntityComposite with Commands with Events with Data

trait Commands
{
  self: Events =>
  def updateFoo(newValue : String)
  {
    // Check here if input is ok
    updatedFoo(newValue)
  }
}

trait Events
{
  self: Data =>
  def updatedFoo(newValue : String)
  {
    // Register change by modifying state
    foo = newValue
  }
}

// Raw data of entity goes here
trait Data
{
  @UseDefaults
  def foo: Property[String]
  def foo_=(v:String) = foo.set(v)
}