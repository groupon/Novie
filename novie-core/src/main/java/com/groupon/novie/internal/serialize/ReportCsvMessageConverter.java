/*
Copyright (c) 2013, Groupon, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

Neither the name of GROUPON nor the names of its contributors may be
used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.groupon.novie.internal.serialize;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.groupon.novie.internal.response.GroupDisplayingRecord;
import com.groupon.novie.internal.response.Report;
import com.groupon.novie.internal.response.ReportRecord;

/**
 * <p>
 * This Class adds support for transparent csv conversion of the {@link Report}
 * Class at the Controller level.
 * </p>
 *
 * @author ricardo
 */
public class ReportCsvMessageConverter extends AbstractHttpMessageConverter<Report> {

    public static final MediaType MEDIA_TYPE = new MediaType("text", "csv", Charset.forName("utf-8"));

    public ReportCsvMessageConverter() {
        super(MEDIA_TYPE);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Report.class.equals(clazz);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return supports(clazz) && MEDIA_TYPE.includes(mediaType);
    }

    @Override
    protected Report readInternal(Class<? extends Report> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return null;
    }

    @Override
    protected void writeInternal(Report report, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {

        outputMessage.getHeaders().setContentType(MEDIA_TYPE);
        outputMessage.getHeaders().set("Content-Disposition", "attachment; filename=\"records.csv\"");
        PrintWriter writer = new PrintWriter(outputMessage.getBody());

        if (!report.getRecords().isEmpty()) {

            ReportRecord headerRecord = report.getRecords().get(0);
            StringBuilder header = new StringBuilder();
            for (GroupDisplayingRecord headerDisplay : headerRecord.getGroup()) {
                String groupName = headerDisplay.getGroupName();
                for (String key : headerDisplay.getInformations().keySet()) {
                    header.append("\"");
                    header.append(groupName);
                    header.append(".");
                    header.append(key);
                    header.append("\",");
                }
            }

            for (String key : headerRecord.getMeasures().keySet()) {
                header.append("\"");
                header.append(key);
                header.append("\",");
            }

            writer.write(header.substring(0, header.length() - 1));
            writer.write("\n");

            for (ReportRecord record : report.getRecords()) {
                StringBuilder line = new StringBuilder();
                for (GroupDisplayingRecord display : record.getGroup()) {
                    for (String value : display.getInformations().values()) {
                        line.append("\"");
                        line.append(value);
                        line.append("\",");
                    }
                }
                for (Number value : record.getMeasures().values()) {
                    line.append(value);
                    line.append(",");
                }
                writer.write(line.substring(0, line.length() - 1));
                writer.write("\n");
            }
        }
        writer.flush();

    }

}
