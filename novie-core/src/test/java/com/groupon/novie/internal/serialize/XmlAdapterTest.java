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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class XmlAdapterTest {

    @Test
    public void testXmlMeasureAdapter() throws Exception {

        MapXmlMeasureAdapter<String> measureAdapter = new MapXmlMeasureAdapter<String>();
        Map<String, String> map = new HashMap<String, String>();
        map.put("one", "1");
        map.put("two", "2");
        XmlMeasureMapType<String> measureResult = measureAdapter.marshal(map);
        Assert.assertEquals(measureResult.getEntries().size(), map.size());
        for (XmlMapEntryType<String> entry : measureResult.getEntries()) {
            Assert.assertTrue(map.containsKey(entry.getName()));
            Assert.assertEquals(entry.getValue(), map.get(entry.getName()));
        }

        map = measureAdapter.unmarshal(measureResult);
        for (XmlMapEntryType<String> entry : measureResult.getEntries()) {
            Assert.assertTrue(map.containsKey(entry.getName()));
            Assert.assertEquals(entry.getValue(), map.get(entry.getName()));
        }
    }

    @Test
    public void testXmlInformationAdapter() throws Exception {

        MapXmlInformationAdapter<String> informationAdapter = new MapXmlInformationAdapter<String>();
        Map<String, String> map = new HashMap<String, String>();
        map.put("one", "1");
        map.put("two", "2");

        XmlInformationMapType<String> informationResult = informationAdapter.marshal(map);
        Assert.assertEquals(informationResult.getEntries().size(), map.size());
        for (XmlMapEntryType<String> entry : informationResult.getEntries()) {
            Assert.assertTrue(map.containsKey(entry.getName()));
            Assert.assertEquals(entry.getValue(), map.get(entry.getName()));
        }

        map = informationAdapter.unmarshal(informationResult);
        for (XmlMapEntryType<String> entry : informationResult.getEntries()) {
            Assert.assertTrue(map.containsKey(entry.getName()));
            Assert.assertEquals(entry.getValue(), map.get(entry.getName()));
        }
    }
}
