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
package org.qi4j.spi.composite;

import org.qi4j.api.composite.Composite;

public class InvalidCompositeException
    extends RuntimeException
{
    private Class<? extends Composite> composite;
    private String message;
    private Class mixinClass;

    public InvalidCompositeException( String message, Class<? extends Composite> composite )
    {
        super( message );
        this.message = message;
        this.composite = composite;
    }

    public InvalidCompositeException()
    {
    }

    public Class<? extends Composite> getFailingComposite()
    {
        return composite;
    }

    @Override
    public String getMessage()
    {
        return message;
    }

    public void setMessage( String message )
    {
        this.message = message;
    }

    public String toString()
    {
        if( mixinClass == null )
        {
            return super.toString() + " in " + composite.getName();
        }
        else
        {
            return super.toString() + " for " + mixinClass.getName() + " in " + composite.getName();
        }
    }

    public void setFailingCompositeType( Class<? extends Composite> compositeType )
    {
        composite = compositeType;
    }

    public void setMixinClass( Class mixinClass )
    {
        this.mixinClass = mixinClass;
    }

    public Class getMixinClass()
    {
        return mixinClass;
    }
}
