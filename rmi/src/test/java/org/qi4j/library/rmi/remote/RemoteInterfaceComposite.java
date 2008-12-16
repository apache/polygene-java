/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.qi4j.library.rmi.remote;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.library.rmi.RMIMixin;
import org.qi4j.library.cache.InvocationCacheAbstractComposite;

/**
 * Implement the RemoteInterface by using RMI.
 * Results of RMI calls are cached, so if an
 * IOException occurs we can reuse a previous result
 * if possible.
 */
@Mixins( RMIMixin.class )
public interface RemoteInterfaceComposite
    extends RemoteInterface, InvocationCacheAbstractComposite, Composite
{
}
