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

import java.lang.annotation.*;

/** If the ValueComposite is annotated with this, it means that the lookup of the Value Property should be performed
 * using the <strong>unqualified</strong> name only, and not via the default of the full qualified name. This
 * means that the Property may be declared in the different interfaces and still be matched.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
@Documented
public @interface Unqualified
{
    boolean value() default true;
}
