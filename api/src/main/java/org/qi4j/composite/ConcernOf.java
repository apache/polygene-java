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
package org.qi4j.composite;

import org.qi4j.composite.internal.ConcernFor;

/**
 * Base class for Concerns. It introduces a typed "next" pointer
 * that Concerns can use to invoke the next Concern (or mixin) in
 * the chain.
 * <p/>
 * Generic Concerns should subclass {@link GenericConcern} instead.
 */
public abstract class ConcernOf<T>
{
    @ConcernFor protected T next;
}
