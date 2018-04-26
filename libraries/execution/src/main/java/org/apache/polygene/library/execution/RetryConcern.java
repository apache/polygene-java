package org.apache.polygene.library.execution;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.polygene.api.common.AppliesTo;
import org.apache.polygene.api.concern.ConcernOf;
import org.apache.polygene.api.injection.scope.Invocation;

import static org.apache.polygene.api.util.Classes.classHierarchy;

@AppliesTo( Retry.class )
public class RetryConcern extends ConcernOf<InvocationHandler>
    implements InvocationHandler
{
    private final int retries;
    private final HashSet<Class<? extends Throwable>> on;
    private final HashSet<Object> unless;
    private final int backoff;

    @SuppressWarnings( "unchecked" )
    public RetryConcern( @Invocation Retry annotation )
    {
        this.retries = annotation.value();
        if( retries < 1 )
        {
            throw new IllegalArgumentException( "@Retry must have a positive value greater than zero." );
        }
        this.on = new HashSet<>();
        List<Class<? extends Throwable>> on = Arrays.asList( annotation.on() );
        this.on.addAll( on );

        this.unless = new HashSet<>();
        List<Class<? extends Throwable>> unless = Arrays.asList( annotation.unless() );
        this.unless.addAll( unless );
        this.backoff = annotation.backoff();
    }

    @Override
    @SuppressWarnings( { "SuspiciousMethodCalls", "ConstantConditions" } )
    public Object invoke( Object o, Method method, Object[] objects )
        throws Throwable
    {
        int count = retries;
        long sleep = backoff;
        while( true )
        {
            try
            {
                return next.invoke( o, method, objects );
            }
            catch( Throwable e )
            {
                --count;
                List<Class<?>> types = classHierarchy( e.getClass() ).collect( Collectors.toList() );
                for( Class<?> type : types )
                {
                    if( this.unless.contains( type ) )
                    {
                        throw e;
                    }
                    if( count == 0 && this.on.contains( type ))
                    {
                        throw e;
                    }
                }
                if( sleep > 0 )
                {
                    Thread.sleep( sleep );
                    sleep = sleep * 2;
                }
            }
        }
    }
}
