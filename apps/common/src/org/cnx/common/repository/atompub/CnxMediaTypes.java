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
package org.cnx.common.repository.atompub;

import javax.ws.rs.core.MediaType;

/**
 * AtomPub Media Types.
 * 
 * Additional MimeTypes defined as per : http://www.iana.org/assignments/media-types/index.html
 * 
 * @author Arjun Satyapal
 */
public class CnxMediaTypes extends MediaType {
    /** ServiceDocument : "application/atomsvc+xml" */
    public final static String APPLICATION_ATOMSVC_XML = "application/atomsvc+xml";
    public final static MediaType APPLICATION_ATOMSVC_XML_TYPE = new MediaType("application",
            "atomsvc+xml");

    /** Text/html with charset = UTF-8 */
    public final static String TEXT_HTML_UTF8 = "text/html; charset=utf-8";
    public final static MediaType TEXT_HTML_UTF8_TYPE = new MediaType("text", "html");

    /** text/xml with charset = UTF-8 */
    public final static String TEXT_XML_UTF8 = "text/xml; charset=utf-8";
    public final static MediaType TEXT_XML_UTF8_TYPE = new MediaType("text", "xml");

    /** image/png */
    public final static String IMAGE_PNG = "image/png";
    public final static MediaType IMAGE_PNG_TYPE = new MediaType("image", "png");

    /** application/postscript */
    public final static String APPLICATION_POST_SCRIPT = "application/postscript";
    public final static MediaType APPLICATION_POST_SCRIPT_TYPE = new MediaType("application",
            "postscript");

    /** application/vnd.wolfram.cdf.text */
    public final static String APPLICATION_CDF = "application/vnd.wolfram.cdf.text";
    public final static MediaType APPLICATION_CDF_TYPE = new MediaType("application",
            "vnd.wolfram.cdf.text");

    /** image/dwg */
    public final static String IMAGE_DWG = "image/x-dwg";
    public final static MediaType IMAGE_DWG_TYPE = new MediaType("image", "x-dwg");

    /** image/jpg */
    public final static String IMAGE_JPG = "image/jpeg";
    public final static MediaType IMAGE_JPG_TYPE = new MediaType("image", "jpeg");

    /** Labview .llb Reference : http://www.filesuffix.com/extension/llb.html */
    public final static String APPLICATION_LLB = APPLICATION_OCTET_STREAM;
    public final static MediaType APPLICATION_LLB_TYPE = APPLICATION_OCTET_STREAM_TYPE;

    /** NBP : Wolfram format. Reference : http://www.wolfram.com/technology/nb/ */
    public final static String APPLICATION_NBP = "application/mathematica";
    public final static MediaType APPLICATION_NBP_TYPE =
            new MediaType("application", "mathematica");

    /** TEX Reference :  http://filext.com/file-extension/TEX */
    public final static String APPLICATION_TEX= "application/x-tex";
    public final static MediaType APPLICATION_TEX_TYPE =
            new MediaType("application", "x-tex");

    /** VI Reference : http://cnx.org/eip-help/labview */
    public final static String APPLICATION_VI= "application/x-labview-vi";
    public final static MediaType APPLICATION_VI_TYPE =
            new MediaType("application", "x-labview-vi");
    
    
    /** ZIP : Reference : http://en.wikipedia.org/wiki/Internet_media_type */
    public final static String APPLICATION_ZIP= "application/zip";
    public final static MediaType APPLICATION_ZIP_TYPE =
            new MediaType("application", "zip");
}
