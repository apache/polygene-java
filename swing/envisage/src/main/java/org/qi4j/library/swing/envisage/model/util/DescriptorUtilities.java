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
package org.qi4j.library.swing.envisage.model.util;

import java.util.List;
import org.qi4j.library.swing.envisage.model.descriptor.ServiceDetailDescriptor;
import org.qi4j.library.swing.envisage.util.TableRow;

/**
 * Collection of Desciptor Utilities
 *
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class DescriptorUtilities
{
    private DescriptorUtilities()
    {
        throw new Error("This is a utility class for static methods");
    }

    /** Return Descriptor Detail
     * @param descriptor ServiceDetailDescriptor
     * @return Descritpor Detail or null 
     * */
    public static Object findServiceConfiguration ( ServiceDetailDescriptor descriptor )
    {
        return new ServiceConfigurationFinder().findConfigurationDescriptor( descriptor );
    }

    /** Return list of Descriptor Detail for particular Service Usage
     * @param descriptor ServiceDetailDescriptor
     * @return list of service usage (never return null) 
     * */
    public static List<TableRow> findServiceUsage (ServiceDetailDescriptor descriptor)
    {
        return new ServiceUsageFinder().findServiceUsage( descriptor );
    }
}
