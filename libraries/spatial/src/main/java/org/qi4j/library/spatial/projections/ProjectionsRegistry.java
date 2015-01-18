/*
 * Copyright 2014 Jiri Jetmar.
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

package org.qi4j.library.spatial.projections;

import org.cts.CRSFactory;
import org.cts.crs.CRSException;
import org.cts.crs.CoordinateReferenceSystem;
import org.cts.registry.*;

import java.util.Set;


public class ProjectionsRegistry
{

    protected static CRSFactory cRSFactory = new CRSFactory();

    static
    {
        RegistryManager registryManager = cRSFactory.getRegistryManager();
        registryManager.addRegistry(new IGNFRegistry());
        registryManager.addRegistry(new EPSGRegistry());
        registryManager.addRegistry(new ESRIRegistry());
        registryManager.addRegistry(new Nad27Registry());
        registryManager.addRegistry(new Nad83Registry());
        registryManager.addRegistry(new worldRegistry());
    }


    public Set<String> getSupportedSRID(String registryName) throws RegistryException
    {
        return cRSFactory.getSupportedCodes(registryName);
    }

    // EPSG, IGNF, ESRI
    public Set<String> getSupportedRegistryCodes(String registryName) throws RegistryException
    {
        return cRSFactory.getRegistryManager().getRegistry(registryName).getSupportedCodes();
    }

    public String[] dumpRegistries()
    {
        return cRSFactory.getRegistryManager().getRegistryNames();
    }

    public CoordinateReferenceSystem getCRS(String wkt)
    {
        try
        {
            return cRSFactory.getCRS(wkt);
        } catch (CRSException _ex)
        {
            throw new RuntimeException(_ex);
        }
    }


    public CoordinateReferenceSystem getReferenceSystem(String csName) throws CRSException
    {
        return cRSFactory.getCRS(csName);
    }

}
