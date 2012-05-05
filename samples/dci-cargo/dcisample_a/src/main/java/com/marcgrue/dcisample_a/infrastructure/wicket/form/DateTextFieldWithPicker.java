package com.marcgrue.dcisample_a.infrastructure.wicket.form;

import com.google.code.joliratools.StatelessAjaxEventBehavior;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.datetime.DateConverter;
import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.devutils.stateless.StatelessComponent;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.DateValidator;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.Date;
import java.util.Map;

//import org.wicketstuff.stateless.StatelessAjaxEventBehavior;

/**
 * {@link DateTextField} with a {@link DatePicker}.
 */
@StatelessComponent
public class DateTextFieldWithPicker extends DateTextField
{
    DatePicker datePicker;

    // Configurable widget options
    LocalDate earliestDate;
    LocalDate selectedDate;

    final static String YUI_DATE_FORMAT = "MM/dd/yyyy";

    public DateTextFieldWithPicker( String id, String label, Component model )
    {
//      this( id, new PropertyModel<Date>( model, id ), new StyleDateConverter( "S-", true ) );
        this( id, label, new PropertyModel<Date>( model, id ), new PatternDateConverter( "yyyy-MM-dd", true ) );

    }

    public DateTextFieldWithPicker( String id, String label, IModel<Date> model, DateConverter converter )
    {
        super( id, model, converter );

        // Make the text field reachable by Ajax requests
        setOutputMarkupId( true );

        // Set required as default
        setRequired( true );

        // Add Date Picker with callback configuration
        add( newDatePicker().setShowOnFieldClick( true ) );

        setNonEmptyLabel( label );

        // Show calendar if tabbing into the text field (weird it isn't default)
        add( new StatelessAjaxEventBehavior( "onfocus" )
        {
            @Override
            protected void onEvent( AjaxRequestTarget target )
            {
                String componentId = getComponent().getMarkupId();
                String widgetId = componentId + "DpJs";
                target.appendJavaScript(
                      "Wicket.DateTime.showCalendar(" +
                            "YAHOO.wicket['" + widgetId + "'], " +
                            "document.getElementById('" + componentId + "').value, " +
                            "'yyyy-MM-dd');"
                );
            }

            @Override
            protected PageParameters getPageParameters()
            {
                return null;
            }
        } );

//        add( new StatelessAjaxEventBehavior( "onBlur" )
//        {
//            @Override
//            protected void onEvent( AjaxRequestTarget target )
//            {
//                String componentId = getComponent().getMarkupId();
//                String widgetId = componentId + "DpJs";
//                target.appendJavaScript(
//                      "Wicket.DateTime.showCalendar(" +
//                            "YAHOO.wicket['" + widgetId + "'], " +
//                            "document.getElementById('" + componentId + "').value, " +
//                            "'yyyy-MM-dd');" +
//                      "YAHOO.wicket['" + widgetId + "'].hide();" );
//            }
//
//            @Override
//            protected PageParameters getPageParameters()
//            {
//                return null;
//            }
//        } );
    }

    private void setNonEmptyLabel( String label )
    {
        if (label == null)
            return;

        if (label.isEmpty())
            throw new IllegalArgumentException( "Can't set an empty label on the drop down selector." );

        setLabel( Model.of( label ) );
    }


    /**
     * The DatePicker that gets added to the DateTextField component. Users may override this method
     * with a DatePicker of their choice.
     *
     * @return a new {@link DatePicker} instance
     */
    protected DatePicker newDatePicker()
    {
        return new DatePicker()
        {
            private static final long serialVersionUID = 1L;

            @Override
            protected void configure( final Map<String, Object> widgetProperties,
                                      final IHeaderResponse response, final Map<String, Object> initVariables )
            {
                super.configure( widgetProperties, response, initVariables );

                DateTextFieldWithPicker.this.configure( widgetProperties );
            }
        };
    }


    /**
     * Gives overriding classes the option of adding (or even changing/ removing) configuration
     * properties for the javascript widget. See <a
     * href="http://developer.yahoo.com/yui/calendar/">the widget's documentation</a> for the
     * available options. If you want to override/ remove properties, you should call
     * super.configure(properties) first. If you don't call that, be aware that you will have to
     * call {@link #localize(Map)} manually if you like localized strings to be added.
     *
     * @param widgetProperties the current widget properties
     */
    protected void configure( Map<String, Object> widgetProperties )
    {
        // Set various YUI calendar options - add more if necessary
        widgetProperties.put( "mindate", getEarliestDateStr() );
        widgetProperties.put( "selected", getSelectedDateStr() );
    }

    public DateTextFieldWithPicker earliestDate( LocalDate newEarliestDate )
    {
        if (selectedDate != null && newEarliestDate.isAfter( selectedDate ))
            throw new IllegalArgumentException( "Earliest date can't be before selected day." );

        earliestDate = newEarliestDate;

        // Input field validation - date should be _after_ minimumDate (not the same)
        LocalDate minimumDate = newEarliestDate.minusDays( 1 );
        Date convertedMinimumDate = new DateTime( minimumDate.toDateTime( new LocalTime() ) ).toDate();
        add( DateValidator.minimum( convertedMinimumDate ) );

        return this;
    }

    // Add latestDate(..) + other configuration options if needed..

    public DateTextFieldWithPicker selectedDate( LocalDate newSelectedDate )
    {
        if (earliestDate != null && newSelectedDate.isBefore( earliestDate ))
            throw new IllegalArgumentException( "Selected date can't be before earliest day." );

        selectedDate = newSelectedDate;

        return this;
    }

    private String getSelectedDateStr()
    {
        if (selectedDate != null)
            return selectedDate.toString( YUI_DATE_FORMAT );

        // Select today or earliest date (if later) as default
        return earliestDate == null ?
              new LocalDate().toString( YUI_DATE_FORMAT ) :
              earliestDate.toString( YUI_DATE_FORMAT );
    }

    private String getEarliestDateStr()
    {
        return earliestDate == null ? "" : earliestDate.toString( YUI_DATE_FORMAT );
    }
}