package com.marcgrue.dcisample_b.communication.web.handling;

import com.marcgrue.dcisample_b.communication.query.CommonQueries;
import com.marcgrue.dcisample_b.communication.query.HandlingQueries;
import com.marcgrue.dcisample_b.communication.web.BasePage;
import com.marcgrue.dcisample_b.context.interaction.handling.ProcessHandlingEvent;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType;
import com.marcgrue.dcisample_b.infrastructure.wicket.form.AbstractForm;
import com.marcgrue.dcisample_b.infrastructure.wicket.form.DateTextFieldWithPicker;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.value.ValueMap;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Incident Logging Application mockup page
 *
 * This is a mockup of an Incident Logging Application interface that external handling
 * authorities would use to send us handling event data for cargos that they have handled.
 *
 * For simplicity we don't create a separate Incident Logging Application for now but instead
 * let it reside inside our booking application. We act as a handling authority entering handling
 * event data, and in the lower half of the page we mock the receipt of the data and show the
 * validation results from our own validation of the incoming data.
 *
 * We could also instead (in future versions?) implement a web service endpoint receiving
 * asynchronous messages from a separate incident logging application. We have prepared for
 * this by separating the processing in three steps, with ProcessHandlingEvent coordinating
 * the following steps:
 *
 * {@link ProcessHandlingEvent}
 * 1. parsedHandlingEventData = parse( completion, trackingId, eventType, unLocode, voyage )
 * 2. handlingEvent = register( parsedHandlingEventData )
 * 3. inspect( handlingEvent )
 *
 * The last step updates the delivery status of the cargo.
 *
 * On this page we act as a handling authority sending handling event data to our booking
 * application. We perform basic validation and parsing of incoming data, and in case of a
 * valid registration attempt, synchronously send the data to the booking application for
 * processing there.
 */
public class IncidentLoggingApplicationMockupPage extends BasePage
{
    public IncidentLoggingApplicationMockupPage()
    {
        super( "handling" ); // Selects the Handling tab
        add( new ReportHandlingEventForm() );
    }

    private final class ReportHandlingEventForm extends AbstractForm<Void>
    {
        FeedbackPanel feedback;

        // Form values
        Date completion;
        String trackingId, unLocode, voyageNumber, eventType;

        // Input
        TextField<String> trackingIdInput, eventTypeInput, voyageInput, locationInput;
        String trackingIdSelected, eventTypeSelected, voyageSelected, locationSelected;
        DropDownChoice<String> trackingIdSelector, eventTypeSelector, voyageSelector, locationSelector;

        // To avoid re-submitting same data
        String lastSubmittedData;

        public ReportHandlingEventForm()
        {
            final FeedbackPanel feedback = new FeedbackPanel( "feedback" );
            add( feedback.setOutputMarkupId( true ) );

            // Completion time

            final DateTextFieldWithPicker completionDateInput = new DateTextFieldWithPicker( "completion", "Completion", this );
            completionDateInput.earliestDate( new LocalDate() );
            add( completionDateInput.setLabel( Model.of( "Completion" ) ) );

            HandlingQueries fetch = new HandlingQueries();


            // Tracking id

            trackingIdInput = new TextField<String>( "trackingIdInput", new PropertyModel<String>( this, "trackingId" ) );
            add( trackingIdInput.setRequired( true ).setLabel( Model.of( "Cargo" ) ).setOutputMarkupId( true ) );

            trackingIdSelector = new DropDownChoice<String>( "trackingIdSelector",
                                                             new PropertyModel<String>( this, "trackingIdSelected" ),
                                                             fetch.cargoIds() );
            trackingIdSelector.add( new AjaxFormComponentUpdatingBehavior( "onchange" )
            {
                @Override
                protected void onUpdate( AjaxRequestTarget target )
                {
                    trackingId = trackingIdSelected;
                    target.add( feedback, trackingIdInput, trackingIdSelector );
                }
            } );
            add( trackingIdSelector.setOutputMarkupId( true ) );


            // Event Type

            eventTypeInput = new TextField<String>( "eventTypeInput", new PropertyModel<String>( this, "eventType" ) );
            add( eventTypeInput.setRequired( true ).setLabel( Model.of( "Event Type" ) ).setOutputMarkupId( true ) );

            eventTypeSelector = new DropDownChoice<String>( "eventTypeSelector",
                                                            new PropertyModel<String>( this, "eventTypeSelected" ),
                                                            fetch.eventTypes() );
            eventTypeSelector.add( new AjaxFormComponentUpdatingBehavior( "onchange" )
            {
                @Override
                protected void onUpdate( AjaxRequestTarget target )
                {
                    eventType = eventTypeSelected;
                    target.add( feedback, eventTypeInput, eventTypeSelector );
                }
            } );
            add( eventTypeSelector.setOutputMarkupId( true ) );


            // Voyage (optional in some cases)

            voyageInput = new TextField<String>( "voyageInput", new PropertyModel<String>( this, "voyageNumber" ) );
            add( voyageInput.setLabel( Model.of( "Voyage" ) ).setOutputMarkupId( true ) );

            voyageSelector = new DropDownChoice<String>( "voyageSelector",
                                                         new PropertyModel<String>( this, "voyageSelected" ),
                                                         fetch.voyages() );
            voyageSelector.add( new AjaxFormComponentUpdatingBehavior( "onchange" )
            {
                @Override
                protected void onUpdate( AjaxRequestTarget target )
                {
                    voyageNumber = voyageSelected;
                    target.add( feedback, voyageInput, voyageSelector );
                }
            } );
            add( voyageSelector.setOutputMarkupId( true ) );


            // Location

            locationInput = new TextField<String>( "locationInput", new PropertyModel<String>( this, "unLocode" ) );
            add( locationInput.setRequired( true ).setLabel( Model.of( "Location" ) ).setOutputMarkupId( true ) );

            locationSelector = new DropDownChoice<String>( "locationSelector",
                                                           new PropertyModel<String>( this, "locationSelected" ),
                                                           new CommonQueries().unLocodes() );
            locationSelector.add( new AjaxFormComponentUpdatingBehavior( "onchange" )
            {
                @Override
                protected void onUpdate( AjaxRequestTarget target )
                {
                    unLocode = locationSelected;
                    target.add( feedback, locationInput, locationSelector );
                }
            } );
            add( locationSelector.setOutputMarkupId( true ) );


            // Submit and process

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

                        // We simulate receiving raw text data from incident logging applications
                        // Add current time to date to have same-dates in processing order (would register full time in real app)
                        Date adjustedCompletion = new Date( completion.getTime() + new DateTime().getMillisOfDay() );
                        String completionTimeString = new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).format( adjustedCompletion );

                        // Parse "incoming" data (step 1 of ProcessHandlingEvent use case)
                        tbf.newTransient( ProcessHandlingEvent.class ).parse(
                              completionTimeString, trackingId, eventType, unLocode, voyageNumber );

                        /**
                         * We could redirect to Details, but it's more fun to update details in a separate
                         * window to follow the successive handling event registrations you make...
                         * */
//                        setResponsePage( CargoDetailsPage.class, new PageParameters().set( 0, trackingId ) );

                        try
                        {
                            HandlingEventType.valueOf( eventType );
                        }
                        catch (Exception e)
                        {
                            throw new Exception( "'" + eventType + "' is not a valid handling event type" );
                        }

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
                        logger.warn( "Problem registering handling event: " + e.getMessage() );
                        feedback.error( e.getMessage() );
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