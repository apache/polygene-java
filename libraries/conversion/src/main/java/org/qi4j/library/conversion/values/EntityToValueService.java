/*
 * Copyright 2010 Niclas Hedhman.
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
package org.qi4j.library.conversion.values;

import org.qi4j.api.service.ServiceComposite;

/** The EntityToValueService converts Entities to matching Values.
 * <p>
 * The main purpose of this service is to provide convenience to map entities to serializable values, for instance
 * to be transported across a network. The ValueComposite to convert the Entity to must closely match the Entity
 * for this mapping to work. The rules are fairly straight forward;
 * </p>
 * <ol>
 *  <li>Any Property in the EntityComposite to be mapped, must (by default) exist in the ValueComposite with the same
 *      fully qualified name, i.e. method declared in the same interface. If the ValueComposite is annotated with
 *      &#64;Unqualified then the Property method look up will only locate the properties with the name only,
 *      i.e. the methods may defined in different interfaces.</li>
 *  <li>For any Association in the EntityComposite, a Property&lt;String&gt; with the same <strong>unqualified</strong>
 *      name will be looked up in the ValueComposite. If found, the EntityReference of the Association will be
 *      converted to an URI and written to the String property.</li>
 *  <li>For any ManyAssociation in the EntityComposite, a Property&lt;List&lt;String&gt;&gt; with the same <strong>
 *      unqualified</strong> name will be looked up in the ValueComposite. If found, the EntityReferences in the
 *      ManyAssociation will be converted to URIs and placed into a List and set to the Property in ValueComposite.</li>
 *
 * </ol>
 * <p>
 * If a Property from the Entity is not found in the Value, then it is ignored.
 * </p>
 */
public interface EntityToValueService extends EntityToValue, ServiceComposite
{
}
