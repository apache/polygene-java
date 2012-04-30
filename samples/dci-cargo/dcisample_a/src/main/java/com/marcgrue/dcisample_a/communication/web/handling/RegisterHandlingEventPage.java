package com.marcgrue.dcisample_a.communication.web.handling;

import com.marcgrue.dcisample_a.communication.query.CommonQueries;
import com.marcgrue.dcisample_a.communication.query.HandlingQueries;
import com.marcgrue.dcisample_a.communication.web.BasePage;
import com.marcgrue.dcisample_a.context.shipping.handling.RegisterHandlingEvent;
import com.marcgrue.dcisample_a.infrastructure.wicket.form.AbstractForm;
import com.marcgrue.dcisample_a.infrastructure.wicket.form.DateTextFieldWithPicker;
import com.marcgrue.dcisample_a.infrastructure.wicket.form.SelectorInForm;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.value.ValueMap;
import org.joda.time.LocalDate;

import java.util.Date;

/**
 * Register Handling Events
 *
 * Interface used by various authorities to register handling events for cargos. This would be a
 * separate interface not coupled to the booking system, but for testing convenience it is integrated
 * with Booking and Tracking.
 *
 * Tip: try open the Event Registration window side-by-side to a Cargo details window for a specific cargo.
 * Then register events for that cargo and here and update the details window to see the progress. You can
 * also then in the details window see what event is expected next...
 */
public class RegisterHandlingEventPage extends BasePage
{
    public RegisterHandlingEventPage()
    {
        super( "handling" ); // Selects the Event Registration tab
        add( new RegisterHandlingEventForm() );
    }

    private final class RegisterHandlingEventForm extends AbstractForm<Void>
    {
        // Set by Wicket property resolvers:
        private Date completion;
        private String trackingId, unLocode, voyageNumber, eventType;

        private String lastSubmittedData;

        public RegisterHandlingEventForm()
        {
            final FeedbackPanel feedback = new FeedbackPanel( "feedback" );
            add( feedback.setOutputMarkupId( true ) );

            final DateTextFieldWithPicker completionDateInput = new DateTextFieldWithPicker(  "completion", "Completion", this );
            completionDateInput.earliestDate( new LocalDate() );

            HandlingQueries fetch = new HandlingQueries();
            add( completionDateInput.setLabel( Model.of( "Completion" ) ) );
            add( new SelectorInForm( "trackingId", "Tracking Id", fetch.cargoIds(), this ).setRequired( true ) );
            add( new SelectorInForm( "eventType","Event Type", fetch.eventTypes(), this ).setRequired( true ));
            add( new SelectorInForm( "unLocode", "Location", new CommonQueries().unLocodes(), this ).setRequired( true ) );
            add( new SelectorInForm( "voyageNumber", "Voyage number", fetch.voyages(), this ) );

            add( new AjaxFallbackButton( "register", this )
            {
                @Override
                protected void onSubmit( AjaxRequestTarget target, Form<?> form )
                {
                    try
                    {
                        // We want to allow making multiple _unique_ handling event registrations
                        if (sameDataIsSubmitted())
                            throw new Exception( "Can't re-submit the same data." );

                        // Perform use case
                        new RegisterHandlingEvent(
                              new Date(), completion, trackingId, eventType, unLocode, voyageNumber ).register();

                        // We could redirect to Details, but it's more fun to update details in a separate
                        // window to follow the successive handling event registrations you make...
//                        setResponsePage( CargoDetails.class, new PageParameters().set( 0, trackingId ) );

                        ValueMap map = new ValueMap();
                        map.put( "type", eventType );
                        map.put( "location", unLocode );
                        if (voyageNumber != null) map.put( "voyage", voyageNumber );
                        String msg = new StringResourceModel( "handlingEvent.${type}", this, new Model<ValueMap>( map ) ).getObject();

                        feedback.info( "Registered handling event for cargo '" + trackingId + "': " + msg );
                        target.add( feedback );
                    }
                    catch (Exception e)
                    {
                        logger.warn( "Problem registering handling event: " +  e.getMessage() );
                        feedback.error(  e.getMessage() );
                        target.add( feedback );
                    }
                }

                @Override
                protected void onError( final AjaxRequestTarget target, Form<?> form )
                {
                    target.add( feedback );
                    focusFirstError( target );
                }
            } );
        }

        private boolean sameDataIsSubmitted()
        {
            String submittedData = completion.toString() + trackingId + unLocode + voyageNumber + eventType;

            if (submittedData.equals( lastSubmittedData ))
                return true;

            // Valid new data submitted
            lastSubmittedData = submittedData;

            return false;
        }
    }
}