/*

Copyright 2010, Google Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the
distribution.
    * Neither the name of Google Inc. nor the names of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,           
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY           
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package com.google.refine.exporters;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Properties;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.refine.browsing.Engine;
import com.google.refine.model.Project;
import com.google.refine.util.ParsingUtilities;
import com.google.refine.util.IOUtils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


public class HdfsExporter implements WriterExporter{

    final static Logger logger = LoggerFactory.getLogger("HdfsExporter");
    char separator;
    String propertPath = "/properties/HdfsInfo.properties";
    FSDataOutputStream out = null;
    //int rowCount = 0;

    public HdfsExporter() {
        separator = ','; //Comma separated-value is default
    }

    public HdfsExporter(char separator) {
        this.separator = separator;
    }
    
    private static class SetConfiguration {
        @JsonProperty("separator")
        protected String separator = null;
        @JsonProperty("lineSeparator")
        protected String lineSeparator = null;
        @JsonProperty("quoteAll")
        protected boolean quoteAll = false;
    }

    public void writeData(String rowData) {
        try {
                out.write(rowData.getBytes("UTF-8"));
        } catch (Exception e) {
                logger.error("Error Msg :: {}", e.getMessage());
        }
        /*
        rowCount += 1;
        if (rowCount % 10000 == 0) {
            logger.error("[IRIS] ROW :: {}", rowCount);
        }
        */
    }

    @Override
    public void export(Project project, Properties params, Engine engine, final Writer writer)
            throws IOException {
        String optionsString = (params == null) ? null : params.getProperty("options");
        SetConfiguration options = new SetConfiguration();
        if (optionsString != null) {
            try {
                options = ParsingUtilities.mapper.readValue(optionsString, SetConfiguration.class);
            } catch (IOException e) {
                // Ignore and keep options null.
                e.printStackTrace();
            }
        }
        if (options.separator == null) {
            options.separator = Character.toString(separator);
        }
        
        final String separator = options.separator;
        final String lineSeparator = System.getProperty("line.separator");
        
        final boolean printColumnHeader =
            (params != null && params.getProperty("printColumnHeader") != null) ?
                Boolean.parseBoolean(params.getProperty("printColumnHeader")) :
                true;
        
        Properties hdsfInfo = IOUtils.getProperty(propertPath);
        String workDir =  System.getProperty("user.dir");

        Configuration hdfsConf = new Configuration();
        hdfsConf.addResource(new Path(workDir + "/conf/hdfs-site.xml"));
        hdfsConf.addResource(new Path(workDir + "/conf/core-site.xml"));

        FileSystem fs = null;
        String defaultFs = hdsfInfo.getProperty("defaultFs");
        String rootPath = hdsfInfo.getProperty("rootPath");
        try {
                fs = FileSystem.get(new URI(defaultFs), hdfsConf);

                Path outFile = new Path(rootPath + params.getProperty("filename"));
        
                if (!fs.exists(outFile)) {
                    logger.debug("create file::{}", outFile);
                    out = fs.create(outFile);
                }
                else {
                    logger.debug("append found::{}", outFile);
                    out = fs.append(outFile);            
                }
        }
        catch (Exception e) {
                logger.error("Error Msg :: {}", e.getMessage());
        } 

        TabularSerializer serializer = new TabularSerializer() {
            @Override
            public void startFile(JsonNode options) {
            }

            @Override
            public void endFile() {
            }

            @Override
            public void addRow(List<CellData> cells, boolean isHeader) {
                if (!isHeader || printColumnHeader) {
                    String[] strings = new String[cells.size()];
                    String rowData;
                    for (int i = 0; i < strings.length; i++) {
                        CellData cellData = cells.get(i);
                        strings[i] =
                            (cellData != null && cellData.text != null) ?
                            cellData.text : "";
                    }
                    rowData = String.join(separator, strings) + lineSeparator;
                    writeData(rowData);
                }
            }
        };
        long start = System.currentTimeMillis();
        CustomizableTabularExporterUtilities.exportRows(project, engine, params, serializer);
        out.close();
        out = null;
        fs.close();
        fs = null;
        //rowCount = 0;
    
        long end = System.currentTimeMillis();
        logger.debug("hdfs write execution time : {} sec", ( end - start )/1000.0);
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }
}
