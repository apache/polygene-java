package org.qi4j.sample.scala

import org.qi4j.api.concern.ConcernOf

/**
 * TODO
 */
class HelloThereConcern
  extends ConcernOf[ HelloWorldMixin2 ] with HelloWorldMixin2
{
  override def sayHello(name: String ) = next.sayHello("there " + name)
}