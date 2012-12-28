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
package org.qi4j.test.indexing.model;

import java.util.Map;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.Property;

/**
 * JAVADOC Add JavaDoc
 */
public interface Person
    extends Nameable, Alive
{
    enum Title
    {
        MR, MS, MRS, DR
    }

    Property<Title> title();

    @Optional
    Association<City> placeOfBirth();

    Property<Integer> yearOfBirth();

    @Optional
    Property<Address> address();

    @Optional
    Association<Female> mother();

    @Optional
    Association<Male> father();

    ManyAssociation<Domain> interests();

    @Optional
    Property<String> email();

    @Optional
    Property<URL> personalWebsite();

    @Queryable( false )
    Property<String> password();

    @Queryable( false )
    @Optional
    Association<Account> mainAccount();

    @Queryable( false )
    ManyAssociation<Account> accounts();

    @Optional
    Property<Map<String, String>> additionalInfo();
}
