/*
 * Copyright (c) 2008, Marcus Ständer. All Rights Reserved.
 * Copyright (c) 2008, Sonny Gill. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.swing.visualizer.application;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.Timer;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.util.display.PaintListener;

/**
 * This class wraps a prefuse <code>display</code> into a JScrollPane that
 * also handles dragging the graph/tree out of the pane.
 *
 * @author <a href="http://www.msdevelopment.org">marcus staender</a>
 */

/* A notice to a workaround: forwardScollpaneChanges If the prefuse display notifies the scrollpane about a changes
 * the scrollpane would instantly report that value back to the pane, which reports it back to the scrollpane...
 * To face this I tried to overwrite methods of JScrollBar *not* to fire events if I don't want it.
 * Since it's not the JScrollBar that fires the events I get (even if it has the possibilities,
 * but it is the model behind) and I didn't want to change the model I implemented a boolean
 * variable forwardScollpaneChanges. This is set to false if prefuse reports changes and it causes
 * the eventhandler *NOT* to forward any changes to prefuse as long as set to false.
 */

public class PrefuseJScrollPane extends JPanel
{
    private static final long serialVersionUID = 1L;

    /**
     * The horizontal scrollbar.
     */
    protected JScrollBar barH = null;

    /**
     * The vertical scrollbar.
     */
    protected JScrollBar barV = null;

    /**
     * If false, no events will be forwarded to prefuse.
     * This should be set to false before che changes made in prefuse are set and set back to
     * true if the changes are submitted.
     */
    protected boolean forwardScollpaneChanges = true;

    /**
     * The last horizontal value. The difference between this and current value is used for prefuses "pan".
     */
    protected int lastH = 0;

    /**
     * The last vertical value. The difference between this and current value is used for prefuses "pan".
     */
    protected int lastV = 0;

    /**
     * Indicates that a panning action caused by moving the horizontal scrollbar is still in progress
     */
    private boolean barHAdjustmentInProgress = false;

    /**
     * Indicates that a panning action caused by moving the vertical scrollbar is still in progress
     */
    private boolean barVAdjustmentInProgress = false;

    /**
     * The prefuse <code>display</code>.
     */
    protected Display m_display = null;

    private static int UNIT_INCREMENT = 20;
    private static int PREVIOUSLY_VISIBLE_AREA_WHEN_SCROLLING = 50;

    /**
     * Initializes an empty pane
     */
    public PrefuseJScrollPane()
    {
        super();
        initialize();
    }

    /**
     * Creates a scrollpane and ads the display.
     *
     * @param display The prefuse <code>display</code>.
     */
    public PrefuseJScrollPane( Display display )
    {
        super();
        initialize();
        addDisplay( display );
    }


    /**
     * Initializes the scrollbar values and the layout.
     */
    protected void initialize()
    {

        barH = new JScrollBar( JScrollBar.HORIZONTAL );
        barH.setMaximum( 0 );
        barH.setUnitIncrement( UNIT_INCREMENT );
        barH.addAdjustmentListener( new ScrollbarListener()
        {

            public void adjustmentValueChanged( AdjustmentEvent e )
            {

                // This is kind of an ugly work around. I tried to modify the ScrollBar
                if( forwardScollpaneChanges && !e.getValueIsAdjusting() )
                {
                    barHAdjustmentInProgress = true;

                    m_display.animatePan( lastH - e.getValue(), 0, 500 );
                    m_display.repaint();
                    lastH = e.getValue();
                }

            }

        } );

        barV = new JScrollBar( JScrollBar.VERTICAL );
        barV.setMaximum( 0 );
        barV.setUnitIncrement( UNIT_INCREMENT );
        barV.addAdjustmentListener( new ScrollbarListener()
        {

            public void adjustmentValueChanged( AdjustmentEvent e )
            {

                // This is kind of an ugly work around. I tried to modify the ScrollBar
                if( forwardScollpaneChanges && !e.getValueIsAdjusting() )
                {
                    barVAdjustmentInProgress = true;

                    m_display.animatePan( 0, lastV - e.getValue(), 500 );
                    m_display.repaint();
                    lastV = e.getValue();
                }

            }

        } );

        setLayout( new BorderLayout() );

    }

    /**
     * This method adds the prefuse <code>display</code> to the scrollpane.
     *
     * @param display The prefuse <code>display</code> that should be used.
     */
    public void addDisplay( Display display )
    {

        // Initially the scrollbars are not yet added to the layout
        if( this.m_display != null )
        {
            // If there was an old display, remove it from the pane
            remove( this.m_display );
        }
        else
        {
            add( barV, BorderLayout.EAST );
            add( barH, BorderLayout.SOUTH );
        }

        this.m_display = display;

        // Add the paint listener that notifies us when the pane is repainted
        this.m_display.addPaintListener( new DisplayPaintListener( this ) );

        add( this.m_display, BorderLayout.CENTER );

        // Fit the scrollbars
        checkScrollbarState();

    }

    /**
     * This method sets the values for the scrollbars according to the <code>display</code> state.
     */
    public void checkScrollbarState()
    {
        if( this.m_display != null )
        {

            // Get the BoundingBox around all items
            Rectangle2D rect = m_display.getVisualization().getBounds( Visualization.ALL_ITEMS );

            /*
             * Only some personal thoughts: might contain mistakes since I had to do correct some bugs when testing!
             * Getting the true scroll bar values is a bit tricky.
             * The calculation thoughts for the horizontal scrollbar:
             *  - The affine transformation has 0 as linear x translation if the root is in the upper left corner
             *  - rect = m_display.getVisualization().getBounds(Visualization.ALL_ITEMS) gives the BoundingBox around
             *    all elements in unscaled coordinates
             *  - The most left element of the graph is located at rect.getX() in absolute coordinates
             *  - That is rect.getX()*m_display.getScale() in scaled coordinates
             *  - Further rect.getWidth*m_display.getScale() is the size of the bounding box to be displayed
             *  - Since the graph can be moved around check
             *     AffineTransform at = m_display.getTransform();
             *  - at.getTranslateX() gives in scaled values the x offset of the root node
             *  - Obviously the whole x area for the scroll pane should be:
             *     - x offset of the most left node to the left border of the paint area in scaled coordinates
             *     - plus the size of the bounding box in scaled coordinates
             *     -> at.getTranslateX()+((rect.getX()+rect.getWidth())*at.getScaleX())
             *     => at.getTranslateX()+(rect.getMaxX()*at.getScaleX())
             *  - The barH.setVisibleAmount is the current size of the view panel
             */

            checkHorizontalScrollbar( rect );
            checkVerticalScrollbar( rect );

        }
    }

    /**
     * This method detects the values for the vertical scrollbar.
     *
     * @param rect The bounding box around all items in the prefuse graph/tree.
     */
    protected void checkVerticalScrollbar( Rectangle2D rect )
    {
        int newHeight = 0;
        int newValue = 0;
        int newVisibleAmount = m_display.getHeight();

        if( newVisibleAmount < 0 )
        {

            // May happen during initialization: -1
            newVisibleAmount = 0;
        }

        AffineTransform at = m_display.getTransform();

        if( at != null )
        {

            // Calculate values for the JScrollBar
            int scaledTopYOfBB = (int) ( rect.getY() * at.getScaleY() );    // Top bound of BB
            int scaledBottomYOfBB = (int) ( rect.getMaxY() * at.getScaleY() );    // Bottom bound of BB
            int scaledHeightBB = (int) ( rect.getHeight() * at.getScaleY() );    // Scaled height of the BB
            int scaledTranslatedTopY = (int) at.getTranslateY();    // If 0 -> root is at left side

            // This is the offset that has to be added when the graph is dragged out of the pane to the bottom
            int scaledOffsetYTop = 0;

            if( scaledTranslatedTopY > -scaledTopYOfBB )
            {
                scaledOffsetYTop = scaledTranslatedTopY + scaledTopYOfBB;
            }

            // This is the offset that has to be added when the graph is dragged out of the pane to the top
            int scaledOffsetYBottom = 0;

            if( m_display.getHeight() - scaledTranslatedTopY > scaledBottomYOfBB )
            {
                scaledOffsetYBottom = ( +m_display.getHeight() - scaledTranslatedTopY - scaledBottomYOfBB );
            }

            // Height is top offset + height of BB + bottom offset
            newHeight = scaledOffsetYTop + scaledHeightBB + scaledOffsetYBottom;

            // Calculate value of the slider

            if( scaledOffsetYBottom > 0 )
            {

                // If the graph is dragged out to the top the slider has to be on the bottom
                newValue = newHeight - scaledTopYOfBB;
            }
            else if( scaledOffsetYTop == 0 )
            {

                // Else if it is *NOT* dragged out on the  bottom, use normal behavior
                newValue = (int) ( m_display.getDisplayY() - scaledTopYOfBB );
            }

            // Otherwise (if dragged out on the bottom) align the slider on the top
            // (default value is 0, so do nothing)

        }

        if( barVAdjustmentInProgress )
        {
            // This is required because the movement of scrollbar causes as an animated pan
            // Hence this method will be called many times while the animated pan action is completed
            // During this time, the scrollbar value, extent etc. must not be updated
            barVAdjustmentInProgress = m_display.isTranformInProgress();
            return;
        }

        // Set the values
        if( newHeight > m_display.getHeight() )
        {

            // Show bar if the graph does not fit on the screen
            add( barV, BorderLayout.EAST );
            revalidate();

            forwardScollpaneChanges = false;
            barV.setMaximum( newHeight );
            barV.setVisibleAmount( newVisibleAmount );
            barV.setValue( newValue );
            barV.setBlockIncrement( newVisibleAmount - PREVIOUSLY_VISIBLE_AREA_WHEN_SCROLLING );
            lastV = newValue;
            forwardScollpaneChanges = true;

        }
        else
        {

            remove( barV );
            revalidate();

        }

    }

    /**
     * This method detects the values for the horizontal scrollbar.
     *
     * @param rect The bounding box around all items in the prefuse graph/tree.
     */
    protected void checkHorizontalScrollbar( Rectangle2D rect )
    {
        int newWidth = 0;
        int newValue = 0;
        int newVisibleAmount = m_display.getWidth();

        if( newVisibleAmount < 0 )
        {

            // May happen during initialization: -1
            newVisibleAmount = 0;
        }

        AffineTransform at = m_display.getTransform();

        if( at != null )
        {

            // Calculate values for the JScrollBar
            int scaledLeftXOfBB = (int) ( rect.getX() * at.getScaleX() );    // Left bound of BB
            int scaledRightXOfBB = (int) ( rect.getMaxX() * at.getScaleX() );    // Right bound of BB
            int scaledWidthBB = (int) ( rect.getWidth() * at.getScaleX() );    // ScaledWidth of the BB
            int scaledTranslatedLeftX = (int) at.getTranslateX();    // If 0 -> root is at left side

            // This is the offset that has to be added when the graph is dragged out of the pane to the right
            int scaledOffsetXLeft = 0;

            if( scaledTranslatedLeftX > -scaledLeftXOfBB )
            {
                scaledOffsetXLeft = scaledTranslatedLeftX + scaledLeftXOfBB;
            }

            // This is the offset that has to be added when the graph is dragged out of the pane to the left
            int scaledOffsetXRight = 0;

            if( m_display.getWidth() - scaledTranslatedLeftX > scaledRightXOfBB )
            {
                scaledOffsetXRight = +m_display.getWidth() - scaledTranslatedLeftX - scaledRightXOfBB;
            }

            // Width is left offset + width of BB + right offset
            newWidth = scaledOffsetXLeft + scaledWidthBB + scaledOffsetXRight;

            // Calculate value of the slider

            if( scaledOffsetXRight > 0 )
            {

                // If the graph is dragged out to the left the slider has to be on the right
                newValue = newWidth - scaledLeftXOfBB;
            }
            else if( scaledOffsetXLeft == 0 )
            {

                // Else if it is *NOT* dragged out on the  right, use normal behavior
                newValue = (int) ( m_display.getDisplayX() - scaledLeftXOfBB );
            }

            // Otherwise (if dragged out on the right) align the slider on the left
            // (default value is 0, so do nothing)

        }


        if( barHAdjustmentInProgress )
        {
            // This is required because the movement of scrollbar causes as an animated pan
            // Hence this method will be called many times while the animated pan action is completed
            // During this time, the scrollbar value, extent etc. must not be updated
            barHAdjustmentInProgress = m_display.isTranformInProgress();
            return;
        }

        // Set the values
        if( newWidth > m_display.getWidth() )
        {

            // Show bar if the graph does not fit on the screen
            add( barH, BorderLayout.SOUTH );
            revalidate();

            forwardScollpaneChanges = false;
            barH.setMaximum( newWidth );
            barH.setVisibleAmount( newVisibleAmount );
            barH.setValue( newValue );
            barH.setBlockIncrement( newVisibleAmount - PREVIOUSLY_VISIBLE_AREA_WHEN_SCROLLING );
            lastH = newValue;
            forwardScollpaneChanges = true;

        }
        else
        {

            // Remove bar if the graph fits on the screen
            remove( barH );
            revalidate();

        }
    }

    /**
     * The DisplayPaintListener has the intend to notify the scollpane when the content is repainted.
     * Since there seems to be no true solution to get informed about scale changes or movement this might be
     * a long living work around.
     * If someone finds a better solution feel free to change the notification method.<br />
     * <br />
     * My first attempt was creating a control that notifies me on all mouse actions but that didn't catch
     * automated zooms, so I tied a control with a timer that checks if transformations or scaling changed.<br />
     * Now I have combined the timer idea with the repainting, let�s see if it works better.
     *
     * @author <a href="http://www.msdevelopment.org">marcus staender</a>
     */
    public class DisplayPaintListener implements PaintListener
    {

        /**
         * The delay after which a new update is allowed to avoid too high load.
         */
        protected static final int DEFAULT_TIMER_DELAY = 50;

        /**
         * A reference to the scrollpane.
         */
        protected PrefuseJScrollPane m_pane = null;

        /**
         * If <code>true</code> updates are allowed, if <code>false</code> they are ignored.
         */
        protected boolean acceptRecheck = true;

        /**
         * The timer managing <code>acceptRecheck</code>.
         */
        protected Timer timerAcceptChanges = null;

        /**
         * The timer delay.
         */
        protected int timerDelay = DEFAULT_TIMER_DELAY;    // milliseconds

        /**
         * The task that should be performed.
         */
        protected ActionListener taskPerformer = new ActionListener()
        {
            public void actionPerformed( ActionEvent evt )
            {
                timerAcceptChanges.stop();
                acceptRecheck = true;
            }
        };


        /**
         * This creates a <code>DisplayPaintListener</code>
         *
         * @param pane A reference to the scrollpane.
         */
        public DisplayPaintListener( PrefuseJScrollPane pane )
        {
            this( pane, DEFAULT_TIMER_DELAY );
        }

        /**
         * This creates a <code>DisplayPaintListener</code> and adds a prefuse <code>display</code>
         *
         * @param pane        A reference to the scrollpane.
         * @param acceptDelay The <code>display</code> to add.
         */
        public DisplayPaintListener( PrefuseJScrollPane pane, int acceptDelay )
        {
            this.m_pane = pane;
            this.timerDelay = acceptDelay;
            timerAcceptChanges = new Timer( timerDelay, taskPerformer );
        }


        /**
         * Notification that Display painting has completed.
         *
         * @param d the Display about to paint itself
         * @param g the Graphics context for the Display
         */
        public void postPaint( Display d, Graphics2D g )
        {
            notifyPane();
        }

        /**
         * Notification that Display painting is beginning.
         *
         * @param d the Display about to paint itself
         * @param g the Graphics context for the Display
         */
        public void prePaint( Display d, Graphics2D g )
        {
        }

        /**
         * This method fires the notification for the scrollbars to get actual values from the <code>display</code>.
         */
        protected void notifyPane()
        {
            if( acceptRecheck )
            {
                acceptRecheck = false;
                timerAcceptChanges.start();

                if( m_pane != null )
                {
                    m_pane.checkScrollbarState();
                }
            }
        }
    }    // End of DisplayPaintListener


    /**
     * A small helper class for asigning an adjustment listener
     *
     * @author <a href="http://www.msdevelopment.org">marcus staender</a>
     */
    public abstract class ScrollbarListener implements AdjustmentListener
    {
        public ScrollbarListener()
        {
        }
    }
}
