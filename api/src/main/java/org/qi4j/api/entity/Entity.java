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
package org.qi4j.api.entity;

import org.qi4j.api.unitofwork.UnitOfWork;

/**
 * Interface that all Entities implement. It contains methods
 * that are specific for Entities.
 */
public interface Entity
{
    /**
     * Check if the Entity is a reference or not. A reference
     * is an Entity that has been requested from a UnitOfWork, but
     * has not yet been loaded.
     *
     * @return true if the Entity is a reference
     */
    boolean isReference();

    UnitOfWork unitOfWork();
}
