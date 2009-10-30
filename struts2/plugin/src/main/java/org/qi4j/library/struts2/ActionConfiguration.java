package org.qi4j.library.struts2;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

public class ActionConfiguration
    implements Assembler, Serializable
{
    static final long serialVersionUID = 1L;

    private final Set<Class> objectTypes;
    private final Set<Class<? extends Composite>> compositeTypes;

    public ActionConfiguration()
    {
        compositeTypes = new HashSet<Class<? extends Composite>>();
        objectTypes = new HashSet<Class>();
    }

    public void addObjects( Class... objectTypes )
        throws AssemblyException
    {
        for( Class objectType : objectTypes )
        {
            this.objectTypes.add( objectType );
        }
    }

    public void addComposites( Class<? extends Composite>... compositeTypes )
        throws AssemblyException
    {
        for( Class<? extends Composite> compositeType : compositeTypes )
        {
            this.compositeTypes.add( compositeType );
        }
    }

    public Set<Class> getClasses()
    {
        Set<Class> classes = new HashSet<Class>( objectTypes );
        classes.addAll( compositeTypes );
        return Collections.unmodifiableSet( classes );
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addObjects( objectTypes.toArray( new Class[]{ } ) ).visibleIn( Visibility.module );
        module.addTransients( (Class<? extends TransientComposite>[]) compositeTypes.toArray( new Class[]{ } ) )
            .visibleIn( Visibility.module );
    }
}
