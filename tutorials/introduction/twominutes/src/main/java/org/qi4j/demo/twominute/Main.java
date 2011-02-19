package org.qi4j.demo.twominute;

import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

public class Main
{
    public static void main( String[] args )
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly assembly )
                throws AssemblyException
            {
                assembly.addTransients( PoliticianComposite.class );
            }
        };
        TransientBuilderFactory factory =
            assembler.transientBuilderFactory();
        Speaker speaker =
            factory.newTransient( PoliticianComposite.class );
        System.out.println( speaker.sayHello() );
    }
}