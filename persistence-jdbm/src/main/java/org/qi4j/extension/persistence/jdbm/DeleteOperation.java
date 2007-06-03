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
package org.qi4j.extension.persistence.jdbm;

import java.io.IOException;
import java.util.Map;
import jdbm.RecordManager;
import org.qi4j.api.persistence.binding.PersistenceBinding;

class DeleteOperation
    implements Operation
{
    private PersistenceBinding binding;

    public DeleteOperation( PersistenceBinding binding )
    {
        this.binding = binding;
    }

    public void perform( RecordManager recordManager )
    {
        String identity = binding.getIdentity();
        try
        {
            long recordId = recordManager.getNamedObject( identity );
            recordManager.delete( recordId );
        }
        catch( IOException e )
        {
            throw new DeleteOperationException( "Unable to delete object [" + identity + "]", e );
        }
    }

    public String getIdentity()
    {
        return binding.getIdentity();
    }

    public void playback( String identity, Map<Class, Object> mixins )
    {
        if( identity.equals( binding.getIdentity() ) )
        {
            mixins.clear();
        }
    }

}
