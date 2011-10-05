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

import java.util.logging.Logger;


/**
 * This Enum maps file name to ContentType using file extensions.
 * 
 * @author Arjun Satyapal
 */
public enum FileContentType {
    // Wolfram
    CDF(".cdf", ContentType.CDF),
    
    // Cad.
    DWG(".dwg", ContentType.DWG),
    
    // Postscript
    EPS(".eps", ContentType.EPS),
    
    // Image
    JPG(".jpg", ContentType.JPG),
    
    // Labview .llb Reference : http://www.filesuffix.com/extension/llb.html
    // Labview : Texas Instruments.
    LLB(".llb", ContentType.LLB),
    
    // Wolfram : NBP : Wolfram format. Reference : http://www.wolfram.com/technology/nb/
    NBP(".nbp", ContentType.NBP),
    
    // Image
    PNG(".png", ContentType.PNG),
    
    // Postscript
    PS(".ps", ContentType.PS),
    
    // Tex / Latex
    TEX(".tex", ContentType.TEX),
    
    // Labview : VI Reference : http://cnx.org/eip-help/labview
    VI(".vi", ContentType.VI),

    // Labview
    VIINFO(".viinfo", ContentType.VIINFO),
    
    // Zip.
    ZIP(".zip", ContentType.ZIP),
    
    // Default value.
    DEFAULT("", ContentType.OCTET_STREAM);

    private static Logger logger = Logger.getLogger(FileContentType.class.getName());
    
    /* File Extension. e.g. If file name is abc.txt, then extension is txt */
    private String extension;
    private String contentType;

    private FileContentType(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    /**
     * Returns extension expected for this FileContentType.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Returns ContentType expected for this FileContentType.
     */
    public String getContentType() {
        return contentType;
    }
    
    /**
     * Returns ContentType on the basis of File Extension.
     * 
     * If file extension does not match to a supported format, then {@link #DEFAULT} is returned.
     * 
     * @param fileName File Extension.
     * @return FileContentType.
     */
    public static FileContentType getFileContentTypeEnumFromFileName(String fileName) { 
        for (FileContentType curr : FileContentType.values()) {
            if (fileName.endsWith(curr.getExtension())) {
                return curr;
            }
        }
        
        logger.warning("Sending Default Enum type for file : " + fileName);
        return DEFAULT;
    }
}
