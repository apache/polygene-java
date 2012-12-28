package org.qi4j.sample.scala

/**
 * TODO
 */

trait HelloWorldMixin3
  extends HelloWorldMixin
{
  override def doStuff() = "Do custom stuff"
}