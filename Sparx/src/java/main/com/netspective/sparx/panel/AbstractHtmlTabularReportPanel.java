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
 * $Id: AbstractHtmlTabularReportPanel.java,v 1.19 2003-08-31 15:29:13 shahid.shah Exp $
 */

package com.netspective.sparx.panel;

import java.io.Writer;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.netspective.sparx.navigate.NavigationContext;
import com.netspective.sparx.report.tabular.HtmlTabularReportSkin;
import com.netspective.sparx.report.tabular.HtmlTabularReportValueContext;
import com.netspective.sparx.report.tabular.AbstractHtmlTabularReportDataSource;
import com.netspective.sparx.report.tabular.HtmlTabularReport;
import com.netspective.sparx.report.tabular.BasicHtmlTabularReport;
import com.netspective.sparx.report.tabular.HtmlTabularReportDataSourceScrollStates;
import com.netspective.sparx.report.tabular.HtmlTabularReportDataSourceScrollState;
import com.netspective.sparx.theme.Theme;
import com.netspective.sparx.form.DialogContext;
import com.netspective.sparx.form.field.type.DataSourceNavigatorButtonsField;
import com.netspective.commons.value.ValueSource;
import com.netspective.commons.value.source.StaticValueSource;
import com.netspective.commons.report.tabular.column.GeneralColumn;
import com.netspective.commons.report.tabular.TabularReportColumn;
import com.netspective.commons.report.tabular.TabularReportDataSource;
import com.netspective.commons.report.tabular.TabularReportDataSourceScrollState;

public abstract class AbstractHtmlTabularReportPanel extends AbstractPanel implements HtmlTabularReportPanel
{
    private boolean scrollable;         // indicates if the report is pageable
    private boolean selectable;         // indicates if the report is selectable
    private int scrollRowsPerPage = 25; // the rows per page count when the report is pageable
    private String reportSkin;          // the skin to use to display the report

    public AbstractHtmlTabularReportPanel()
    {
    }

    public boolean isSelectable()
    {
        return selectable;
    }

    public void setSelectable(boolean selectable)
    {
        this.selectable = selectable;
    }

    public boolean isScrollable()
    {
        return scrollable;
    }

    public void setScrollable(boolean scrollable)
    {
        this.scrollable = scrollable;
    }

    public int getScrollRowsPerPage()
    {
        return scrollRowsPerPage;
    }

    public void setScrollRowsPerPage(int scrollRowsPerPage)
    {
        this.scrollRowsPerPage = scrollRowsPerPage;
    }

    public String getReportSkin()
    {
        return reportSkin;
    }

    public void setReportSkin(String reportSkin)
    {
        this.reportSkin = reportSkin;
    }



    public HtmlTabularReportValueContext createContext(NavigationContext nc, HtmlTabularReportSkin skin)
    {
        return new HtmlTabularReportValueContext(nc.getServlet(), nc.getRequest(), nc.getResponse(), this, getReport(nc), skin);
    }

    public HtmlTabularReportValueContext createContext(NavigationContext nc, HtmlTabularReportSkin skin, TabularReportDataSourceScrollState scrollState)
    {
        return new HtmlTabularReportValueContext(nc.getServlet(), nc.getRequest(), nc.getResponse(), this, (HtmlTabularReport) scrollState.getReport(), skin);
    }

    public void render(Writer writer, NavigationContext nc, Theme theme, int flags) throws IOException
    {
        HtmlTabularReportValueContext vc = createContext(nc, reportSkin != null ? theme.getReportSkin(reportSkin) : theme.getDefaultReportSkin());
        vc.setPanelRenderFlags(flags);
        TabularReportDataSource ds = createDataSource(nc);
        vc.produceReport(writer, ds);
        ds.close();
    }

    /**
     * Render the html tabular report panel
     * @param writer
     * @param dc
     * @param theme
     * @param flags
     * @throws IOException
     */
    public void render(Writer writer, DialogContext dc, Theme theme, int flags) throws IOException
    {
        if(isScrollable())
        {
            // TODO: Need to add handling of selection of report rows
            HtmlTabularReportDataSourceScrollStates scrollStates = HtmlTabularReportDataSourceScrollStates.getInstance();
            HtmlTabularReportDataSourceScrollState scrollState = scrollStates.getScrollStateByDialogTransactionId(dc);
            HtmlTabularReportValueContext vc = null;

            if(scrollState != null)
            {
                // reuse the scroll state object
                vc = createContext(dc.getNavigationContext(), theme.getDefaultReportSkin(), scrollState);
                vc.setDialogContext(dc);
                vc.setPanelRenderFlags(flags);
                vc.setResultsScrolling(scrollState);
                //vc.produceReport(writer, scrollState.getDataSource());
            }
            else
            {
                // no existing scroll state object associated with current dialog transaction ID
                // so create a new scroll state object
                vc = createContext(dc.getNavigationContext(), theme.getDefaultReportSkin());
                vc.setDialogContext(dc);
                vc.setPanelRenderFlags(flags);
                TabularReportDataSource ds = createDataSource(dc.getNavigationContext());
                scrollState = (HtmlTabularReportDataSourceScrollState) ds.createScrollState(dc.getTransactionId());
                scrollState.setPanel(this);
                scrollState.setRowsPerPage(getScrollRowsPerPage());
                scrollState.setReport(vc.getReport());
                // save the scroll state as a session attribute based on the dialog context transaction id
                scrollStates.setActiveScrollState(dc, scrollState);
                vc.setResultsScrolling(scrollState);
                //vc.produceReport(writer, ds);
                // don't close the data source -- we're scrolling
            }


            // now that we've got our scroll state, see if the user is requesting us to move to another page
            HttpServletRequest request = dc.getHttpRequest();
            if (request.getParameter(DataSourceNavigatorButtonsField.RSNAV_BUTTONNAME_NEXT) != null)
            {
                scrollState.setPageDelta(1);
            }
            else if (request.getParameter(DataSourceNavigatorButtonsField.RSNAV_BUTTONNAME_PREV) != null)
            {
                scrollState.setPageDelta(-1);
            }
            else if (request.getParameter(DataSourceNavigatorButtonsField.RSNAV_BUTTONNAME_LAST) != null)
            {
                scrollState.setPageLast();
            }
            else if (request.getParameter(DataSourceNavigatorButtonsField.RSNAV_BUTTONNAME_FIRST) != null)
            {
                scrollState.setPageFirst();
            }
            vc.produceReport(writer, scrollState.getDataSource());
        }
        else
        {
            // not scrollable
            HtmlTabularReportValueContext vc = null;
            vc = createContext(dc.getNavigationContext(), (reportSkin == null || theme.getReportSkin(reportSkin) == null )? theme.getDefaultReportSkin() : theme.getReportSkin(reportSkin));
            vc.setDialogContext(dc);
            vc.setPanelRenderFlags(flags);
            TabularReportDataSource ds = createDataSource(dc.getNavigationContext());
            vc.produceReport(writer, ds);
            ds.close();
        }

    }

    public class SimpleMessageDataSource extends AbstractHtmlTabularReportDataSource
    {
        private ValueSource message;

        public SimpleMessageDataSource(String message)
        {
            super();
            this.message = new StaticValueSource(message);
        }

        public SimpleMessageDataSource(ValueSource message)
        {
            super();
            this.message = message;
        }

        public ValueSource getNoDataFoundMessage()
        {
            return message;
        }

        public int getTotalRows()
        {
            return 0;
        }

        public boolean hasMoreRows()
        {
            return false;
        }

        public boolean isScrollable()
        {
            return false;
        }

        public boolean next()
        {
            return false;
        }

        public void setActiveRow(int rowNum)
        {
        }
    }

    public static final HtmlTabularReport constructReportFromList(List list)
    {
        if(list == null || list.size() == 0)
            throw new RuntimeException("List has no contents.");

        List firstRow = (List) list.get(0);

        HtmlTabularReport result = new BasicHtmlTabularReport();
        for(int i = 0; i < firstRow.size(); i++)
        {
            TabularReportColumn column = new GeneralColumn();
            Object value = firstRow.get(0);
            if(value instanceof ValueSource)
                column.setHeading((ValueSource) value);
            else if(value != null)
                column.setHeading(new StaticValueSource(value.toString()));
        }

        return result;
    }

    public class ListDataSource extends SimpleMessageDataSource
    {
        protected int activeRowIndex = -1;
        protected int lastRowIndex;
        private List list;

        public ListDataSource(String message)
        {
            super(message);
        }

        public ListDataSource(ValueSource message)
        {
            super(message);
        }

        public List getList()
        {
            return list;
        }

        public void setList(List list)
        {
            this.list = list;
            activeRowIndex = -1;
            lastRowIndex = list.size() - 1;
        }

        public Object getActiveRowColumnData(int columnIndex, int flags)
        {
            List activeRow = (List) list.get(activeRowIndex);
            return activeRow.get(columnIndex);
        }

        public boolean next()
        {
            if(activeRowIndex < lastRowIndex)
            {
                activeRowIndex++;
                return true;
            }

            return false;
        }

        public int getActiveRowNumber()
        {
            return activeRowIndex + 1;
        }
    }

}
