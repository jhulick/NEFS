/*
 * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
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
 *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
 *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
 *    without written consent of Netspective.
 *
 * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
 *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
 * ARE HEREBY DISCLAIMED.
 *
 * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
 * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
 * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * @author Shahid N. Shah
 */

/**
 * $Id: StaticQueryDocumentationPanel.java,v 1.2 2003-04-07 17:13:55 shahid.shah Exp $
 */

package com.netspective.sparx.console.panel.data;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;
import java.io.StringReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.io.IOException;

import com.netspective.sparx.navigate.NavigationContext;
import com.netspective.sparx.panel.AbstractHtmlTabularReportPanel;
import com.netspective.sparx.report.tabular.HtmlTabularReport;
import com.netspective.sparx.report.tabular.BasicHtmlTabularReport;
import com.netspective.sparx.report.tabular.AbstractHtmlTabularReportDataSource;
import com.netspective.sparx.report.tabular.HtmlTabularReportValueContext;
import com.netspective.sparx.panel.HtmlSyntaxHighlightPanel;
import com.netspective.sparx.console.page.StaticQueryPage;
import com.netspective.axiom.sql.Query;
import com.netspective.axiom.sql.DbmsSqlTexts;
import com.netspective.axiom.sql.DbmsSqlText;
import com.netspective.commons.report.tabular.TabularReportDataSource;
import com.netspective.commons.report.tabular.TabularReportColumn;
import com.netspective.commons.report.tabular.TabularReportValueContext;
import com.netspective.commons.report.tabular.column.GeneralColumn;
import com.netspective.commons.value.source.StaticValueSource;
import com.netspective.commons.value.ValueSource;
import com.netspective.commons.text.TextUtils;

public class StaticQueryDocumentationPanel extends AbstractHtmlTabularReportPanel
{
    public static final HtmlTabularReport queryReport = new BasicHtmlTabularReport();
    public static final String REQPARAMNAME_QUERY = "selected-query-id";
    private static final ValueSource noQueryParamAvailSource = new StaticValueSource("No '"+ REQPARAMNAME_QUERY +"' parameter provided.");

    static
    {
        TabularReportColumn column = new GeneralColumn();
        column.setHeading(new StaticValueSource("DBMS"));
        queryReport.addColumn(column);

        column = new GeneralColumn();
        column.setHeading(new StaticValueSource("SQL Text"));
        queryReport.addColumn(column);
    }

    public boolean affectsNavigationContext(NavigationContext nc)
    {
        return true;
    }

    public TabularReportDataSource createDataSource(NavigationContext nc, HtmlTabularReportValueContext vc)
    {
        // the query should be automatically assigned to the state using the "assign-state-params" method
        String queryName = ((StaticQueryPage.State) nc.getActiveState()).getSelectedQueryId();
        if(queryName == null)
            return new NoQueryParameterDataSource(vc);

        Query query = nc.getSqlManager().getQueries().get(queryName);
        if(query == null)
            return new QueryNotFoundDataSource(vc, queryName);

        nc.setPageHeading("Static SQL: " + query.getQualifiedName());
        return new SqlTextDataSource(vc, query);
    }

    public HtmlTabularReport getReport(NavigationContext nc)
    {
        return queryReport;
    }

    public class NoQueryParameterDataSource extends AbstractHtmlTabularReportDataSource
    {
        public NoQueryParameterDataSource(TabularReportValueContext reportValueContext)
        {
            super(reportValueContext);
        }

        public ValueSource getNoDataFoundMessage()
        {
            return noQueryParamAvailSource;
        }
    }

    public class QueryNotFoundDataSource extends AbstractHtmlTabularReportDataSource
    {
        private String queryName;

        public QueryNotFoundDataSource(HtmlTabularReportValueContext vc, String queryName)
        {
            super(vc);
            this.queryName = queryName;
        }

        public ValueSource getNoDataFoundMessage()
        {
            return new StaticValueSource("Query '"+ queryName +"' not found.");
        }
    }

    public class SqlTextDataSource extends AbstractHtmlTabularReportDataSource
    {
        private List rows = new ArrayList();
        private int activeRow = -1;
        private int lastRow;

        public SqlTextDataSource(HtmlTabularReportValueContext vc, Query query)
        {
            super(vc);
            DbmsSqlTexts texts = query.getSqlTexts();
            Set availableDbmsIds = new TreeSet(texts.getAvailableDbmsIds());

            for(Iterator i = availableDbmsIds.iterator(); i.hasNext(); )
            {
                String dbmsId = (String) i.next();
                rows.add(new Object[] { dbmsId, texts.getByDbmsId(dbmsId) });
            }

            lastRow = rows.size() - 1;
        }

        public int getActiveRowNumber()
        {
            return activeRow;
        }

        public boolean next()
        {
            if(activeRow < lastRow)
            {
                activeRow++;
                return true;
            }

            return false;
        }

        public Object getActiveRowColumnData(int columnIndex, int flags)
        {
            switch(columnIndex)
            {
                case 0:
                    return ((Object[]) rows.get(activeRow))[0];

                case 1:
                    DbmsSqlText sqlText = (DbmsSqlText) ((Object[]) rows.get(activeRow))[1];
                    String sql = TextUtils.getUnindentedText(sqlText.getSql());
                    Reader reader = new StringReader(sql);
                    Writer writer = new StringWriter();
                    try
                    {
                        HtmlSyntaxHighlightPanel.emitHtml("sql", reader, writer);
                    }
                    catch (IOException e)
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
