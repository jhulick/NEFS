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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.netspective.axiom.SqlManager;
import com.netspective.axiom.schema.Schema;
import com.netspective.axiom.schema.Schemas;
import com.netspective.axiom.schema.table.TableQueryDefinition;
import com.netspective.axiom.sql.dynamic.QueryDefinition;
import com.netspective.axiom.sql.dynamic.QueryDefinitions;
import com.netspective.commons.report.tabular.TabularReportColumn;
import com.netspective.commons.report.tabular.TabularReportDataSource;
import com.netspective.commons.report.tabular.column.GeneralColumn;
import com.netspective.commons.report.tabular.column.NumericColumn;
import com.netspective.commons.value.source.StaticValueSource;
import com.netspective.sparx.console.panel.data.sql.dynamic.QueryDefnDetailPanel;
import com.netspective.sparx.navigate.NavigationContext;
import com.netspective.sparx.panel.AbstractHtmlTabularReportPanel;
import com.netspective.sparx.report.tabular.AbstractHtmlTabularReportDataSource;
import com.netspective.sparx.report.tabular.BasicHtmlTabularReport;
import com.netspective.sparx.report.tabular.HtmlTabularReport;

public class DynamicQueriesCatalogPanel extends AbstractHtmlTabularReportPanel
{
    public static final HtmlTabularReport catalogReport = new BasicHtmlTabularReport();
    private static final TabularReportColumn queryDefnIdColumn = new GeneralColumn();

    static
    {
        queryDefnIdColumn.setHeading(new StaticValueSource("Query Definition"));
        catalogReport.addColumn(queryDefnIdColumn);

        GeneralColumn column = new NumericColumn();
        column.setHeading(new StaticValueSource("Fields"));
        catalogReport.addColumn(column);

        column = new NumericColumn();
        column.setHeading(new StaticValueSource("Joins"));
        catalogReport.addColumn(column);

        column = new NumericColumn();
        column.setHeading(new StaticValueSource("Selects"));
        catalogReport.addColumn(column);

        column = new NumericColumn();
        column.setHeading(new StaticValueSource("Dialogs"));
        catalogReport.addColumn(column);
    }

    public DynamicQueriesCatalogPanel()
    {
        getFrame().setHeading(new StaticValueSource("Available Dynamic Queries"));
    }

    public TabularReportDataSource createDataSource(NavigationContext nc)
    {
        return new CatalogDataSource(nc.getSqlManager());
    }

    public HtmlTabularReport getReport(NavigationContext nc)
    {
        return catalogReport;
    }

    public class CatalogDataSource extends AbstractHtmlTabularReportDataSource
    {
        private QueryDefinition activeRowQueryDefn;
        private String activeRowHeading;
        private List rows = new ArrayList();
        private int activeRow = -1;
        private int lastRow;
        private TabularReportDataSource.Hierarchy hierarchy = new ActiveHierarchy();

        protected class ActiveHierarchy implements TabularReportDataSource.Hierarchy
        {
            public int getColumn()
            {
                return 0;
            }

            public int getLevel()
            {
                return activeRowQueryDefn != null ? 1 : 0;
            }

            public int getParentRow()
            {
                return -1; //TODO: need to implement this
            }
        }

        public CatalogDataSource(SqlManager sqlManager)
        {
            super();

            QueryDefinitions customQueryDefns = sqlManager.getQueryDefns();
            if(customQueryDefns.size() > 0)
            {
                rows.add("Custom");
                Set sortedNames = new TreeSet(customQueryDefns.getNames());
                for(Iterator i = sortedNames.iterator(); i.hasNext();)
                {
                    String queryDefnName = (String) i.next();
                    rows.add(customQueryDefns.get(queryDefnName));
                }
            }

            Schemas schemas = sqlManager.getSchemas();
            for(int i = 0; i < schemas.size(); i++)
            {
                Schema schema = schemas.get(i);
                QueryDefinitions tableQueryDefns = schema.getQueryDefinitions();
                if(tableQueryDefns.size() > 0)
                {
                    rows.add("Schema '" + schema.getName() + "'");
                    Set sortedNames = new TreeSet(tableQueryDefns.getNames());
                    for(Iterator iter = sortedNames.iterator(); iter.hasNext();)
                    {
                        String queryDefnName = (String) iter.next();
                        rows.add(tableQueryDefns.get(queryDefnName));
                    }
                }
            }

            lastRow = rows.size() - 1;
        }

        public TabularReportDataSource.Hierarchy getActiveHierarchy()
        {
            return hierarchy;
        }

        public boolean isHierarchical()
        {
            return true;
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
            Object item = rows.get(activeRow);
            if(item instanceof QueryDefinition)
            {
                activeRowHeading = null;
                activeRowQueryDefn = (QueryDefinition) item;
            }
            else
            {
                activeRowHeading = (String) item;
                activeRowQueryDefn = null;
            }
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
            if(activeRowHeading != null)
            {
                if(columnIndex == 0)
                    return activeRowHeading;
                else
                    return null;
            }

            switch(columnIndex)
            {
                case 0:
                    StringBuffer href = new StringBuffer("detail?");
                    if(activeRowQueryDefn instanceof TableQueryDefinition)
                        href.append(QueryDefnDetailPanel.REQPARAMNAME_QUERY_DEFN_SOURCE + "=" + "schema," + ((TableQueryDefinition) activeRowQueryDefn).getOwner().getSchema().getName() +
                                    "&" + QueryDefnDetailPanel.REQPARAMNAME_QUERY_DEFN + '=' + activeRowQueryDefn.getName());
                    else
                        href.append(QueryDefnDetailPanel.REQPARAMNAME_QUERY_DEFN + '=' + activeRowQueryDefn.getName());

                    //return reportValueContext.getSkin().constructRedirect(reportValueContext, Commands.getInstance().getCommand("redirect," + href), activeRowQueryDefn.getName(), null, null);
                    return "<a href=\"" + href + "\">" + activeRowQueryDefn.getName() + "</a>";

                case 1:
                    return new Integer(activeRowQueryDefn.getFields().size());

                case 2:
                    return new Integer(activeRowQueryDefn.getJoins().size());

                case 3:
                    return new Integer(activeRowQueryDefn.getSelects().size());

                default:
                    return null;
            }
        }

    }
}

