/*
 * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
 *
 * Netspective Corporation permits redistribution, modification and use
 * of this file in source and binary form ("The Software") under the
 * Netspective Source License ("NSL" or "The License"). The following 
 * conditions are provided as a summary of the NSL but the NSL remains the 
 * canonical license and must be accepted before using The Software. Any use of
 * The Software indicates agreement with the NSL. 
 *
 * 1. Each copy or derived work of The Software must preserve the copyright
 *    notice and this notice unmodified.
 *
 * 2. Redistribution of The Software is allowed in object code form only 
 *    (as Java .class files or a .jar file containing the .class files) and only 
 *    as part of an application that uses The Software as part of its primary 
 *    functionality. No distribution of the package is allowed as part of a software 
 *    development kit, other library, or development tool without written consent of 
 *    Netspective Corporation. Any modified form of The Software is bound by 
 *    these same restrictions.
 * 
 * 3. Redistributions of The Software in any form must include an unmodified copy of 
 *    The License, normally in a plain ASCII text file unless otherwise agreed to,
 *    in writing, by Netspective Corporation.
 *
 * 4. The names "Sparx" and "Netspective" are trademarks of Netspective 
 *    Corporation and may not be used to endorse products derived from The 
 *    Software without without written consent of Netspective Corporation. "Sparx" 
 *    and "Netspective" may not appear in the names of products derived from The 
 *    Software without written consent of Netspective Corporation.
 *
 * 5. Please attribute functionality to Sparx where possible. We suggest using the 
 *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
 *    http://www.netspective.com for each application using Sparx.
 *
 * The Software is provided "AS IS," without a warranty of any kind. 
 * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
 *
 * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING 
 * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGES.      
 *
 * @author Shahid N. Shah
 */
 
/**
 * $Id: ReportColumnFactory.java,v 1.5 2004-08-09 22:14:27 shahid.shah Exp $
 */

package com.netspective.commons.report.tabular.column;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import com.netspective.commons.report.tabular.TabularReportColumn;
import com.netspective.commons.text.TextUtils;
import com.netspective.commons.value.source.StaticValueSource;

public class ReportColumnFactory
{
    static private Map formats = new HashMap();

    static
    {
        NumberFormat plainFmt = (NumberFormat) NumberFormat.getNumberInstance().clone();
        plainFmt.setGroupingUsed(false);
        formats.put("plain", plainFmt);
        formats.put("general", NumberFormat.getNumberInstance());
        formats.put("decimal", DecimalFormat.getNumberInstance());
        formats.put("currency", NumberFormat.getCurrencyInstance());
        formats.put("percentage", NumberFormat.getPercentInstance());
        formats.put("date", DateFormat.getDateInstance());
        formats.put("datetime", DateFormat.getDateTimeInstance());
        formats.put("time", DateFormat.getInstance());
    }

    public static TabularReportColumn createReportColumn(ResultSetMetaData rsmd, int resultSetColIndex) throws SQLException
    {
        TabularReportColumn column = null;

        int dataType = rsmd.getColumnType(resultSetColIndex);
        switch (dataType)
        {
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.BIGINT:
            case Types.TINYINT:
            case Types.BIT:
                column = new NumericColumn();
                break;

            case Types.FLOAT:
            case Types.REAL:
                column = new DecimalColumn();
                break;

            case Types.NUMERIC:
            case Types.DECIMAL:
                if (rsmd.getScale(resultSetColIndex) > 0)
                    column = new DecimalColumn();
                else
                    column = new NumericColumn();
                break;

            default:
                column = new GeneralColumn();
                break;
        }

        column.setColIndex(resultSetColIndex - 1);
        column.setHeading(new StaticValueSource(TextUtils.getInstance().sqlIdentifierToText(rsmd.getColumnName(resultSetColIndex), true)));
        column.setDataType(dataType);
        column.setWidth(rsmd.getColumnDisplaySize(resultSetColIndex));

        return column;
    }

    public static void addFormat(String fmtSpec, Format fmt)
    {
        formats.put(fmtSpec, fmt);
    }

    public static Format getFormat(String fmtSpec)
    {
        return (Format) formats.get(fmtSpec);
    }
}