package com.marcgrue.dcisample_b.communication.web.tracking;

import com.marcgrue.dcisample_b.communication.query.CommonQueries;
import com.marcgrue.dcisample_b.communication.query.TrackingQueries;
import com.marcgrue.dcisample_b.communication.query.dto.CargoDTO;
import com.marcgrue.dcisample_b.communication.web.BasePage;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType;
import com.marcgrue.dcisample_b.infrastructure.wicket.form.AbstractForm;
import com.marcgrue.dcisample_b.infrastructure.wicket.form.SelectorInForm;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.value.ValueMap;
import org.qi4j.api.unitofwork.NoSuchEntityException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Track a cargo
 *
 * For convenience during testing, routed (non-received) cargos can be chosen from the dropdown list.
 */
public class TrackCargoPage extends BasePage
{
    public TrackCargoPage()
    {
        super( "tracking" ); // Selects the Tracking tab
        add( new TrackingForm() );
    }

    private final class TrackingForm extends AbstractForm<Void>
    {
        private String trackingId;
        private String selectedTrackingId; // Set by Wicket property resolver

        private TextField<String> trackingIdInput;
        private SelectorInForm selectedTrackingIdSelector;

        private FeedbackPanel feedback = new FeedbackPanel( "feedback" );
        private Fragment statusFragment = new Fragment( "status", "statusFragment", new WebMarkupContainer( "mock" ) );

        private TrackingForm()
        {
            // Manual input
            trackingIdInput = new TextField<String>( "trackingId", new PropertyModel<String>( this, "trackingId" ) );
            add( trackingIdInput.setRequired( true ).setOutputMarkupId( true ) );

            // Submit button
            add( new AjaxFallbackButton( "track", this )
            {
                @Override
                protected void onSubmit( AjaxRequestTarget target, Form<?> form )
                {
                    updateStatusFragment( target );
                }

                @Override
                protected void onError( final AjaxRequestTarget target, Form<?> form )
                {
                    updateStatusFragment( target );
                    target.add( feedback, trackingIdInput, selectedTrackingIdSelector, statusFragment.setVisible( false ) );
                }
            } );

            // Drop down selector (for convenience)
            List<String> cargoIds = new TrackingQueries().routedCargos();
            add( selectedTrackingIdSelector = new SelectorInForm(
                  "selectedTrackingId", "Selected Tracking id", cargoIds, this ) );
            selectedTrackingIdSelector.add( new AjaxFormComponentUpdatingBehavior( "onchange" )
            {
                @Override
                protected void onUpdate( AjaxRequestTarget target )
                {
                    trackingId = selectedTrackingId;
                    updateStatusFragment( target );
                }
            } );

            add( feedback.setOutputMarkupId( true ) );
            add( statusFragment.setOutputMarkupId( true ).setOutputMarkupPlaceholderTag( true ).setVisible( false ) );
        }

        private void updateStatusFragment( final AjaxRequestTarget target )
        {
            try
            {
                IModel<CargoDTO> cargoModel = new CommonQueries().cargo( trackingId );
                statusFragment = (Fragment) statusFragment.replaceWith( new StatusFragment( cargoModel, false ) );
                target.add( feedback, trackingIdInput, selectedTrackingIdSelector, statusFragment.setVisible( true ) );
            }
            catch (NoSuchEntityException e)
            {
                e.printStackTrace();
                error( "Cargo '" + trackingId + "' wasn't found in the system. Please check the tracking number." );

                target.add( feedback, trackingIdInput, selectedTrackingIdSelector, statusFragment.setVisible( false ) );
            }
            catch (Exception e)
            {
                e.printStackTrace();
                error( "Problem retrieving status for cargo '" + trackingId + "': " + e.getMessage() );
                target.add( feedback, trackingIdInput, selectedTrackingIdSelector, statusFragment.setVisible( false ) );
            }
        }

        private class StatusFragment extends Fragment
        {
            public StatusFragment( IModel<CargoDTO> cargoModel, Boolean visible )
            {
                super( "status", "statusFragment", TrackingForm.this );
                setVisible( visible );

                CargoDTO cargo = cargoModel.getObject();

                // Status ----------------------------------------------------------------------
                ValueMap map = new ValueMap();
                map.put( "status", cargo.delivery().get().transportStatus().get().name() );
                map.put( "trackingId", trackingId );
                HandlingEvent lastEvent = cargo.delivery().get().lastHandlingEvent().get();
                if (lastEvent != null)
                {
                    String voyageString = lastEvent.voyage().get() != null ?
                          lastEvent.voyage().get().voyageNumber().get().number().get() : "UNKNOWN_VOYAGE";
                    map.put( "voyage", voyageString );
                    map.put( "location", lastEvent.location().get().getString() );
                }
                else
                {
                    map.put( "voyage", "UNKNOWN_VOYAGE" );
                    map.put( "location", cargo.origin().get().getString() );
                }
                add( new Label( "transportStatus", new StringResourceModel(
                      "transportStatus.${status}", this, new Model<ValueMap>( map ) ) ) );


                // ETA ----------------------------------------------------------------------
                String destination = cargo.routeSpecification().get().destination().get().getString();
                Date eta = cargo.delivery().get().eta().get();
                String etaString = eta == null ? "?" : new SimpleDateFormat( "yyyy-MM-dd" ).format( eta );
                add( new Label( "eta", new StringResourceModel(
                      "eta", this, null, Model.of( destination ), Model.of( etaString ) ) ) );


                // Warning/Notifier ----------------------------------------------------------------------
                add( new WebMarkupContainer( "isMisdirected" ).setVisible( cargo.delivery().get().isMisdirected().get() ) );
                add( new WebMarkupContainer( "isClaimed" ).setVisible(
                      !cargo.delivery().get().isMisdirected().get()
                            && cargo.delivery().get().isUnloadedAtDestination().get()
                            && lastEvent != null
                            && lastEvent.handlingEventType().get() == HandlingEventType.CLAIM
                ) );


                // Handling history ----------------------------------------------------------------------
                if (cargo.delivery().get().lastHandlingEvent().get() == null)
                    add( new Label( "handlingHistoryPanel" ) );
                else
                    add( new HandlingHistoryPanel( "handlingHistoryPanel", cargoModel, trackingId ) );
            }
        }
    }
}