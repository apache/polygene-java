/*
 * Copyright 2008 Georg Ragaller. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.qi4j.library.constraints;

import java.util.Collection;
import java.util.List;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.*;

/**
 * Test composite with all the constraints
 */
@Mixins( TestCaseComposite.TestCaseMixin.class)
public interface TestCaseComposite extends TransientComposite
{
    // START SNIPPET: constraints
    @Contains( "foo" ) Property<String> containsString();

    @Email Property<String> email();

    @URL Property<String> url();

    @URI Property<String> uri();

    @GreaterThan( 10 ) Property<Integer> greaterThan();

    @InstanceOf( List.class ) Property<Collection> instanceOf();

    @LessThan( 10 ) Property<Integer> lessThan();

    @Matches( "a*b*c*" ) Property<String> matches();

    @MaxLength( 3 ) Property<String> maxLength();

    @MinLength( 3 ) Property<String> minLength();

    @NotEmpty Property<String> notEmptyString();

    @NotEmpty Property<Collection> notEmptyCollection();

    @NotEmpty Property<List> notEmptyList();

    @Range( min = 0, max = 100 ) Property<Integer> range();

    @OneOf( { "Bar", "Xyzzy" } ) Property<String> oneOf();

    void testParameters(@GreaterThan(10) Integer greaterThan);
    // END SNIPPET: constraints

    abstract class TestCaseMixin
        implements TestCaseComposite
    {
        public void testParameters( @GreaterThan( 10 ) Integer greaterThan )
        {
        }
    }
}
