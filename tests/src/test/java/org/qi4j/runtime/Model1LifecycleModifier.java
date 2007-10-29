/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime;

import org.qi4j.api.annotation.scope.ConcernFor;
import org.qi4j.api.persistence.Lifecycle;

public class Model1LifecycleModifier
    implements Lifecycle
{
    static boolean deleteMethod;
    static boolean createMethod;

    @ConcernFor Lifecycle next;

    /**
     * Creation callback method.
     * <p/>
     * Called by the Qi4J runtime before the newInstance of the composite completes, allowing
     * for additional initialization.
     */
    public void create()
    {
        createMethod = true;
        next.create();
    }

    /**
     * Deletion callback method.
     * <p/>
     * Called by the Qi4J runtime before the composite is deleted from the system, allowing
     * for clean-up operations.
     */
    public void delete()
    {
        deleteMethod = true;
        next.delete();
    }
}
