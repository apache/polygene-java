/*
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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
package org.qi4j.envisage.util;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.tools.model.descriptor.MixinDetailDescriptor;
import org.qi4j.tools.model.descriptor.ObjectDetailDescriptor;
import org.qi4j.tools.model.descriptor.ServiceUsage;

public final class TableRowUtilities
{

    public static List<TableRow> toTableRows( List<ServiceUsage> serviceUsages )
    {
        List<TableRow> rows = new ArrayList<>();
        for( ServiceUsage usage : serviceUsages )
        {
            TableRow row = new TableRow( 5 );
            if( usage.ownerDescriptor() instanceof MixinDetailDescriptor )
            {
                MixinDetailDescriptor mixinDescriptor = (MixinDetailDescriptor) usage.ownerDescriptor();
                row.set( 0, mixinDescriptor.composite() );
                row.set( 1, usage.field() );
                row.set( 2, mixinDescriptor.composite().module() );
                row.set( 3, mixinDescriptor.composite().module().layer() );
            }
            else
            {
                // assume ObjectDetailDescriptor
                ObjectDetailDescriptor objectDescriptor = (ObjectDetailDescriptor) usage.ownerDescriptor();
                row.set( 0, objectDescriptor );
                row.set( 1, usage.field() );
                row.set( 2, objectDescriptor.module() );
                row.set( 3, objectDescriptor.module().layer() );
            }
            rows.add( row );
        }
        return rows;
    }

    private TableRowUtilities()
    {
    }

}
