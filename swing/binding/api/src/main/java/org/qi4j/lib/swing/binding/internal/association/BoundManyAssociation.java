package org.qi4j.lib.swing.binding.internal.association;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.Uses;
import org.qi4j.lib.swing.binding.StateModel;
import org.qi4j.lib.swing.binding.SwingAdapter;
import org.qi4j.lib.swing.binding.SwingBinding;
import org.qi4j.lib.swing.binding.internal.AbstractBinding;
import org.qi4j.object.ObjectBuilderFactory;

/**
 * @author Lan Boon Ping
 */
public abstract class BoundManyAssociation<T> extends AbstractBinding<T, ManyAssociation<T>, ManyAssociation<T>>
    implements ManyAssociation<T>
{

    private List<ManyAssociationDelegate> delegates;

    @SuppressWarnings( "unchecked" )
    public BoundManyAssociation(
        @Uses Method anAssociationMethod,
        @Structure ObjectBuilderFactory objectBuilderFactory,
        @Service Iterable<SwingAdapter> allAdapters )
    {
        super( anAssociationMethod, objectBuilderFactory, allAdapters );

        delegates = new ArrayList<ManyAssociationDelegate>();
    }

    public final Map<Class<? extends JComponent>, SwingAdapter> adapters()
    {
        return adapters;
    }

    public void delegate( ManyAssociationDelegate delegate )
    {
        delegates.add( delegate );
    }

    public final StateModel<T> stateModel()
    {
        return stateModel;
    }

    public int size()
    {
        throw new IllegalArgumentException( "size() is not allowed in binding templates." );
    }

    public boolean isEmpty()
    {
        throw new IllegalArgumentException( "isEmpty() is not allowed in binding templates." );
    }

    public boolean contains( Object o )
    {
        throw new IllegalArgumentException( "contains() is not allowed in binding templates." );
    }

    public Iterator<T> iterator()
    {
        throw new IllegalArgumentException( "iterator() is not allowed in binding templates." );
    }

    public Object[] toArray()
    {
        throw new IllegalArgumentException( "toArray() is not allowed in binding templates." );
    }

    public <T> T[] toArray( T[] a )
    {
        throw new IllegalArgumentException( "toArray() is not allowed in binding templates." );
    }

    public boolean add( T o )
    {
        throw new IllegalArgumentException( "add() is not allowed in binding templates." );
    }

    public boolean remove( Object o )
    {
        throw new IllegalArgumentException( "remove() is not allowed in binding templates." );
    }

    public boolean containsAll( Collection<?> c )
    {
        throw new IllegalArgumentException( "containsAll() is not allowed in binding templates." );
    }

    public boolean addAll( Collection<? extends T> c )
    {
        throw new IllegalArgumentException( "addAll() is not allowed in binding templates." );
    }

    public boolean removeAll( Collection<?> c )
    {
        throw new IllegalArgumentException( "removeAll() is not allowed in binding templates." );
    }

    public boolean retainAll( Collection<?> c )
    {
        throw new IllegalArgumentException( "retainAll() is not allowed in binding templates." );
    }

    public void clear()
    {
        throw new IllegalArgumentException( "clear() is not allowed in binding templates." );
    }

    public <K> K metaInfo( Class<K> infoType )
    {
        throw new IllegalArgumentException( "metaInfo() is not allowed in binding templates." );
    }

    public String name()
    {
        throw new IllegalArgumentException( "name() is not allowed in binding templates." );
    }

    public String qualifiedName()
    {
        throw new IllegalArgumentException( "qualifiedName() is not allowed in binding templates." );
    }

    public Type type()
    {
        return type;
    }

    public SwingBinding<T> to( JComponent component )
    {
        return delegates.get( delegates.size() - 1 );
    }

    public void stateInUse( ManyAssociation<T> aNewState )
    {
        for( ManyAssociationDelegate delegate : delegates )
        {
            delegate.stateInUse( aNewState );
        }
    }

    public void fieldInUse( ManyAssociation<T> anActualField )
    {
        for( ManyAssociationDelegate delegate : delegates )
        {
            delegate.fieldInUse( anActualField );
        }
    }

    @Override
    public String toString()
    {
        return name + "[" + type.getSimpleName() + "] -> " + stateModel.toString();
    }

}
