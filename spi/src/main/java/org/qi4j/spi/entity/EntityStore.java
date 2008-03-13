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
package org.qi4j.spi.entity;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.qi4j.entity.EntitySession;
import org.qi4j.spi.composite.CompositeBinding;

public interface EntityStore<T extends EntityState>
{
    boolean exists( String identity, CompositeBinding compositeBinding )
        throws StoreException;

    T newEntityInstance( EntitySession session,
                         String identity,
                         CompositeBinding compositeBinding,
                         Map<Method, Object> propertyValues )
        throws StoreException;

    T getEntityInstance( EntitySession session,
                         String identity,
                         CompositeBinding compositeBinding )
        throws StoreException;

    void complete( EntitySession session, List<T> states )
        throws StoreException;
}
