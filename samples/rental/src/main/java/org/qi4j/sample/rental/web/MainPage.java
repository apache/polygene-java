/*
 * Copyright 2009 Niclas Hedhman.
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

package org.qi4j.sample.rental.web;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.sample.rental.domain.Booking;
import org.qi4j.sample.rental.domain.Period;
import org.qi4j.sample.rental.domain.RentalShop;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Mixins( { MainPage.BodyContributorMixin.class } )
public interface MainPage
    extends Page
{
    Node[] bookings( QuikitContext context );

    abstract class BodyContributorMixin
        implements MainPage
    {
        private SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );

        @Service
        BookingPage bookingPage;
        @Service
        UrlService urlService;
        @Structure
        private UnitOfWorkFactory uowf;

        public Node[] bookings( QuikitContext context )
        {
            ArrayList<Node> nodes = new ArrayList<Node>();
            Document dom = context.dom();
            UnitOfWork uow = uowf.currentUnitOfWork();
            RentalShop shop = uow.get( RentalShop.class, "SHOP" );
            for( Booking booking : shop.findAllBookings() )
            {
                String plate = booking.car().get().licensePlate().get();

                // TODO: Next version;
//                Node node = create( dom, "<li><a href='{1}'>{2}</a>{3}</li>", createLink( booking ), plate, createPeriod( booking.period().get() ) );
//                nodes.add( node );

                Element listItem = createElement( dom, "li" );
                Element bookingLink = (Element) listItem.appendChild( createElement( dom, "a" ) );
                bookingLink.setAttribute( "href", createLink( booking ) );
                bookingLink.setTextContent( plate );
                listItem.appendChild( dom.createTextNode( createPeriod( booking.period().get() ) ) );
                nodes.add( listItem );
            }
            Node[] bookingList = new Node[ nodes.size() ];
            nodes.toArray( bookingList );
            return bookingList;
        }

        private Node create( Document dom, String pattern, Object... args )
        {
            String result = MessageFormat.format( pattern, args );
            // TODO: fix up ANTLR to parse XML and generate the Node.
            return null;
        }

        private String createPeriod( Period period )
        {

            return " / " +
                   sdf.format( period.startOfPeriod().get() ) +
                   " - " +
                   sdf.format( period.endOfPeriod().get() );
        }

        private Element createElement( Document dom, String element )
        {
            return dom.createElementNS( Page.XHTML, element );
        }

        private String createLink( Booking booking )
        {
            String pageUrl = urlService.createLink( bookingPage );
            String entityId = booking.identity().get();
            return pageUrl + "/" + entityId;
        }
    }
}
