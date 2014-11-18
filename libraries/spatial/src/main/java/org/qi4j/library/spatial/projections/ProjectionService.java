package org.qi4j.library.spatial.projections;

import org.cts.CRSFactory;
import org.cts.IllegalCoordinateException;
import org.cts.crs.CRSException;
import org.cts.crs.CoordinateReferenceSystem;
import org.cts.crs.GeodeticCRS;
import org.cts.op.CoordinateOperation;
import org.cts.op.CoordinateOperationFactory;
import org.cts.registry.*;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;

import java.util.List;
import java.util.Set;

@Mixins( ProjectionService.Mixin.class )
public interface ProjectionService
        extends ServiceComposite, ServiceActivation {

    void test() throws Exception;
    double[] transform(String csNameSrc, String csNameDest, double[] pointSource) throws IllegalCoordinateException, CRSException;
    Set<String> getSupportedSRID(String registryName) throws RegistryException;


    public abstract class Mixin implements ProjectionService
    {

        protected static CRSFactory cRSFactory;

        // private static SpatialRefRegistry srr = new SpatialRefRegistry();

        public void test() throws Exception
        {

            // getSupportedCodes
            // Set<String> codes =  cRSFactory.getSupportedCodes("EPSG");

            // System.out.println(codes);

            // CoordinateReferenceSystem crs = cRSFactory .createFromPrj(prj);
            System.out.println(cRSFactory.getCRS("EPSG:4326").getAuthorityKey()); //  getAuthorityName());

            // String csNameSrc = "EPSG:4326"; //Input EPSG
            String csNameSrc = "4326"; //Input EPSG

            String csNameDest = "EPSG:27582";  //Target EPSG lambert 2 etendu france

            CoordinateReferenceSystem inputCRS = cRSFactory.getCRS(csNameSrc);
            System.out.println(inputCRS.toWKT());
            CoordinateReferenceSystem outputCRS = cRSFactory.getCRS(csNameDest);

        }

        public Set<String> getSupportedSRID(String registryName) throws RegistryException
        {
            // cRSFactory.
            return cRSFactory.getSupportedCodes(registryName);
        }

        public double[] transform(String csNameSrc, String csNameDest, double[] pointSource) throws IllegalCoordinateException, CRSException {

            CoordinateReferenceSystem inputCRS = cRSFactory.getCRS(csNameSrc);
            CoordinateReferenceSystem outputCRS = cRSFactory.getCRS(csNameDest);

            return transform((GeodeticCRS) inputCRS, (GeodeticCRS) outputCRS, pointSource);
        }


        public double[] transform(GeodeticCRS sourceCRS, GeodeticCRS targetCRS, double[] pointSource) throws IllegalCoordinateException {

            if (sourceCRS.equals(targetCRS))
                return pointSource;

            List<CoordinateOperation> ops;
            ops = CoordinateOperationFactory.createCoordinateOperations(sourceCRS, targetCRS);
            if (!ops.isEmpty()) {
               // if (verbose) {
                    System.out.println(ops.get(0));
               // }
                return ops.get(0).transform(new double[]{pointSource[0], pointSource[1], pointSource[2]});
            } else {
                return new double[]{0.0d, 0.0d, 0.0d};
            }
        }


        @Structure
        Module module;

        public void activateService()
                throws Exception
        {

            cRSFactory = new CRSFactory();
            RegistryManager registryManager = cRSFactory.getRegistryManager();
            registryManager.addRegistry(new IGNFRegistry());
            registryManager.addRegistry(new EPSGRegistry());
            registryManager.addRegistry(new ESRIRegistry());
            registryManager.addRegistry(new Nad27Registry());
            registryManager.addRegistry(new Nad83Registry());
            registryManager.addRegistry(new worldRegistry());

        }

        // END SNIPPET: realm-service
        public void passivateService()
                throws Exception
        {
        }


    }
}
