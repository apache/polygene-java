package com.pathfinder.assembly;

import com.pathfinder.api.GraphTraversalService;
import com.pathfinder.internal.GraphDAO;
import com.pathfinder.internal.GraphTraversalServiceImpl;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

public class PathFinderModuleAssembler
    implements Assembler
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.importServices( GraphTraversalService.class ).importedBy( GraphTraversalServiceImport.class );
    }

    public static class GraphTraversalServiceImport
        implements ServiceImporter
    {

        public Object importService( ImportedServiceDescriptor serviceDescriptor )
            throws ServiceImporterException
        {
            GraphDAO dao = new GraphDAO();
            return new GraphTraversalServiceImpl( dao );
        }

        public boolean isActive( Object instance )
        {
            return true;
        }

        public boolean isAvailable( Object instance )
        {
            return true;
        }
    }
}
