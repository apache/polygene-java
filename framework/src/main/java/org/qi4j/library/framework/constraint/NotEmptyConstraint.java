/*
 * Copyright 2008 Richard Wallace
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
package org.qi4j.library.framework.constraint;

import org.qi4j.composite.Constraint;
import org.qi4j.library.framework.constraint.annotation.NotEmpty;

/**
 * Constraints that a string is non empty.
 *
 * @author Richard Wallace
 * @author Alin Dreghiciu
 * @since 09 May, 2008
 */
public class NotEmptyConstraint
    implements Constraint<NotEmpty, String>
{

    public boolean isValid( NotEmpty annotation, String value )
    {
        return (null != value) && (value.trim().length() > 0);
    }

}
