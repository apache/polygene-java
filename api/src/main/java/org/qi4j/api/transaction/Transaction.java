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
package org.qi4j.api.transaction;

public interface Transaction
{
    void begin();

    void commit();

    void rollback();

    /**
     * urn:<repo>:<identity>
     * urn:<repo>:<type-identifier>:<identity>
     *
     * public class AbcMixin
     * {
     *   private Def def;
     *   private Rst def;
     *   private Xyz def;
     * }
     * @param identity
     * @param compositeType
     * @return
     */
    TransactionComposite getInstance( String identity, Class compositeType );

    TransactionComposite getInstance( String identity, Class compositeType, boolean autoCreate );
}
