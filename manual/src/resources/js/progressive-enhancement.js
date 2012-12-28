

/**
 * Qi4j Documentation WebSite Progressive Enhancement.
 */
$( document ).ready( function($){

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
                $.scrollTo( dt, 200, {
                    'offset': -96
                });
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
    var versions =
    {
        'develop':
        {
            'url': 'http://qi4j.org/develop',
            'relpath': '../develop'
        },
        'latest':
        {
            'url': 'http://qi4j.org/latest',
            'relpath': '../latest'
        },
        '<=1.4.x':
        {
            'url': 'http://qi4j.org/1.4',
            'relpath': '../1.4'
        }
    };
    function endsWith(str, suffix) {
            return str.indexOf(suffix, str.length - suffix.length) !== -1;
    }
    var selected = "latest";
    var stripedHref = window.location.href.substring( 0, window.location.href.lastIndexOf('/') );
    if ( endsWith( stripedHref, 'develop' ) )
        selected = "develop";
    else if ( endsWith( stripedHref, 'latest' ) )
        selected = "latest";
    else if ( endsWith( stripedHref, '1.4' ) )
        selected = "<=1.4.x";
    // --
    var switcher_html ='<p style="margin-top:2em; text-align: center"><select style="font-size: 0.5em">';
    var ifselect = function( candidate ) {
        return candidate == selected ? "selected=\"selected\"" : "";
    }
    for( var version in versions )
    {
        switcher_html += '<option value="' + version + '" ' + ifselect( version ) + '>' + version + '</option>';
    }
    switcher_html += '</select></p>' ;
    $( "div.logo" ).append( switcher_html );
    $( "div.logo select" ).change( function()
    {
        if( window.location.hostname == "qi4j.org" || window.location.hostname == "www.qi4j.org" )
        { // Loaded from qi4j.org
            window.location = versions[ $( this ).val() ].relpath;
        }
        else
        { // Loaded from elsewhere
            window.location = versions[ $( this ).val() ].url;
        }
    } );

    // Add separator space between tutorials series
    $( "div.sub-nav div.toc dt" ).each( function( idx, dt ) {
        var $dt = $( dt );
        var item = $dt.find( "span.section:first-child" ).text().trim();
        switch( item )
        {
            case "Qi4j in 2 minutes":
                $dt.attr( "style", "margin-top: 24px" );
                break;
            case "Qi4j in 2 hours":
            case "Leverage Properties":
            case "Use I/O API":
                $dt.attr( "style", "margin-bottom: 24px" );
                break;
        }
    } );

    // Section specific enhancements
    var $section = $( 'body > div.section' );
    var section_title = $section.attr( 'title' ).trim();
    switch( section_title ) {
        case "Glossary":
            glossary( $section );
            break;
        default:
            break;
    }

} );
