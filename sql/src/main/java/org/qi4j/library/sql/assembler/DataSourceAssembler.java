package org.qi4j.library.sql.assembler;

import org.qi4j.api.specification.Specification;
import org.qi4j.api.specification.Specifications;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.circuitbreaker.CircuitBreaker;
import org.qi4j.library.circuitbreaker.CircuitBreakers;
import org.qi4j.spi.service.importer.ServiceInstanceImporter;

import javax.sql.DataSource;
import java.net.ConnectException;

import static org.qi4j.api.specification.Specifications.not;
import static org.qi4j.library.circuitbreaker.CircuitBreakers.in;
import static org.qi4j.library.circuitbreaker.CircuitBreakers.rootCause;

/**
 * TODO
 */
public class DataSourceAssembler
   implements Assembler
{
   private String dataSourceServiceId;
   private String dataSourceId;

   public DataSourceAssembler(String dataSourceServiceId, String dataSourceId)
   {
      this.dataSourceServiceId = dataSourceServiceId;
      this.dataSourceId = dataSourceId;
   }

   @Override
   public void assemble(ModuleAssembly module) throws AssemblyException
   {
      module.importedServices(DataSource.class).
              importedBy( ServiceInstanceImporter.class ).
              setMetaInfo( dataSourceServiceId ).
              setMetaInfo(new CircuitBreaker(5, 1000*60*5, not( rootCause( in( ConnectException.class ) ) ))).
              identifiedBy( dataSourceId );
   }
}
