package org.qi4j.demo.twominute;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

// START SNIPPET: documentation
public class Main
{
    public static void main( String[] args )
        throws Exception
    {
        SingletonAssembler assembler = new SingletonAssembler() // <1>
        {
            @Override
            public void assemble( ModuleAssembly assembly )
                throws AssemblyException
            {
                assembly.transients( Speaker.class );           // <2>
            }
        };
        Speaker speaker = assembler.module().newTransient( Speaker.class ); // <3>
        System.out.println( speaker.sayHello() );
    }
}
// END SNIPPET: documentation
