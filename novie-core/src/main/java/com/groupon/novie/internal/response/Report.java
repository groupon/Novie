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
package com.groupon.novie.internal.response;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.google.common.collect.Lists;
import com.groupon.novie.internal.serialize.SummaryJsonCustomSerializer;

/**
 * <p>
 * This class is a representation of the data returned by this API.
 * </p>
 *
 * @author ricardo
 */
@XmlRootElement(name = "report")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"total", "grouping", "summary", "records"})
public class Report {

    /**
     * Total elements available for the requested report
     */
    @XmlAttribute
    private int total;

    /**
     * List of elements used in the GROUP BY clause.
     * <p/>
     * This elements where provided by the user.
     */
    @XmlElement
    private List<String> grouping = Lists.newArrayList();

    /**
     * The summary is a marker interface as the values for json serialization
     * come from the object fields and each type of report has a different type
     * of summary.
     * <p/>
     * For full header information refer to the
     *
     * @see <a href="http://goo.gl/MJK64">API documentation</a>
     */
    @JsonSerialize(using = SummaryJsonCustomSerializer.class)
    @XmlElement
    private ReportMeasure summary;

    /**
     * List of object that subclasses ReportRecord. Each type of report has a
     * different set of parameters.
     */
    @XmlElement(name = "record")
    @XmlElementWrapper(name = "records")
    private List<ReportRecord> records = new ArrayList<ReportRecord>();

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<String> getGrouping() {
        return grouping;
    }

    public void setGrouping(List<String> grouping) {
        this.grouping = grouping;
    }

    public ReportMeasure getSummary() {
        return summary;
    }

    public void setSummary(ReportMeasure summary) {
        this.summary = summary;
    }

    public List<ReportRecord> getRecords() {
        return records;
    }

    public void setRecords(List<ReportRecord> records) {
        this.records = records;
    }

    public void addGroup(String group) {
        grouping.add(group);
    }

}
