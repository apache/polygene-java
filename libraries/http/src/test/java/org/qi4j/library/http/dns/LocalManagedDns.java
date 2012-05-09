/*
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.http.dns;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.xbill.DNS.spi.DNSJavaNameServiceDescriptor;
import sun.net.spi.nameservice.NameService;

public class LocalManagedDns
        implements NameService
{

    private static final NameService DEFAULT_DNS = new DNSJavaNameServiceDescriptor().createNameService();
    private static final Map<String, String> NAMES = new ConcurrentHashMap<String, String>();

    public static void clearNames()
    {
        NAMES.clear();
    }

    public static String putName( String name, String ip )
    {
        return NAMES.put( name, ip );
    }

    public static String removeName( String name )
    {
        return NAMES.remove( name );
    }

    public String getHostByAddr( byte[] bytes )
            throws UnknownHostException
    {
        return DEFAULT_DNS.getHostByAddr( bytes );
    }

    public InetAddress[] lookupAllHostAddr( String name )
            throws UnknownHostException
    {
        String log = "[CDNS] Lookup request";
        String ip = NAMES.get( name );
        InetAddress[] result = new InetAddress[ 0 ];
        if ( ip != null && ip.length() > 0 ) {
            log += " on managed name (" + name + ")";
            byte[] ipBytes = ipStringToBytes( ip );
            result = new InetAddress[]{ Inet4Address.getByAddress( ipBytes ) };
        } else {
            log += " on non-managed name (" + name + ")";
            result = DEFAULT_DNS.lookupAllHostAddr( name );
        }
        System.out.println( log + " will return: " + Arrays.toString( result ) );
        return result;
    }

    private static byte[] ipStringToBytes( String ipString )
    {
        if ( ipString == null || ipString.length() == 0 ) {
            return null;
        }

        int inetAddrSize = 4;
        int octets;
        char ch;
        byte[] dst = new byte[ inetAddrSize ];
        char[] srcb = ipString.toCharArray();
        boolean saw_digit = false;

        octets = 0;
        int i = 0;
        int cur = 0;
        while ( i < srcb.length ) {
            ch = srcb[i++];
            if ( Character.isDigit( ch ) ) {
                int sum = dst[cur] * 10 + ( Character.digit( ch, 10 ) & 0xff );

                if ( sum > 255 ) {
                    return null;
                }

                dst[cur] = ( byte ) ( sum & 0xff );
                if ( !saw_digit ) {
                    if ( ++octets > inetAddrSize ) {
                        return null;
                    }
                    saw_digit = true;
                }
            } else if ( ch == '.' && saw_digit ) {
                if ( octets == inetAddrSize ) {
                    return null;
                }
                cur++;
                dst[cur] = 0;
                saw_digit = false;
            } else {
                return null;
            }
        }

        if ( octets < inetAddrSize ) {
            return null;
        }
        return dst;
    }

}
