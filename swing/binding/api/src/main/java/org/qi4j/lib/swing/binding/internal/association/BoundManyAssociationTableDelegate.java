package org.qi4j.lib.swing.binding.internal.association;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.lib.swing.binding.StateModel;
import org.qi4j.lib.swing.binding.TableBinding;
import org.qi4j.lib.swing.binding.internal.property.BoundProperty;
import org.qi4j.property.Property;

/**
 * @author Lan Boon Ping
 */
public class BoundManyAssociationTableDelegate<T>
    implements TableBinding<T>, BoundManyAssociationDelegate<T, ManyAssociation<T>>
{

    private StateModel<T> stateModel;

    private JTable table;

    private Map<Integer, Property> boundColumnMap;

    private List<BoundProperty> validatedBoundColumns;

    public BoundManyAssociationTableDelegate( StateModel<T> stateModel )
    {
        this.stateModel = stateModel;

        boundColumnMap = new HashMap<Integer, Property>();
    }

    public TableBinding<T> to( JComponent component )
    {
        if( !( component instanceof JTable ) )
        {
            throw new IllegalArgumentException( "[component] must be type of JTable." );
        }

        if( this.table != null )
        {
            throw new IllegalArgumentException( "You are allowed to bind to one Jtable only." );
        }

        this.table = (JTable) component;

        return this;
    }

    public StateModel<T> stateModel()
    {
        return stateModel;
    }

    public void bindColumn( Property property, int columnIndex )
    {
        if( boundColumnMap.containsKey( columnIndex ) )
        {
            throw new IllegalArgumentException( "Duplicate ColumnIndex " + columnIndex );
        }

        boundColumnMap.put( columnIndex, property );
    }

    @SuppressWarnings( "unchecked" )
    public void stateToUse( ManyAssociation<T> aNewAssociation )
    {

        DefaultTableModel model = (DefaultTableModel) table.getModel();

        int rowIndex = 0;
        int totalColumns = model.getColumnCount();

        model.setNumRows( aNewAssociation.size() );

        initValidatedBoundColumns( totalColumns );

        for( T t : aNewAssociation )
        {
            for( int columnIndex = 0; columnIndex < totalColumns; columnIndex++ )
            {
                BoundProperty property = validatedBoundColumns.get( columnIndex );

                Method method = property.propertyMethod();

                Property<Object> actualProperty = getActualProperty( method, t );

                if( actualProperty != null )
                {
                    model.setValueAt( actualProperty.get(), rowIndex, columnIndex );
                }
            }

            rowIndex++;
        }
    }

    private void initValidatedBoundColumns( int totalColumns )
    {
        if( validatedBoundColumns == null )
        {
            validatedBoundColumns = new ArrayList<BoundProperty>();

            for( int columnIndex = 0; columnIndex < totalColumns; columnIndex++ )
            {
                BoundProperty property = (BoundProperty) boundColumnMap.get( columnIndex );

                if( property == null )
                {
                    throw new IllegalStateException( "ColumnIndex " + columnIndex + " doesn't exist." );
                }

                validatedBoundColumns.add( property );
            }
        }
    }

    private Property<Object> getActualProperty( Method method, T object )
    {
        Property<Object> actualProperty;

        try
        {
            actualProperty = (Property<Object>) method.invoke( object );

            return actualProperty;
        }
        catch( IllegalAccessException e )
        {
            e.printStackTrace();
        }
        catch( InvocationTargetException e )
        {
            e.printStackTrace();
        }

        return null;
    }

    public void fieldToUse( ManyAssociation<T> aNewAssociation )
    {
        stateToUse( aNewAssociation );
    }
}
