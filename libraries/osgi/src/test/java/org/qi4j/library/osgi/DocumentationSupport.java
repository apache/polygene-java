package org.qi4j.library.osgi;

import org.osgi.framework.BundleContext;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

public class DocumentationSupport
{

    public static class Export
        implements Assembler
    {

        // START SNIPPET: export
        interface MyQi4jService
            extends OSGiEnabledService
        {
            // ...
        }
        // END SNIPPET: export

        @Override
        // START SNIPPET: export
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            BundleContext bundleContext = // ...
                          // END SNIPPET: export
                          null;
            // START SNIPPET: export
            module.services( OSGiServiceExporter.class ).
                setMetaInfo( bundleContext );
            module.services( MyQi4jService.class );
        }
        // END SNIPPET: export

    }

    interface MyOSGiService
    {
    }

    interface MyOtherOSGiService
    {
    }

    static class MyFallbackStrategy
    {
    }

    public static class Import
        implements Assembler
    {

        @Override
        // START SNIPPET: import
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            BundleContext bundleContext = // END SNIPPET: import
                          null;
            // START SNIPPET: import
            module.services( OSGiServiceImporter.class ).
                setMetaInfo( new OSGiImportInfo( bundleContext,
                                                 MyOSGiService.class,
                                                 MyOtherOSGiService.class ) ).
                setMetaInfo( new MyFallbackStrategy() );
        }
        // END SNIPPET: import

    }

}
