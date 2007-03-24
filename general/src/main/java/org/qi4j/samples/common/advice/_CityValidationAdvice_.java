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
package org.qi4j.samples.common.advice;

import org.qi4j.runtime.advice.Advice;
import org.qi4j.samples.common.mixin.Address;
import org.qi4j.samples.common.validation.mixin.Validation;

/** THIS IS A SAMPLE GENERATED ADVICE SUBCLASS * */
public class _CityValidationAdvice_ extends CityValidationAdvice
    implements Advice
{
    public final Object getTarget( Class targetClass )
    {
        return next;
    }

    public final void resolveDependency( Object instance )
    {
        target = (Address) instance;
    }

    public final void setTarget( Class targetClass, Object target )
    {
        next = (Validation) target;
    }
}
