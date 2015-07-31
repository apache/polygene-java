/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_a.infrastructure.dci;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Context
 *
 * Base class for DCI contexts.
 *
 * Helps assigning Entity objects to Roles and to "inject" the Context object into Methodful Roles.
 */
public abstract class Context
{
    protected Logger logger = LoggerFactory.getLogger( getClass() );

    protected static UnitOfWorkFactory uowf;

    /*
    * Role assignment + Context "injection"
    *
    * Cast a Data object to a Role, set the Context object in the Role and return the RolePlayer.
    *
    * Requirements:
    * 1) SomeRole interface declares a 'void setContext(YourContext context);' method
    * 2) SomeRole.Mixin class extends RoleMixin<YourContext>
    *
    * The RolePlayer can then use the context pointer to lookup other Roles in the current Context.
    */
    protected <T> T rolePlayer( Class<T> roleClass, Object dataObject )
    {
        if( dataObject == null )
        {
            return null;
        }

        objectCanPlayRole( roleClass, dataObject );
        T rolePlayer = roleClass.cast( dataObject );
        setContext( rolePlayer, this );
        return rolePlayer;
    }

    private <T> void objectCanPlayRole( Class<T> roleClass, Object dataObject )
    {
        if( roleClass.isAssignableFrom( dataObject.getClass() ) )
        {
            return;
        }

        String className;
        if( dataObject instanceof Proxy )
        {
            className = Proxy.getInvocationHandler( dataObject ).getClass().getSimpleName();
        }
        else
        {
            className = dataObject.getClass().getSimpleName();
        }

        throw new IllegalArgumentException(
            "Object '" + className + "' can't play Role of '" + roleClass.getSimpleName() + "'" );
    }

    /*
    * Poor mans Context injection
    * */
    private <T> void setContext( T rolePlayer, Context context )
    {
        try
        {
            Method setContextMethod = rolePlayer.getClass().getDeclaredMethod( "setContext", context.getClass() );

            // Set Context in Role
            setContextMethod.invoke( rolePlayer, context );
        }
        catch( Exception e )
        {
            String c = context.getClass().getSimpleName();
            String r = rolePlayer.getClass().getSimpleName();
            String msg = "Couldn't invoke 'void setContext( " + c + " context);' on " + r + "." +
                         "\nPlease check the following requirements: " +
                         "\n1) 'void setContext( " + c + " context);' is declared in " + r + " interface." +
                         "\n2) " + r + " extends RoleMixin<" + c + ">";
            logger.error( msg, e.getMessage() );
            e.printStackTrace();
            throw new RuntimeException( msg );
        }
    }

    // Entity object instantiation

    protected <T, U> T rolePlayer( Class<T> roleClass, Class<U> dataClass, String entityId )
    {
        U dataObject = uowf.currentUnitOfWork().get( dataClass, entityId );
        return rolePlayer( roleClass, dataObject );
    }

    protected static <T> T loadEntity( Class<T> roleClass, String entityId )
    {
        return uowf.currentUnitOfWork().get( roleClass, entityId );
    }

    public static void prepareContextBaseClass( UnitOfWorkFactory unitOfWorkFactory )
    {
        uowf = unitOfWorkFactory;
    }
}