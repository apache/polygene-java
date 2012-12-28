package org.qi4j.sample.scala

import org.qi4j.api.composite.TransientComposite
import org.qi4j.api.concern.Concerns

@Concerns(Array(classOf[ HelloThereConcern ]))
trait HelloWorldComposite
  extends TransientComposite with HelloWorldMixin with HelloWorldMixin2
