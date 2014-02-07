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

import com.groupon.novie.internal.exception.NovieException;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * <p>
 * {@link XmlAdapter} to tell JAXB how to serialize a {@link Map}
 * </p>
 * <p>
 * The {@link XmlMeasureMapType} object has specific JAXB annotation for
 * attribute names.
 * </p>
 * <p>
 * The Generic implementation allows for Maps of multiple types.
 * </p>
 *
 * @author ricardo
 * @author thomas 
 */
public class MapXmlMeasureAdapter<T> extends XmlAdapter<XmlMeasureMapType<T>, Map<String, T>> {

    @Override
    public Map<String, T> unmarshal(XmlMeasureMapType<T> xmlMapType) throws NovieException {
        Map<String, T> map = new LinkedHashMap<String, T>();
        for (XmlMapEntryType<T> entry : xmlMapType.getEntries()) {
            map.put(entry.getName(), entry.getNumber());
        }
        return map;
    }

    @Override
    public XmlMeasureMapType<T> marshal(Map<String, T> map) throws NovieException {
        XmlMeasureMapType<T> result = new XmlMeasureMapType<T>();
        for (String key : map.keySet()) {
            result.addEntry(key, map.get(key));
        }
        return result;
    }

}
