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
package com.groupon.novie.engine;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This enum represents the supported data type.
 * <p/>
 * It also provide of each of them the method to interpret and format the SQL result.
 *
 * @author thomas
 * @author damiano
 */
public enum ColumnDataType {

    STRING(Types.VARCHAR), DATE(Types.DATE) {
        @Override
        public String mapResult(String alias, ResultSet rs, TimeZone tz) throws SQLException {
            // Use getTimeStamp cause of the tz which is
            // dependent of the table. getDate do not work
            // because it make a tz shift

            FastDateFormat sdfD = FastDateFormat.getInstance(DATE_PATTERN, tz);
            if (rs.getTimestamp(alias, Calendar.getInstance(tz)) != null) {
                Date date = new Date(rs.getTimestamp(alias, Calendar.getInstance(tz)).getTime());
                final String groupValueStr = sdfD.format(date);
                if (LOG.isTraceEnabled()) {
                    FastDateFormat sdfLogTrace = FastDateFormat.getInstance(DATE_TIME_PATTERN, TimeZone.getTimeZone("UTC"));
                    LOG.trace("Grouping - Retrieved date (SqlTypes.Date): UTC-" + sdfLogTrace.format(date) + " => TZ-" + groupValueStr);
                }
                return groupValueStr;
            } else {
                return null;
            }
        }
    },
    DATETIME(Types.TIMESTAMP) {
        @Override
        public String mapResult(String alias, ResultSet rs, TimeZone tz) throws SQLException {

            if (rs.getTimestamp(alias, Calendar.getInstance(tz)) != null) {
                FastDateFormat sdfDT = FastDateFormat.getInstance(DATE_TIME_PATTERN, tz);
                Date date = new Date(rs.getTimestamp(alias, Calendar.getInstance(tz)).getTime());
                final String groupValueStr = sdfDT.format(date);
                if (LOG.isTraceEnabled()) {
                    SimpleDateFormat sdfLogTrace = new SimpleDateFormat(DATE_TIME_PATTERN);
                    sdfLogTrace.setTimeZone(TimeZone.getTimeZone("UTC"));
                    LOG.trace("Grouping - Retrieved date (SqlTypes.TIMESTAMP): UTC-" + sdfLogTrace.format(date) + " => TZ-" + groupValueStr);
                }
                return groupValueStr;
            }
            return "";
        }
    },
    DECIMAL(Types.DECIMAL) {
        @Override
        public String mapResult(String alias, ResultSet rs, TimeZone tz) throws SQLException {
            return String.valueOf(rs.getDouble(alias));
        }

    },
    INTEGER(Types.INTEGER) {
        @Override
        public String mapResult(String alias, ResultSet rs, TimeZone tz) throws SQLException {
            return String.valueOf(rs.getInt(alias));
        }
    };

    private static final Logger LOG = LoggerFactory.getLogger(ColumnDataType.class);
    private static final String DATE_PATTERN = "yyyy-MM-ddZ";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mmZ";

    private int sqlType;

    ColumnDataType(int sqlType) {
        this.sqlType = sqlType;
    }

    public int getSqlType() {
        return sqlType;
    }

    public String mapResult(String alias, ResultSet rs, TimeZone tz) throws SQLException {
        return rs.getString(alias);

    }

}
