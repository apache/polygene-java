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
package org.qi4j.library.validation;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCallback;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;

public class ValidatableMixin
    implements Validatable
{
    @This Validatable validatable;

    public ValidatableMixin( @Structure UnitOfWorkFactory uowf )
    {
        UnitOfWork unitOfWork = uowf.currentUnitOfWork();
        if( unitOfWork != null )
        {
            UnitOfWorkCallback callback = new UnitOfWorkCallback()
            {
                public void beforeCompletion() throws UnitOfWorkCompletionException
                {
                    try
                    {
                        validatable.checkValid();
                    }
                    catch( ValidationException e )
                    {
                        throw (UnitOfWorkCompletionException) new UnitOfWorkCompletionException( "Validation failed" ).initCause( e );
                    }
                }

                public void afterCompletion( UnitOfWorkStatus status )
                {
                }
            };

            unitOfWork.registerUnitOfWorkCallback( callback );
        }
    }

    public List<ValidationMessage> validate()
    {
        return new ArrayList<ValidationMessage>();
    }

    public void checkValid() throws ValidationException
    {
        List<ValidationMessage> messages = validatable.validate();
        if( messages.size() > 0 )
        {
            throw new ValidationException( messages );
        }
    }
}
