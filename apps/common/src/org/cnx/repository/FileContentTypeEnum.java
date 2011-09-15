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
package org.cnx.repository;

import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.cnx.repository.atompub.CnxMediaTypes;

/**
 * This Enum maps file name to ContentType using file extensions.
 * 
 * @author Arjun Satyapal
 */
public enum FileContentTypeEnum {
    // Wolfram
    CDF(".cdf", CnxMediaTypes.APPLICATION_CDF_TYPE),
    
    // Cad.
    DWG(".dwg", CnxMediaTypes.IMAGE_DWG_TYPE),
    
    // Postscript
    EPS(".eps", CnxMediaTypes.APPLICATION_POST_SCRIPT_TYPE),
    
    // Image
    JPG(".jpg", CnxMediaTypes.IMAGE_JPG_TYPE),
    
    // Labview : Texas Instruments.
    LLB(".llb", CnxMediaTypes.APPLICATION_LLB_TYPE),
    
    // Wolfram.
    NBP(".nbp", CnxMediaTypes.APPLICATION_NBP_TYPE),
    
    // Image
    PNG(".png", CnxMediaTypes.IMAGE_PNG_TYPE),
    
    // Postscript
    PS(".ps", CnxMediaTypes.APPLICATION_POST_SCRIPT_TYPE),
    
    // Tex / Latex
    TEX(".tex", CnxMediaTypes.APPLICATION_TEX_TYPE),
    
    // Labview
    VI(".vi", CnxMediaTypes.APPLICATION_VI_TYPE),

    // Labview
    VIINFO(".viinfo", CnxMediaTypes.APPLICATION_VI_TYPE),
    
    // Zip.
    ZIP(".zip", CnxMediaTypes.APPLICATION_ZIP_TYPE),
    
    // Default value.
    DEFAULT("", CnxMediaTypes.APPLICATION_OCTET_STREAM_TYPE);

    private static Logger logger = Logger.getLogger(FileContentTypeEnum.class.getName());
    
    /* File Extension. e.g. If file name is abc.txt, then extension is txt */
    private String extension;
    private MediaType mediaType;

    FileContentTypeEnum(String extension, MediaType mediaType) {
        this.extension = extension;
        this.mediaType = mediaType;
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
    public MediaType getContentType() {
        return mediaType;
    }
    
    /**
     * Returns ContentType on the basis of File Extension.
     * 
     * If file extension does not match to a supported format, then {@link #DEFAULT} is returned.
     * 
     * @param fileName File Extension.
     * @return FileContentType.
     */
    public static FileContentTypeEnum getFileContentTypeEnumFromFileName(String fileName) { 
        for (FileContentTypeEnum curr : FileContentTypeEnum.values()) {
            if (fileName.endsWith(curr.getExtension())) {
                return curr;
            }
        }
        
        logger.warning("Sending Default Enum type for file : " + fileName);
        return DEFAULT;
    }
}
