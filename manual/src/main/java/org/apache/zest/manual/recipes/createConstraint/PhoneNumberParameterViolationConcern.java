/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.manual.recipes.createConstraint;

import java.util.Collection;
import org.apache.zest.api.concern.ConcernOf;
import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.constraint.ConstraintViolation;
import org.apache.zest.api.constraint.ConstraintViolationException;
import org.apache.zest.api.property.Property;

// START SNIPPET: property
public abstract class PhoneNumberParameterViolationConcern extends ConcernOf<HasPhoneNumber>
    implements HasPhoneNumber
{
    @Concerns( CheckViolation.class )
    public abstract Property<String> phoneNumber();

    private abstract class CheckViolation extends ConcernOf<Property<String>>
        implements Property<String>
    {
        public void set( String number )
        {
            try
            {
                next.set( number );
            }
            catch( ConstraintViolationException e )
            {
                Collection<ConstraintViolation> violations = e.constraintViolations();
                report( violations );
            }
        }

// END SNIPPET: property

// START SNIPPET: property
        private void report( Collection<ConstraintViolation> violations )
        {
        }
    }
}
// END SNIPPET: property
