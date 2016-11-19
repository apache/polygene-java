/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.test.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.IntPredicate;
import java.util.stream.Stream;

import static java.util.Collections.shuffle;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;

public class FreePortFinder
{
    private static class Range
    {
        final int lowerBound;
        final int higherBound;

        private Range( int lowerBound, int higherBound )
        {
            this.lowerBound = lowerBound;
            this.higherBound = higherBound;
        }
    }

    // Presumably the least used port ranges with >500 slots
    // See http://stackoverflow.com/a/28369841/300053
    private static final List<Range> LEAST_USED_RANGES = Arrays.asList(
        new Range( 29170, 29998 ),
        new Range( 38866, 39680 ),
        new Range( 41798, 42507 ),
        new Range( 43442, 44122 ),
        new Range( 46337, 46997 ),
        new Range( 35358, 36000 ),
        new Range( 36866, 37474 ),
        new Range( 38204, 38799 ),
        new Range( 33657, 34248 ),
        new Range( 30261, 30831 ),
        new Range( 41231, 41793 ),
        new Range( 21011, 21552 ),
        new Range( 28590, 29117 ),
        new Range( 14415, 14935 ),
        new Range( 26490, 26999 )
    );

    private static final int MAX_PORT_CHECKS = 20;

    public static boolean isFreePortOnLocalHost( int port )
    {
        return isFreePort( getLocalHostUnchecked(), port );
    }

    public static boolean isFreePort( InetAddress address, int port )
    {
        try
        {
            checkFreePort( address, port );
            return true;
        }
        catch( IOException ex )
        {
            return false;
        }
    }

    public static void checkFreePortOnLocalHost( int port ) throws IOException
    {
        checkFreePort( getLocalHostUnchecked(), port );
    }

    public static void checkFreePort( InetAddress address, int port ) throws IOException
    {
        ServerSocket server = null;
        try
        {
            server = new ServerSocket( port, 1, address );
        }
        finally
        {
            if( server != null )
            {
                try
                {
                    server.close();
                }
                catch( IOException e )
                {
                    // Ignore
                }
            }
        }
    }

    public static int findFreePortOnLocalHost()
        throws IOException
    {
        return findFreePort( getLocalHostUnchecked() );
    }

    public static int findFreePort( InetAddress address )
        throws IOException
    {
        FreePortPredicate check = new FreePortPredicate( address );
        // Randomly choose MAX_PORT_CHECKS ports from the least used ranges
        Range range = LEAST_USED_RANGES.get( new Random().nextInt( LEAST_USED_RANGES.size() ) );
        OptionalInt port = rangeClosed( range.lowerBound, range.higherBound )
            .boxed()
            .collect( collectingAndThen( toList(), collected ->
            {
              shuffle( collected );
              return collected.stream();
            } ) )
            .limit( MAX_PORT_CHECKS )
            .mapToInt( Integer::intValue )
            .filter( check )
            .findFirst();
        return port.orElseThrow( () ->
        {
            IOException exception = new IOException( "Unable to find a free port on " + address );
            check.errors.build().forEach( exception::addSuppressed );
            return exception;
        } );
    }

    public static int findFreePortInRangeOnLocalhost( int lowerBound, int higherBound )
        throws IOException
    {
        return findFreePortInRange( getLocalHostUnchecked(), lowerBound, higherBound );
    }

    public static int findFreePortInRange( InetAddress address, int lowerBound, int higherBound )
        throws IOException
    {
        if( higherBound - lowerBound < 0 )
        {
            throw new IllegalArgumentException( "Invalid port range " + lowerBound + '-' + higherBound );
        }
        FreePortPredicate check = new FreePortPredicate( address );
        OptionalInt port = rangeClosed( lowerBound, higherBound ).filter( check ).findFirst();
        return port.orElseThrow( () ->
        {
            String message = "Unable to find a free port in range " + lowerBound + '-' + higherBound + " on " + address;
            IOException exception = new IOException( message );
            check.errors.build().forEach( exception::addSuppressed );
            return exception;
        } );
    }

    private static class FreePortPredicate implements IntPredicate
    {
        private final InetAddress address;
        private final Stream.Builder<Throwable> errors;

        private FreePortPredicate( InetAddress address )
        {
            this.address = address;
            this.errors = Stream.builder();
        }

        @Override
        public boolean test( int candidate )
        {
            try
            {
                checkFreePort( address, candidate );
                return true;
            }
            catch( IOException ex )
            {
                errors.add( ex );
                return false;
            }
        }
    }

    private static InetAddress getLocalHostUnchecked()
    {
        try
        {
            return InetAddress.getLocalHost();
        }
        catch( UnknownHostException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }
}
