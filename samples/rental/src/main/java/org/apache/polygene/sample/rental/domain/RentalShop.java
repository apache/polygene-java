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

package org.apache.polygene.sample.rental.domain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.query.QueryBuilderFactory;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueBuilderFactory;

@Mixins( RentalShop.RentalShopMixin.class )
public interface RentalShop
    extends EntityComposite
{
    Customer createCustomer( String name, String address1, String address2, String zip, String city, String country );

    Car findAvailableCarByModel( String model );

    Car findAvailableCarByCategory( CarCategory category );

    List<CarCategory> findAllCarCategories();

    List<Booking> findAllBookings();

    Set<String> findAllCarModels();

    Booking book( Customer customer, Car car, Period plannedPeriod );

    void pickup( Booking booking, Instant time );

    void returned( Booking booking, Instant time );

    void boughtCar( Car car, LocalDate purchasedate );

    void soldCar( Car car, LocalDate soldDate );

    Car createCar( String category, String modelName, String licensePlate );

    List<CarCategory> carCategories();

    interface State
    {
        ManyAssociation<Car> carsOwned();

        ManyAssociation<Car> carsAvailable();

        ManyAssociation<Booking> bookings();

        ManyAssociation<CarCategory> carCategories();
    }

    abstract class RentalShopMixin
        implements RentalShop
    {
        @Structure
        ValueBuilderFactory vbf;
        @Structure
        QueryBuilderFactory qbf;
        @Structure
        UnitOfWorkFactory uowf;
        @This
        private State state;

        public Customer createCustomer( String name,
                                        String address1,
                                        String address2,
                                        String zip,
                                        String city,
                                        String country
        )
        {
            ValueBuilder<Address> addrBuilder = vbf.newValueBuilder( Address.class );
            Address protoAddress = addrBuilder.prototype();
            protoAddress.line1().set( address1 );
            protoAddress.line2().set( address2 );
            protoAddress.zipCode().set( zip );
            protoAddress.city().set( city );
            protoAddress.country().set( country );
            Address address = addrBuilder.newInstance();
            EntityBuilder<Customer> builder = uowf.currentUnitOfWork().newEntityBuilder( Customer.class );
            builder.instance().name().set( name );
            builder.instance().address().set( address );
            return builder.newInstance();
        }

        public Car findAvailableCarByModel( String model )
        {
            for( Car car : state.carsAvailable().toList() )
            {
                if( car.model().get().equals( model ) )
                {
                    return car;
                }
            }
            return null;
        }

        public Car findAvailableCarByCategory( CarCategory category )
        {
            for( Car car : state.carsAvailable().toList() )
            {
                if( car.category().get().equals( category ) )
                {
                    return car;
                }
            }
            return null;
        }

        public List<CarCategory> findAllCarCategories()
        {
            return state.carCategories().toList();
        }

        public Set<String> findAllCarModels()
        {
            return state.carsOwned()
                .toList()
                .stream()
                .map( car -> car.model().get() )
                .collect( Collectors.toSet() );
        }

        public Booking book( Customer customer, Car car, Period plannedPeriod )
        {
            EntityBuilder<Booking> builder = uowf.currentUnitOfWork().newEntityBuilder( Booking.class );
            Booking instance = builder.instance();
            String refNo;
            try
            {
                MessageDigest md;
                md = MessageDigest.getInstance( "MD5" );
                md.update( instance.identity().get().toBytes() );
                StringBuilder buf = new StringBuilder();
                byte[] data = md.digest();
                for( int i = 0; i < 4; i++ )
                {
                    String hex = Integer.toHexString( Math.abs( data[ i ] ) );
                    if( hex.length() == 1 )
                    {
                        buf.append( "0" );
                    }
                    buf.append( hex );
                    if( i == 1 )
                    {
                        buf.append( "-" );
                    }
                }
                refNo = buf.toString().toUpperCase();
            }
            catch( NoSuchAlgorithmException e )
            {
                // Can not happen.
                throw new RuntimeException( e );
            }
            instance.reference().set( refNo );
            instance.car().set( car );
            instance.customer().set( customer );
            instance.period().set( plannedPeriod );
            Booking booking = builder.newInstance();
            state.bookings().add( booking );
            return booking;
        }

        public void pickup( Booking booking, Instant time )
        {
            booking.pickedupTime().set( time );
        }

        public void returned( Booking booking, Instant time )
        {
            booking.returnedTime().set( time );
        }

        public void boughtCar( Car car, LocalDate purchaseDate )
        {
            state.carsOwned().add( car );
            car.purchasedDate().set( purchaseDate );
        }

        public void soldCar( Car car, LocalDate soldDate )
        {
            state.carsOwned().remove( car );
            car.soldDate().set( soldDate );
        }

        public List<Booking> findAllBookings()
        {
            ManyAssociation<Booking> manyAssociation = state.bookings();
            return manyAssociation.toList();
        }

        public List<CarCategory> carCategories()
        {
            return state.carCategories().toList();
        }

        public Car createCar( String category, String modelName, String licensePlate )
        {
            CarCategory carCategory = findCarCategory( category );
            EntityBuilder<Car> builder = uowf.currentUnitOfWork().newEntityBuilder( Car.class );
            builder.instance().model().set( modelName );
            builder.instance().category().set( carCategory );
            builder.instance().licensePlate().set( licensePlate );
            return builder.newInstance();
        }

        private CarCategory findCarCategory( String categoryName )
        {
            for( CarCategory carCategory : state.carCategories() )
            {
                if( carCategory.name().get().equals( categoryName ) )
                {
                    return carCategory;
                }
            }
            EntityBuilder<CarCategory> categoryBuilder = uowf.currentUnitOfWork().newEntityBuilder( CarCategory.class );
            categoryBuilder.instance().name().set( categoryName );
            return categoryBuilder.newInstance();
        }
    }
}
