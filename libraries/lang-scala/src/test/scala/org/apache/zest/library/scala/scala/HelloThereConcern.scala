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
package org.apache.zest.library.scala.scala

import org.apache.zest.api.concern.ConcernOf

// START SNIPPET: typedconcern
class HelloThereConcern
  extends ConcernOf[ HelloWorldMixin2 ] with HelloWorldMixin2
{
  override def sayHello(name: String ) = next.sayHello("there " + name)
}
// END SNIPPET: typedconcern
