/*  Copyright 2009 Tonny Kohar.
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
package org.qi4j.envisage.detail;

import javax.swing.*;

/**
 * Abstract Base class for DetailPane
 */
public abstract class DetailPane
    extends JPanel
{
    protected DetailModelPane detailModelPane;

    public DetailPane( DetailModelPane detailModelPane )
    {
        if( detailModelPane == null )
        {
            throw new IllegalArgumentException( "detailModelPane could not null" );
        }
        this.detailModelPane = detailModelPane;
    }

    public abstract void setDescriptor( Object objectDescriptor );
}
