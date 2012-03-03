/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.qi4j.runtime.query.model;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.runtime.query.model.values.ContactsValue;

public interface Person
    extends Nameable, Alive
{
    @Optional
    Association<City> placeOfBirth();

    @Optional
    Property<Integer> yearOfBirth();

    @Optional
    Association<Female> mother();

    @Optional
    Association<Male> father();

    @Optional
    ManyAssociation<Domain> interests();

    @Optional
    Property<String> email();

    @Optional
    Property<Map<Date, String>> datesToRemember();

    @Optional
    Property<List<String>> tags();

    @Optional
    Property<ContactsValue> contacts();
}
