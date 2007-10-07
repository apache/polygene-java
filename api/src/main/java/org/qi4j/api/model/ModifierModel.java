package org.qi4j.api.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.Iterator;

/**
 * Modifiers provide stateless modifications of method invocation behaviour.
 * <p/>
 * Modifiers can either be classes implementing the interfaces of the modified
 * methods, or they can be generic InvocationHandler mixins.
 */
public abstract class ModifierModel<T>
    extends FragmentModel<T>
{
    private Dependency modifiesDependency;

    public ModifierModel( Class<T> fragmentClass, Iterable<ConstructorDependency> constructorDependencies, Iterable<FieldDependency> fieldDependencies, Iterable<MethodDependency> methodDependencies, Class appliesTo )
    {
        super( fragmentClass, constructorDependencies, fieldDependencies, methodDependencies, appliesTo );

        Iterator<Dependency> modifies = getDependenciesByScope( getModifiesAnnotationType() ).iterator();
        if( modifies.hasNext() )
        {
            this.modifiesDependency = modifies.next();
        }
        else
        {
            String msg = MessageFormat.format( "Invocation {0} does not have any member fields marked with @{1}.", fragmentClass.getName(), getModifiesAnnotationType().getSimpleName() );
            throw new InvalidFragmentException( msg, fragmentClass );
        }
        if( modifies.hasNext() )
        {
            String msg = MessageFormat.format( "Invocation {0} has many member fields marked with @{1}.", fragmentClass.getName(), getModifiesAnnotationType().getSimpleName() );
            throw new InvalidFragmentException( msg, fragmentClass );
        }
    }

    public abstract Class<? extends Annotation> getModifiesAnnotationType();

    public Dependency getModifiesDependency()
    {
        return modifiesDependency;
    }

    public String toString()
    {
        String string = super.toString();

        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( "  @" + getModifiesAnnotationType().getSimpleName() );
        out.println( "    " + modifiesDependency.getKey().getRawType().getSimpleName() );

        if( appliesTo != null )
        {
            out.println( "  @AppliesTo" );
            out.println( "    " + appliesTo.getName() );
        }
        out.close();
        return string + str.toString();
    }
}
