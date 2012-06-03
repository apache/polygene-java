package org.qi4j.sample.scala

import org.qi4j.api.service.ServiceComposite

/**
 * Test service that repeats given string
 */
trait TestService
  extends ServiceComposite
{
  def repeat(input: String ): String = input + input
}