/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.api.concern;

import java.lang.reflect.InvocationHandler;

/**
 * Base class for generic Concerns. Subclass
 * and implement the "invoke" method. Use the
 * "next" field in {@link ConcernOf} to continue the invocation
 * chain.
 */
public abstract class GenericConcern
    extends ConcernOf<InvocationHandler>
    implements InvocationHandler
{
}