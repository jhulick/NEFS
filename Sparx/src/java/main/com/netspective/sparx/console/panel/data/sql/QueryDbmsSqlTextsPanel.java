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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.netspective.axiom.sql.DbmsSqlText;
import com.netspective.axiom.sql.DbmsSqlTexts;
import com.netspective.commons.report.tabular.TabularReportColumn;
import com.netspective.commons.report.tabular.TabularReportDataSource;
import com.netspective.commons.report.tabular.column.GeneralColumn;
import com.netspective.commons.text.TextUtils;
import com.netspective.commons.value.source.StaticValueSource;
import com.netspective.sparx.navigate.NavigationContext;
import com.netspective.sparx.panel.HtmlSyntaxHighlightPanel;
import com.netspective.sparx.report.tabular.AbstractHtmlTabularReportDataSource;
import com.netspective.sparx.report.tabular.BasicHtmlTabularReport;
import com.netspective.sparx.report.tabular.HtmlTabularReport;

public class QueryDbmsSqlTextsPanel extends QueryDetailPanel
{
    public static final HtmlTabularReport queryReport = new BasicHtmlTabularReport();

    static
    {
        TabularReportColumn column = new GeneralColumn();
        column.setHeading(new StaticValueSource("DBMS"));
        queryReport.addColumn(column);

        column = new GeneralColumn();
        column.setHeading(new StaticValueSource("SQL Text"));
        queryReport.addColumn(column);
    }

    public QueryDbmsSqlTextsPanel()
    {
        getFrame().setHeading(new StaticValueSource("Query SQL Texts"));
    }

    public boolean affectsNavigationContext(NavigationContext nc)
    {
        return true;
    }

    public TabularReportDataSource createDataSource(NavigationContext nc)
    {
        QueryDetailPanel.SelectedQuery selectedQuery = getSelectedQuery(nc);
        if(selectedQuery.getDataSource() != null)
            return selectedQuery.getDataSource();
        else
        {
            nc.setPageHeading("Static SQL: " + selectedQuery.getQuery().getQualifiedName());
            return new SqlTextDataSource(selectedQuery);
        }
    }

    public HtmlTabularReport getReport(NavigationContext nc)
    {
        return queryReport;
    }

    public class SqlTextDataSource extends AbstractHtmlTabularReportDataSource
    {
        private List rows = new ArrayList();
        private int activeRow = -1;
        private int lastRow;

        public SqlTextDataSource(QueryDetailPanel.SelectedQuery selectedQuery)
        {
            super();
            DbmsSqlTexts texts = selectedQuery.getQuery().getSqlTexts();
            Set availableDbmsIds = new TreeSet(texts.getAvailableDbmsIds());

            for(Iterator i = availableDbmsIds.iterator(); i.hasNext();)
            {
                String dbmsId = (String) i.next();
                rows.add(new Object[]{dbmsId, texts.getByDbmsId(dbmsId)});
            }

            lastRow = rows.size() - 1;
        }

        public int getActiveRowNumber()
        {
            return activeRow;
        }

        public int getTotalRows()
        {
            return rows.size();
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
            switch(columnIndex)
            {
                case 0:
                    return ((Object[]) rows.get(activeRow))[0];

                case 1:
                    DbmsSqlText sqlText = (DbmsSqlText) ((Object[]) rows.get(activeRow))[1];
                    String sql = TextUtils.getInstance().getUnindentedText(sqlText.getSql(reportValueContext));
                    Reader reader = new StringReader(sql);
                    Writer writer = new StringWriter();
                    try
                    {
                        HtmlSyntaxHighlightPanel.emitHtml("sql", reader, writer);
                    }
                    catch(IOException e)
                    {
                        return e.getMessage();
                    }
                    return writer.toString();

                default:
                    return null;
            }
        }
    }
}
