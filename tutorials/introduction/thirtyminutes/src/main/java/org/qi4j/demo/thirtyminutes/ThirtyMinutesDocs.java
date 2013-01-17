package org.qi4j.demo.thirtyminutes;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.demo.tenminute.Confirmable;
import org.qi4j.demo.tenminute.HasCustomer;
import org.qi4j.demo.tenminute.HasLineItems;
import org.qi4j.demo.tenminute.HasSequenceNumber;
import org.qi4j.demo.tenminute.InventoryConcern;
import org.qi4j.demo.tenminute.LineItem;
import org.qi4j.demo.tenminute.PurchaseLimitConcern;

// START SNIPPET: 6
import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.gt;
import static org.qi4j.api.query.QueryExpressions.templateFor;

import org.qi4j.api.query.QueryBuilder;

// END SNIPPET: 6
public class ThirtyMinutesDocs
{
    Module module;

// START SNIPPET: 6
    @Structure private UnitOfWorkFactory uowFactory; //Injected
// END SNIPPET: 6

    {
// START SNIPPET: 6
        UnitOfWork uow = uowFactory.currentUnitOfWork();
        QueryBuilder<Order> builder = module.newQueryBuilder( Order.class );

        String orderNumber = "12345";
        HasSequenceNumber template = templateFor( HasSequenceNumber.class );
        builder.where( eq( template.number(), orderNumber ) );
        Query<Order> query = uow.newQuery( builder);

        Iterator<Order> result = query.iterator();

        if( result.hasNext() )
        {
            Order order = result.next();
        }
        else
        {
            // Deal with it wasn't found.
        }
// END SNIPPET: 6
    }

    interface Report {
        void addOrderToReport(Order order);
        void addCustomerToReport(String customerName);
    }



    {
        Report report = null;
        UnitOfWork uow = module.currentUnitOfWork();

// START SNIPPET: 7
        QueryBuilder<Order> builder = module.newQueryBuilder( Order.class );

        Calendar cal = Calendar.getInstance();
        cal.setTime( new Date() );
        cal.roll( Calendar.DAY_OF_MONTH, -90 );
        Date last90days = cal.getTime();
        Order template = templateFor( Order.class );
        builder.where( gt( template.createdDate(), last90days ) );
        Query<Order> query = uow.newQuery(builder);

        for( Order order : query )
        {
            report.addOrderToReport( order );
        }
// END SNIPPET: 7
    }

    {
        Report report = null;
        UnitOfWork uow = module.currentUnitOfWork();

// START SNIPPET: 8
        QueryBuilder<HasCustomer> builder = module.newQueryBuilder( HasCustomer.class );

        Calendar cal = Calendar.getInstance();
        cal.setTime( new Date() );
        cal.roll( Calendar.MONTH, -1 );
        Date lastMonth = cal.getTime();
        Order template1 = templateFor( Order.class );
        builder.where( gt( template1.createdDate(), lastMonth ) );
        Query<HasCustomer> query = uow.newQuery(builder);

        for( HasCustomer hasCustomer : query )
        {
            report.addCustomerToReport( hasCustomer.name().get() );
        }
// END SNIPPET: 8

    }

    public interface Order
    {
        void addLineItem( LineItem item );

        void removeLineItem( LineItem item );

        void completed();

        Property<Date> createdDate();
    }


    // START SNIPPET: 2
    public interface HasAuditTrail<M>
    {
        AuditTrail<M> auditTrail();
    }

    public interface AuditTrail<M> extends Property<List<Action<M>>>
    {}

    public interface Action<T> extends ValueComposite          // [2][3]
    {
        enum Type { added, removed, completed };

        @Optional Property<T> item();                          // [1]

        Property<Type> action();                               // [1]
    }

    public interface Trailable<M>
    {
        void itemAdded( M item );
        void itemRemoved( M item );
        void completed();
    }

    public class TrailableMixin<M>
            implements Trailable<M>
    {
        private @This HasAuditTrail<M> hasTrail;

        @Override
        public void itemAdded( M item )
        {
            addAction( item, Action.Type.added );
        }

        @Override
        public void itemRemoved( M item )
        {
            addAction( item, Action.Type.removed );
        }

        @Override
        public void completed()
        {
            addAction( null, Action.Type.completed );
        }

        private Action<M> addAction( M item, Action.Type type )
        {
            ValueBuilder<Action> builder =
                    module.newValueBuilder(Action.class);       // [4]
            Action<M> prototype = builder.prototypeFor( Action.class );
            prototype.item().set( item );
            prototype.action().set( type );
            Action instance = builder.newInstance();
            hasTrail.auditTrail().get().add( instance );
            return instance;
        }
    }
// END SNIPPET: 2

// START SNIPPET: 3
    public abstract class OrderAuditTrailConcern
            extends ConcernOf<Order>
            implements Order
    {
        @This Trailable<LineItem> trail;

        @Override
        public void addLineItem( LineItem item )
        {
            next.addLineItem( item );
            trail.itemAdded( item );
        }

        @Override
        public void removeLineItem( LineItem item )
        {
            next.removeLineItem( item );
            trail.itemRemoved( item );
        }

        @Override
        public void completed()
        {
            next.completed();
            trail.completed();
        }
    }
// END SNIPPET: 3

// START SNIPPET: 4
    public class AuditTrailConcern
            extends ConcernOf<InvocationHandler>
            implements InvocationHandler
    {
        @This Trailable trail;

        @Override
        public Object invoke( Object proxy, Method m, Object[] args )
                throws Throwable
        {
            Object retValue = next.invoke(proxy, m, args);
            String methodName = m.getName();
            if( methodName.startsWith( "add" ) )
            {
                trail.itemAdded( args[0] );
            }
            else if( methodName.startsWith( "remove" ) )
            {
                trail.itemRemoved( args[0] );
            }
            else if( methodName.startsWith( "complete" ) ||
                    methodName.startsWith( "commit" ) )
            {
                trail.completed();
            }

            return retValue;
        }
    }
// END SNIPPET: 4


// START SNIPPET: 5
    @Concerns({
            AuditTrailConcern.class,
            PurchaseLimitConcern.class,
            InventoryConcern.class
    })

    @Mixins( TrailableMixin.class )
    public interface OrderEntity
            extends Order, Confirmable,
            HasSequenceNumber, HasCustomer, HasLineItems,
            EntityComposite
    {
    }
// END SNIPPET: 5
}