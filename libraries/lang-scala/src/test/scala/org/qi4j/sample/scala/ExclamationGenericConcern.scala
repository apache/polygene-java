package org.qi4j.sample.scala

import java.lang.reflect.Method
import org.qi4j.api.concern.GenericConcern
import org.qi4j.api.common.{AppliesToFilter, AppliesTo}

/**
 * Add an exclamation mark to the returned string
 */

@AppliesTo(Array(classOf[ StringFilter ]))
class ExclamationGenericConcern
  extends GenericConcern
{
  def invoke(composite: AnyRef, method: Method, args: Array[ AnyRef ] ) = next.invoke(composite, method, args) + "!"
}

class StringFilter
  extends AppliesToFilter
{
  def appliesTo(method: Method, mixin: Class[ _ ], compositeType: Class[ _ ], fragmentClass: Class[ _ ] ) = method
    .getReturnType
    .equals(classOf[ String ])
}
