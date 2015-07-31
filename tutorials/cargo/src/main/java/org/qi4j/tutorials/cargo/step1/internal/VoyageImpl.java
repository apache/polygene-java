/*
 * Copyright 2007, 2008 Niclas Hedhman.
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
package org.qi4j.tutorials.cargo.step1.internal;

import java.util.HashMap;
import org.qi4j.tutorials.cargo.step1.Cargo;
import org.qi4j.tutorials.cargo.step1.Voyage;

public class VoyageImpl
    implements Voyage
{
    private double capacity;
    private HashMap<Integer, Cargo> bookedCargo;

    public VoyageImpl( int capacity )
    {
        this.capacity = capacity;
        bookedCargo = new HashMap<Integer, Cargo>();
    }

    @Override
    public double getCapacity()
    {
        return capacity;
    }

    @Override
    public double getBookedCargoSize()
    {
        double bookedCargoSize = 0;
        for( Cargo cargo : bookedCargo.values() )
        {
            bookedCargoSize = bookedCargoSize + cargo.getSize();
        }
        return bookedCargoSize;
    }

    public void addCargo( Cargo cargo, int confirmation )
    {
        bookedCargo.put( confirmation, cargo );
    }

    public Cargo removeCargo( int confirmation )
    {
        return bookedCargo.remove( confirmation );
    }
}
