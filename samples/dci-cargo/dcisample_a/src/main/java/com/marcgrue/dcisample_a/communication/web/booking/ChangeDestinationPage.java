package com.marcgrue.dcisample_a.communication.web.booking;

import com.marcgrue.dcisample_a.communication.query.CommonQueries;
import com.marcgrue.dcisample_a.communication.query.dto.CargoDTO;
import com.marcgrue.dcisample_a.context.shipping.booking.BookNewCargo;
import com.marcgrue.dcisample_a.infrastructure.wicket.form.AbstractForm;
import com.marcgrue.dcisample_a.infrastructure.wicket.form.SelectorInForm;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.List;

/**
 * Change destination of Cargo
 */
public class ChangeDestinationPage extends BookingBasePage
{
    public ChangeDestinationPage( PageParameters parameters )
    {
        String trackingId = parameters.get( 0 ).toString();
        add( new Label( "trackingId", trackingId ) );
        add( new CargoEditForm( trackingId ) );
    }

    private class CargoEditForm extends AbstractForm<Void>
    {
        private String trackingId; // Set by Wicket property resolver
        private String origin, destination, oldDestination;

        public CargoEditForm( final String trackingId )
        {
            CommonQueries fetch = new CommonQueries();
            CargoDTO cargo = fetch.cargo( trackingId ).getObject();
            List<String> locations = fetch.unLocodes();

            origin = cargo.origin().get().getCode();
            oldDestination = destination = cargo.routeSpecification().get().destination().get().getCode();

            final FeedbackPanel feedback = new FeedbackPanel( "usecaseFeedback" );
            final SelectorInForm destinationSelector = new SelectorInForm(
                  "destination", "Destination", locations, this, "origin" );
            final ComponentFeedbackPanel destinationFeedback = new ComponentFeedbackPanel(
                  "destinationFeedback", destinationSelector.setRequired( true ) );

            add( feedback.setOutputMarkupId( true ) );
            add( new Label( "origin", origin ) );
            add( new Label( "destination", destination ) );
            add( destinationFeedback.setOutputMarkupId( true ) );
            add( destinationSelector );
            add( new AjaxFallbackButton( "submit", this )
            {
                @Override
                protected void onSubmit( AjaxRequestTarget target, Form<?> form )
                {
                    try
                    {
                        if (oldDestination.equals( destination ))
                            throw new IllegalArgumentException( "Please select a new destination." );

                        // Perform use case
                        new BookNewCargo( trackingId ).changeDestination( destination );

                        // Show updated cargo
                        setResponsePage( CargoDetailsPage.class, new PageParameters().set( 0, trackingId ) );
                    }
                    catch (Exception e)
                    {
                        logger.warn( "Problem changing destination of cargo " + trackingId + ": " +e.getMessage() );
                        feedback.error( e.getMessage() );
                        target.add( feedback );
                    }
                }

                @Override
                protected void onError( final AjaxRequestTarget target, Form<?> form )
                {
                    target.add( destinationFeedback );
                    target.focusComponent( destinationSelector );
                }
            } );
        }
    }
}