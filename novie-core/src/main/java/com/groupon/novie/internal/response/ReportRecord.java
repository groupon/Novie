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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.groupon.novie.internal.serialize.MapXmlMeasureAdapter;

@XmlRootElement
public class ReportRecord implements MeasureAppender {

    protected Map<String, GroupDisplayingRecord> group = new LinkedHashMap<String, GroupDisplayingRecord>();

    private Map<String, Number> measures = new LinkedHashMap<String, Number>();

    @XmlElement
    @XmlElementWrapper(name = "groups")
    public List<GroupDisplayingRecord> getGroup() {
        List<GroupDisplayingRecord> returnValue = new ArrayList<GroupDisplayingRecord>(group.size());
        for (Map.Entry<String, GroupDisplayingRecord> e : group.entrySet()) {
            returnValue.add(e.getValue());
        }
        return Collections.unmodifiableList(returnValue);
    }

    public GroupDisplayingRecord addOrRetrieveGroup(String groupName) {
        GroupDisplayingRecord gdr = group.get(groupName);
        if (gdr == null) {
            gdr = new GroupDisplayingRecord(groupName);
            group.put(groupName, gdr);
        }
        return gdr;
    }

    @Override
    public void addMeasure(String measureName, Number value) {
        measures.put(measureName, value);
    }

    @XmlElement
    @XmlJavaTypeAdapter(MapXmlMeasureAdapter.class)
    public Map<String, Number> getMeasures() {
        return measures;
    }

    @Override
    public String toString() {
        StringBuilder returnValue = new StringBuilder();
        returnValue.append("ReportRecord [");
        returnValue.append("group=");
        returnValue.append(group);
        returnValue.append(", ");
        returnValue.append(measures);
        returnValue.append("]");
        return returnValue.toString();
    }
}
