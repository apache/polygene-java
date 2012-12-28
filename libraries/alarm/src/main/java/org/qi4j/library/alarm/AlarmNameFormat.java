/*
 * Copyright 1996-2011 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.alarm;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;

/**
 * The definition of the format of AlarmPoint system names.
 * <p>
 * AlarmPoint Names must contain characters. The default is 5 characters but can possibly be overridden. It must also
 * not only contain white spaces.
 *
 *
 * To override the default minimumLength, specify the <b>minimumLength</b> value in the annotation, such as;
 * </p>
 * <pre><code>
 *
 * public interface ExpressiveAlarm extends AlarmPoint
 * {
 *     &#64AlarmNameFormat(minimumLength=25)
 *     Property<String> systemName();
 * }
 * </code></pre>
 * <p>
 * The obvious downside is that the Property become publicly visible to the users of AlarmPoint.
 * </p>
 */
@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@Constraints( AlarmNameFormat.AlarmNameConstraint.class )
public @interface AlarmNameFormat
{
    int minimumLength() default 5;

    class AlarmNameConstraint
        implements Constraint<AlarmNameFormat, String>
    {
        @Override
        public boolean isValid( AlarmNameFormat annotation, String value )
        {
            int length = annotation.minimumLength();
            if( length < 1 )
            {
                length = 1;
            }
            String trimmed = value.trim();
            boolean lengthConstraint = trimmed.length() >= length;
            boolean whiteSpaceConstraint = trimmed.length() > 0;
            return lengthConstraint && whiteSpaceConstraint;
        }
    }
}
