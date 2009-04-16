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
package org.qi4j.library.swing.envisage.model.descriptor;

import java.util.LinkedList;
import java.util.List;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.util.Classes;
import org.qi4j.spi.composite.AbstractCompositeDescriptor;

/**
 * XXX Workaround for inconsistency in Qi4J core-api/spi
 * ImportedServiceDescriptor wrapper as composite
 *
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class ImportedServiceCompositeDescriptor implements AbstractCompositeDescriptor
{
    protected ImportedServiceDescriptor importedService;
    protected List<Class> mixins;

    public ImportedServiceCompositeDescriptor( ImportedServiceDescriptor importedService )
    {
        this.importedService = importedService;
        mixins = new LinkedList<Class>();
    }

    public ImportedServiceDescriptor importedService()
    {
        return importedService;
    }

    public Iterable<Class> mixinTypes()
    {
        return mixins;
    }

    public Class<?> type()
    {
        return importedService.type();
    }

    public Visibility visibility()
    {
        return importedService.visibility();
    }

    public MetaInfo metaInfo()
    {
        return importedService.metaInfo();
    }

    public Class<? extends ServiceImporter> serviceImporter()
    {
        return importedService.serviceImporter();
    }
}
