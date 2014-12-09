package org.qi4j.library.spatial.v2.projections;

import org.cts.CRSFactory;
import org.cts.crs.CRSException;
import org.cts.crs.CoordinateReferenceSystem;
import org.cts.registry.*;

import java.util.Set;

/**
 * Created by jj on 17.11.14.
 */
public class ProjectionsRegistry {

    protected static CRSFactory cRSFactory = new CRSFactory();


    static {
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
        // cRSFactory.

        return cRSFactory.getSupportedCodes(registryName);
    }

    // EPSG, IGNF, ESRI
    public Set<String> getSupportedRegistryCodes(String registryName) throws RegistryException {
        return cRSFactory.getRegistryManager().getRegistry(registryName).getSupportedCodes();
    }

    public String[] dumpRegistries() {
        return cRSFactory.getRegistryManager().getRegistryNames();
    }

    public CoordinateReferenceSystem getCRS(String wkt)  {
        try {
            return cRSFactory.getCRS(wkt);
        } catch(CRSException _ex)
        {
            _ex.printStackTrace();
            return null;
        }
    }


        public CoordinateReferenceSystem getReferenceSystem(String csName) throws CRSException
    {
        return cRSFactory.getCRS(csName);
    }

}
