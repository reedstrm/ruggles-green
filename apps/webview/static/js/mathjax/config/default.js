/*************************************************************
 *
 *  MathJax/config/default.js
 *
 *  This configuration file is loaded when you load MathJax
 *  via <script src="MathJax.js?config=default"></script>
 *
 *  Use it to customize the MathJax settings.  See comments below.
 *
 *  ---------------------------------------------------------------------
 *  
 *  Copyright (c) 2009-2011 Design Science, Inc.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


/*
 *  This file lists most, but not all, of the options that can be set for
 *  MathJax and its various components.  Some additional options are
 *  available, however, and are listed in the various links at:
 *  
 *  http://www.mathjax.org/resources/docs/?configuration.html#configuration-options-by-component
 *
 *  You can add these to the configuration object below if you 
 *  want to change them from their default values.
 */

MathJax.Hub.Config({

  //
  //  A comma-separated list of configuration files to load
  //  when MathJax starts up.  E.g., to define local macros, etc.
  //  The default directory is the MathJax/config directory.
  //  
  //  Example:    config: ["local/local.js"],
  //  Example:    config: ["local/local.js","MMLtoHTML.js"],
  //
  config: ["MMLorHTML.js"],
  
  //
  //  A comma-separated list of CSS stylesheet files to be loaded
  //  when MathJax starts up.  The default directory is the
  //  MathJax/config directory.
  // 
  //  Example:    styleSheets: ["MathJax.css"],
  //
  styleSheets: [],
  
  //
  //  Styles to be defined dynamically at startup time.
  //  
  //  Example:
  //      styles: {
  //        ".MathJax .merror": {
  //          color: "blue",
  //          "background-color": "green"
  //        }
  //      },
  //
  styles: {},
  
  //
  //  A comma-separated list of input and output jax to initialize at startup.
  //  Their main code is loaded only when they are actually used, so it is not
  //  inefficient to include jax that may not actually be used on the page.  These
  //  are found in the MathJax/jax directory.
  //  
  jax: ["input/MathML","output/HTML-CSS", "output/NativeMML"],
  
  //
  //  A comma-separated list of extensions to load at startup.  The default
  //  directory is MathJax/extensions.
  //  
  //  Example:    extensions: ["tex2jax.js","TeX/AMSmath.js","TeX/AMSsymbols.js"],
  //
  extensions: ["mml2jax.js", "MathMenu.js", "MathZoom.js"],
  
  //
  //  Patterns to remove from before and after math script tags.  If you are not
  //  using one of the preprocessors (e.g., tex2jax), you need to insert something
  //  extra into your HTML file in order to avoid a bug in Internet Explorer.  IE
  //  removes spaces from the DOM that it thinks are redundent, and since a SCRIPT
  //  tag usually doesn't add content to the page, if there is a space before and after
  //  a MathJax SCRIPT tag, IE will remove the first space.  When MathJax inserts
  //  the typeset mathematics, this means there will be no space before it and the
  //  preceeding text.  In order to avoid this, you should include some "guard characters"
  //  before or after the math SCRIPT tag; define the patterns you want to use below.
  //  Note that these are used as regular expressions, so you will need to quote
  //  special characters.  Furthermore, since they are javascript strings, you must
  //  quote javascript special characters as well.  So to obtain a backslash, you must
  //  use \\ (doubled for javascript).  For example, "\\[" is the pattern \[ in the
  //  regular expression.  That means that if you want an actual backslash in your
  //  guard characters, you need to use "\\\\" in order to get \\ in the regular
  //  expression, and \ in the actual text.  If both preJax and postJax are defined,
  //  both must be present in order to be  removed.
  //
  //  See also the preRemoveClass comments below.
  //  
  //  Example:
  //      preJax: "\\\\\\\\",  // makes a double backslash the preJax text
  //    or
  //      preJax:  "\\[\\[", // jax scripts must be enclosed in double brackets
  //      postJax: "\\]\\]",
  //
  preJax: null,
  postJax: null,
  
  //
  //  The CSS class for a math preview to be removed preceeding a MathJax
  //  SCRIPT tag.  If the tag just before the MathJax SCRIPT tag is of this
  //  class, its contents are removed when MathJax processes the SCRIPT
  //  tag.  This allows you to include a math preview in a form that will
  //  be displayed prior to MathJax performing its typesetting.  It also
  //  avoids the Internet Explorer space-removal bug, and can be used in
  //  place of preJax and postJax if that is more convenient.
  //  
  //  For example
  //  
  //      <span class="MathJax_Preview">[math]</span><script type="math/tex">...</script>
  //
  //  would display "[math]" in place of the math until MathJax is able to typeset it.
  //
  preRemoveClass: "MathJax_Preview",
  
  //
  //  This value controls whether the "Processing Math: nn%" message are displayed
  //  in the lower left-hand corner.  Set to "false" to prevent those messages (though
  //  file loading and other messages will still be shown).
  //
  showProcessingMessages: true,
  
  //
  //  This value controls the verbosity of the messages in the lower left-hand corner.
  //  Set it to "none" to eliminate all messages, or set it to "simple" to show
  //  "Loading..." and "Processing..." rather than showing the full file name and the
  //  percentage of the mathematics processed.
  //
  messageStyle: "normal",
  
  //
  //  These two parameters control the alignment and shifting of displayed equations.
  //  The first can be "left", "center", or "right", and determines the alignment of
  //  displayed equations.  When the alignment is not "center", the second determines
  //  an indentation from the left or right side for the displayed equations.
  //  
  displayAlign: "center",
  displayIndent: "0em",
  
  //
  //  Normally MathJax will perform its starup commands (loading of
  //  configuration, styles, jax, and so on) as soon as it can.  If you
  //  expect to be doing additional configuration on the page, however, you
  //  may want to have it wait until the page's onload hander is called.  If so,
  //  set this to "onload".
  //
  delayStartupUntil: "none",

  //
  //  Normally MathJax will typeset the mathematics on the page as soon as
  //  the page is loaded.  If you want to delay that process, in which case
  //  you will need to call MathJax.Hub.Typeset() yourself by hand, set
  //  this value to true.
  //
  skipStartupTypeset: false,
  
  //
  //  A list of element ID's that are the ones to process for mathematics
  //  when any of the Hub typesetting calls (Typeset, Process, Update, etc)
  //  are called with no element specified.  This lets you restrict the
  //  processing to particular containers rather than scanning the entire
  //  document for mathematics.  If none are supplied, the entire document
  //  is processed.
  //
  elements: [],
  
  //============================================================================
  //
  //  These parameters control the mml2jax preprocessor (when you have included
  //  "mml2jax.js" in the extensions list above).
  //
  mml2jax: {
    
    //
    //  Controls whether mml2jax inserts MathJax_Preview spans to make a
    //  preview available, and what preview to use, whrn it locates
    //  mathematics on the page.  The default is "alttext", which means use
    //  the <math> tag's alttext attribute as the preview (until it is
    //  processed by MathJax), if the tag has one.  Set to "none" to
    //  prevent the previews from being inserted (the math will simply
    //  disappear until it is typeset).  Set to an array containing the
    //  description of an HTML snippet in order to use the same preview for
    //  all equations on the page (e.g., you could have it say "[math]" or
    //  load an image).
    //  
    //  E.g.,     preview: ["[math]"],
    //  or        preview: [["img",{src: "http://myserver.com/images/mypic.jpg"}]]
    //  
    preview: "alttext"
    
  },

  //============================================================================
  //
  //  These parameters control the MathML inupt jax.
  //
  MathML: {
    //
    //  This specifies whether to use TeX spacing or MathML spacing when the
    //  HTML-CSS output jax is used.
    //
    useMathMLspacing: false
  },
  
  //============================================================================
  //
  //  These parameters control the HTML-CSS output jax.
  //
  "HTML-CSS": {
    
    //
    //  This controls the global scaling of mathematics as compared to the 
    //  surrounding text.  Values between 100 and 133 are usually good choices.
    //
    scale: 100,
    
    //
    //  This is a list of the fonts to look for on a user's computer in
    //  preference to using MathJax's web-based fonts.  These must
    //  correspond to directories available in the  jax/output/HTML-CSS/fonts
    //  directory, where MathJax stores data about the characters available
    //  in the fonts.  Set this to ["TeX"], for example, to prevent the
    //  use of the STIX fonts, or set it to an empty list, [], if
    //  you want to force MathJax to use web-based or image fonts.
    //
    availableFonts: ["STIX","TeX"],
    
    //
    //  This is the preferred font to use when more than one of those
    //  listed above is available.
    //
    preferredFont: "TeX",
    
    //
    //  This is the web-based font to use when none of the fonts listed
    //  above are available on the user's computer.  Note that currently
    //  only the TeX font is available in a web-based form.  Set this to
    //  
    //      webFont: null,
    //
    //  if you want to prevent the use of web-based fonts.
    //
    webFont: "TeX",
    
    //
    //  This is the font to use for image fallback mode (when none of the
    //  fonts listed above are available and the browser doesn't support
    //  web-fonts via the @font-face CSS directive).  Note that currently
    //  only the TeX font is available as an image font.  Set this to
    //
    //      imageFont: null,
    //  
    //  if you want to prevent the use of image fonts (e.g., you have not
    //  installed the image fonts on your server).  In this case, only
    //  browsers that support web-based fonts will be able to view your pages
    //  without having the fonts installed on the client computer.  The browsers
    //  that support web-based fonts include: IE6 and later, Chrome, Safari3.1
    //  and above, Firefox3.5 and later, and Opera10 and later.  Note that
    //  Firefox3.0 is NOT on this list, so without image fonts, FF3.0 users
    //  will be required to to download and install either the STIX fonts or the
    //  MathJax TeX fonts.
    //
    imageFont: null,
    
    //
    //  This is the font-family CSS value used for characters that are not
    //  in the selected font (e.g., for web-based fonts, this is where to
    //  look for characters not included in the MathJax_* fonts).  IE will
    //  stop looking after the first font that exists on the system (even
    //  if it doesn't contain the needed character), so order these carefully.
    //  
    undefinedFamily: "STIXGeneral,'Arial Unicode MS',serif",
    
    //
    //  This controls whether the MathJax contextual menu will be available
    //  on the mathematics in the page.  If true, then right-clicking (on
    //  the PC) or control-clicking (on the Mac) will produce a MathJax
    //  menu that allows you to get the source of the mathematics in
    //  various formats, change the size of the mathematics relative to the
    //  surrounding text, and get information about MathJax.
    //  
    //  Set this to false to disable the menu.  When true, the MathMenu 
    //  items below configure the actions of the menu.
    //  
    showMathMenu: true,

    //
    //  This allows you to define or modify the styles used to display
    //  various math elements created by MathJax.
    //  
    //  Example:
    //      styles: {
    //        ".MathJax_Preview": {
    //          "font-size": "80%",          // preview uses a smaller font
    //          color: "red"                 //    and is in red
    //        }
    //      }
    //
    styles: {},
    
    //
    //  Configuration for <maction> tooltips
    //    (see also the #MathJax_Tooltip CSS in MathJax/jax/output/HTML-CSS/config.js,
    //     which can be overriden using the styles values above).
    //
    tooltip: {
      delayPost: 600,          // milliseconds delay before tooltip is posted after mouseover
      delayClear: 600,         // milliseconds delay before tooltip is cleared after mouseout
      offsetX: 10, offsetY: 5  // pixels to offset tooltip from mouse position
    }
  },
  
  //============================================================================
  //
  //  These parameters control the NativeMML output jax.
  //
  NativeMML: {

    //
    //  This controls the global scaling of mathematics as compared to the 
    //  surrounding text.  Values between 100 and 133 are usually good choices.
    //
    scale: 100,

    //
    //  This controls whether the MathJax contextual menu will be available
    //  on the mathematics in the page.  If true, then right-clicking (on
    //  the PC) or control-clicking (on the Mac) will produce a MathJax
    //  menu that allows you to get the source of the mathematics in
    //  various formats, change the size of the mathematics relative to the
    //  surrounding text, and get information about MathJax.
    //  
    //  Set this to false to disable the menu.  When true, the MathMenu 
    //  items below configure the actions of the menu.
    //  
    //  There is a separate setting for MSIE, since the code to handle that
    //  is a bit delicate; if it turns out to have unexpected consequences, 
    //  you can turn it off without turing off other browser support.
    //  
    showMathMenu: true,
    showMathMenuMSIE: true,

    //
    //  This allows you to define or modify the styles used to display
    //  various math elements created by MathJax.
    //  
    //  Example:
    //      styles: {
    //        ".MathJax_MathML": {
    //          color: "red"         //    MathML is in red
    //        }
    //      }
    //
    styles: {}
  },
  
  //============================================================================
  //
  //  These parameters control the contextual menus that are available on the 
  //  mathematics within the page (provided the showMathMenu value is true above).
  //
  MathMenu: {
    //
    //  This is the hover delay for the display of submenus in the
    //  contextual menu.  When the mouse is still over a submenu label for
    //  this long, the menu will appear.  (The menu also will appear if you
    //  click on the label.)  It is in milliseconds.
    //  
    delay: 400,
    
    //
    //  This is the URL for the MathJax Help menu item.
    //
    helpURL: "http://www.mathjax.org/help/user/",

    //
    //  These control whether the "Math Renderer", "Font Preferences",
    //  and "Contextual Menu" submenus will be displayed or not.
    //
    showRenderer: true,
    showFontMenu: false,
    showContext:  false,

    //
    //  These are the settings for the Show Source window.  The initial
    //  width and height will be reset after the source is shown in an
    //  attempt to make the window fit the output better.
    //
    windowSettings: {
      status: "no", toolbar: "no", locationbar: "no", menubar: "no",
      directories: "no", personalbar: "no", resizable: "yes", scrollbars: "yes",
      width: 100, height: 50
    },
    
    //
    //  This allows you to change the CSS that controls the menu
    //  appearance.  See the extensions/MathMenu.js file for details
    //  of the default settings.
    //
    styles: {}
    
  },

  //============================================================================
  //
  //  These parameters control the MMLorHTML configuration file.
  //  NOTE:  if you add MMLorHTML.js to the config array above,
  //  you must REMOVE the output jax from the jax array.
  //
  MMLorHTML: {
    //
    //  The output jax that is to be preferred when both are possible
    //  (set to "MML" for native MathML, "HTML" for MathJax's HTML-CSS output jax).
    //
    prefer: {
      MSIE:    "MML",
      Firefox: "MML",
      Opera:   "HTML",
      other:   "HTML"
    }
  }
});

MathJax.Ajax.loadComplete("[MathJax]/config/default.js");
