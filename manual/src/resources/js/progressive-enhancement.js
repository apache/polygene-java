/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


/**
 * Qi4j Documentation WebSite Progressive Enhancement.
 */
$( document ).ready( function($){

    var atHome = window.location.hostname == "zest.apache.org"

    /**
     * Glossary.
     */
    // Add Alphabetical scroll?
    function glossary( $section ) {

        // Better style
        var $dts = $section.find('dt');
        $dts.css('margin','2em 0 1em 0');
        $dts.css('padding','0 0 0.3em 0.5em');
        $dts.css('border-bottom','1px solid #ddd');
        $dts.css('color','#0B75BC');

        // Better behavior
        function highlight( hash ) {
            if ( hash ) {
                var target = $dts.find(hash);
                var dt = target.parent();
                var dd = dt.next();
                // Scroll
                $.scrollTo( dt, 200, { 'offset': -96 });
                // Highlight
                dd.fadeOut(50).fadeIn(200);
            }
        }
        $( window ).bind( 'hashchange', function() {
            highlight( window.location.hash );
        });
        highlight( window.location.hash );
    }

    // Global enhancements

    // Hide first tab (use the logo link instead)
    $( "div.toc dl dt:first" ).hide();

    // Open external user links in a new window/tab
    $("a.ulink[href^='http:']").attr('target','_blank');
    $("a.ulink[href^='https:']").attr('target','_blank');

    // Add links to different versions
    $.getJSON( "../versions.json", function( versions )
    {
        // alert( JSON.stringify( versions ) );
        var currentPathFragment = window.location.href.substring( 0, window.location.href.lastIndexOf( '/' ) );
        currentPathFragment = currentPathFragment.substring( currentPathFragment.lastIndexOf( '/' ) + 1 );
        var currentVersion = Object.keys( versions ).filter( function( key ) { return versions[ key ] === currentPathFragment } )[ 0 ];
        // alert( "Current version is " + currentVersion + "\nCurrent path fragment is: " + currentPathFragment );
        if( currentVersion )
        {
            var switcher_html ='<p style="margin-top:2em; text-align: center"><select style="font-size: 0.5em">';    
            var ifselect = function( candidate )
            {
                return candidate == currentVersion ? "selected=\"selected\"" : "";
            }
            $.each( versions, function( displayName, pathFragment )
            {
                switcher_html += '<option value="' + displayName + '" ' + ifselect( displayName ) + '>' + displayName + '</option>';
            } );
            switcher_html += '</select></p>';
            $( "div.logo" ).append( switcher_html );
            var toURL = function( displayName )
            {
                if( atHome )
                {
                    return "../" + versions[ displayName ];
                }
                else
                {
                    return "https://zest.apache.org/java/" + versions[ displayName ];
                }
            }
            $( "div.logo select" ).change( function()
            {
                window.location = toURL( $( this ).val() );
            } );
        }
        else
        {
            console.log( "Documentation loaded locally? No version switcher" );
        }
    } );

    // Title links to their own anchor
    $( "body > div.section .title" ).each( function( idx, title ) {
        var $title = $( title );
        var id = $title.find( 'a' ).attr( 'id' );
        if( id ) {
            $title.click( function() {
                window.location.hash = id;
            } );
        }
    } );

    // Scroll down a bit on hash change so that target is not hidden under the floating top menu
    function scrollToHash( hash ) {
        if( hash ) {
            var $target = $( hash );
            if( $target ) {
                setTimeout( function() { $.scrollTo( $target, 100, { 'offset': -96 }) }, 50 );
            }
        }
    };
    $( window ).bind( 'hashchange', function() {
        scrollToHash( window.location.hash );
    });
    scrollToHash( window.location.hash );

    // Enhance left nav
    $( "div.sub-nav div.toc dt" ).each( function( idx, dt ) {
        var $dt = $( dt );
        var item = $dt.find( "span.section:first-child" ).text().trim();
        switch( item )
        {
            // Overview everywhere
            case "Overview":
                $dt.attr( "style", "margin-bottom: 24px" );
            // Tutorials
            case "Zest\u2122 in 30 minutes":
            case "Leverage Properties":
            case "Use I/O API":
                $dt.attr( "style", "margin-bottom: 24px" );
                break;
            // JavaDocs
            case "JavaDocs":
                $dt.hide();
                break;
            // Libraries
            case "Alarms":
                $dt.attr( "style", "margin-top: 24px" );
                break;            
            // Extensions
            case "Ehcache Cache":
            case "Memory EntityStore":
            case "ElasticSearch Index/Query":
            case "Yammer Metrics":
            case "Migration":
                $dt.attr( "style", "margin-top: 24px" );
                break;            
        }
    } );

    // Section specific enhancements
    var $section = $( 'body > div.section' );
    if( $section.attr( 'title' ) )
    {
        var section_title = $section.attr( 'title' ).trim();
        switch( section_title ) {
            case "Glossary":
                glossary( $section );
                break;
            default:
                break;
        }
    }

} );
