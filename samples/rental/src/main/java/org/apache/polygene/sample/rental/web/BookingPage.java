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

package org.apache.polygene.sample.rental.web;

import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.sample.rental.domain.Booking;
import org.apache.polygene.sample.rental.domain.Car;
import org.apache.polygene.sample.rental.domain.Customer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Mixins( BookingPage.BodyContributorMixin.class )
public interface BookingPage
    extends Page
{
    Node car( QuikitContext context );

    Node customer( QuikitContext context );

    abstract class BodyContributorMixin
        implements BookingPage
    {
        @Structure
        private UnitOfWorkFactory uowf;

        public Node car( QuikitContext context )
        {
            Document dom = context.dom();
            Element result = dom.createElementNS( Page.XHTML, "div" );
            String bookingId = context.path();
            UnitOfWork uow = uowf.currentUnitOfWork();
            Booking booking = uow.get( Booking.class, StringIdentity.fromString( bookingId ) );
            Car car = booking.car().get();
            createChildNode( dom, result, car.model().get() );
            createChildNode( dom, result, car.licensePlate().get() );
            createChildNode( dom, result, car.category().get().name().get() );
            return result;
        }

        private void createChildNode( Document dom, Element result, String content )
        {
            Element modelElement = dom.createElement( "div" );
            result.appendChild( modelElement );
            modelElement.setTextContent( content );
        }

        public Node customer( QuikitContext context )
        {
            Document dom = context.dom();
            Element result = dom.createElementNS( Page.XHTML, "div" );
            String bookingId = context.path();
            UnitOfWork uow = uowf.currentUnitOfWork();
            Booking booking = uow.get( Booking.class, StringIdentity.fromString( bookingId ) );
            Customer customer = booking.customer().get();
            createChildNode( dom, result, customer.name().get() );
            createChildNode( dom, result, customer.address().get().line1().get() );
            createChildNode( dom, result, customer.address().get().line2().get() );
            createChildNode( dom, result, customer.address().get().zipCode().get() + " " + customer.address()
                .get()
                .city()
                .get() );
            createChildNode( dom, result, customer.address().get().country().get() );
            return result;
        }
    }
}
