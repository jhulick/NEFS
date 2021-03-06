/*
 * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
 *
 * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
 * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
 * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
 * before using The Software. Any use of The Software indicates agreement with the NSL.
 *
 * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
 *
 * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
 *    containing the .class files) and only as part of an application that uses The Software as part of its primary
 *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
 *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
 *    same restrictions.
 *
 * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
 *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
 *
 * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
 *    used to endorse or appear in products derived from The Software without written consent of Netspective.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
 * ARE HEREBY DISCLAIMED.
 *
 * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
 * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
 * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */
package com.netspective.sparx.console.panel.data.sql;

import com.netspective.axiom.sql.QueryParameter;
import com.netspective.axiom.sql.QueryParameters;
import com.netspective.commons.report.tabular.TabularReportColumn;
import com.netspective.commons.report.tabular.TabularReportDataSource;
import com.netspective.commons.report.tabular.column.GeneralColumn;
import com.netspective.commons.value.ValueSource;
import com.netspective.commons.value.source.StaticValueSource;
import com.netspective.sparx.navigate.NavigationContext;
import com.netspective.sparx.report.tabular.AbstractHtmlTabularReportDataSource;
import com.netspective.sparx.report.tabular.BasicHtmlTabularReport;
import com.netspective.sparx.report.tabular.HtmlTabularReport;

public class QueryParametersPanel extends QueryDetailPanel
{
    public static final HtmlTabularReport queryParamsReport = new BasicHtmlTabularReport();
    protected static final ValueSource noParams = new StaticValueSource("Query has no parameters.");

    static
    {
        TabularReportColumn column = new GeneralColumn();
        column.setHeading(new StaticValueSource("Index"));
        queryParamsReport.addColumn(column);

        column = new GeneralColumn();
        column.setHeading(new StaticValueSource("Name"));
        queryParamsReport.addColumn(column);

        column = new GeneralColumn();
        column.setHeading(new StaticValueSource("JDBC Type"));
        queryParamsReport.addColumn(column);

        column = new GeneralColumn();
        column.setHeading(new StaticValueSource("Java Type"));
        queryParamsReport.addColumn(column);

        column = new GeneralColumn();
        column.setHeading(new StaticValueSource("Value Source"));
        queryParamsReport.addColumn(column);

        column = new GeneralColumn();
        column.setHeading(new StaticValueSource("Value Source Class"));
        queryParamsReport.addColumn(column);
    }

    public QueryParametersPanel()
    {
        getFrame().setHeading(new StaticValueSource("Query Parameters"));
    }

    public TabularReportDataSource createDataSource(NavigationContext nc)
    {
        QueryDetailPanel.SelectedQuery selectedQuery = getSelectedQuery(nc);
        if(selectedQuery.getDataSource() != null)
            return selectedQuery.getDataSource();
        else
            return new SqlParamsDataSource(selectedQuery);
    }

    public HtmlTabularReport getReport(NavigationContext nc)
    {
        return queryParamsReport;
    }

    public class SqlParamsDataSource extends AbstractHtmlTabularReportDataSource
    {
        private QueryParameters params;
        private int activeRow = -1;
        private int lastRow;

        public SqlParamsDataSource(QueryDetailPanel.SelectedQuery selectedQuery)
        {
            super();
            params = selectedQuery.getQuery().getParams();
            if(params != null)
                lastRow = params.size() - 1;
            else
                lastRow = -1;
        }

        public int getActiveRowNumber()
        {
            return activeRow;
        }

        public int getTotalRows()
        {
            return params.size();
        }

        public boolean hasMoreRows()
        {
            return activeRow < lastRow;
        }

        public boolean isScrollable()
        {
            return true;
        }

        public void setActiveRow(int rowNum)
        {
            activeRow = rowNum;
        }

        public boolean next()
        {
            if(!hasMoreRows())
                return false;

            setActiveRow(activeRow + 1);
            return true;
        }

        public Object getActiveRowColumnData(int columnIndex, int flags)
        {
            QueryParameter param = params.get(activeRow);

            switch(columnIndex)
            {
                case 0:
                    return new Integer(activeRow + 1);

                case 1:
                    return param.getName();

                case 2:
                    return new Integer(param.getSqlTypeCode());

                case 3:
                    return param.getJavaType();

                case 4:
                    ValueSource vs = param.getValue();
                    if(vs != null)
                        return vs.getSpecification();
                    else
                        return null;

                case 5:
                    vs = param.getValue();
                    if(vs != null)
                        return reportValueContext.getSkin().constructClassRef(vs.getClass());
                    else
                        return null;

                default:
                    return null;
            }
        }

        public ValueSource getNoDataFoundMessage()
        {
            return noParams;
        }
    }
}
