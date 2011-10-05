/*
 * Copyright (C) 2011 The CNX Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.cnx.common.repository;



/**
 * This Enum maps file name to ContentType using file extensions.
 * 
 * @author Arjun Satyapal
 */
public class ContentType {
    // Service Document for AtomPub Protocol. 
    public static final String APPLICATION_ATOMSVC_XML = "application/atomsvc+xml";

    public static final String APPLICATION_XML = "application/xml";
    
    public final static String APPLICATION_ATOM_XML = "application/atom+xml";
    
    // Wolfram
    public static final String CDF = "application/vnd.wolfram.cdf.text";
    
    // Cad.
    public static final String DWG = "image/x-dwg";
    
    // Postscript
    public static final String EPS = "application/postscript";
    
    // Image
    public static final String JPG = "image/jpeg";
    
    // Labview .llb Reference : http://www.filesuffix.com/extension/llb.html
    // Labview : Texas Instruments.
    public static final String LLB = "application/octet-stream";
    
    // Wolfram : NBP : Wolfram format. Reference : http://www.wolfram.com/technology/nb/
    public static final String NBP = "application/mathematica";
    
    // Octet-Stream
    public static final String OCTET_STREAM = "application/octet-stream";
    
    // Image
    public static final String PNG = "image/png";
    
    // Postscript
    public static final String PS = "application/postscript";
    
    // Tex / Latex
    public static final String TEX = "application/x-tex";
    

    // Text/html with charset = UTF-8 
    public static final String TEXT_HTML_UTF8 = "text/html; charset=utf-8";

    // text/xml with charset = UTF-8 
    public static final String TEXT_XML_UTF8 = "text/xml; charset=utf-8";
    
    // Labview : VI Reference : http://cnx.org/eip-help/labview
    public static final String VI = "application/x-labview-vi";

    // Labview
    public static final String VIINFO = "application/x-labview-vi";
    
    // Zip.
    public static final String ZIP = "application/zip";
}
