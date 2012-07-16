package org.qi4j.sample.scala

import org.qi4j.library.constraints.annotation.MaxLength

trait HelloWorldMixin2
{
  def sayHello(@MaxLength(10) name: String ): String = "Hello " + name
}