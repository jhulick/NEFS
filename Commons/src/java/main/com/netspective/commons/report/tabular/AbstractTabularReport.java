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
package com.netspective.commons.report.tabular;

import java.net.URLEncoder;
import java.util.List;

import com.netspective.commons.report.tabular.column.GeneralColumn;
import com.netspective.commons.value.ValueSource;
import com.netspective.commons.value.ValueSources;
import com.netspective.commons.xdm.XdmParseContext;
import com.netspective.commons.xdm.XmlDataModelSchema;
import com.netspective.commons.xdm.exception.DataModelException;

public class AbstractTabularReport implements TabularReport, XmlDataModelSchema.ConstructionFinalizeListener
{
    public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);

    static
    {
        XML_DATA_MODEL_SCHEMA_OPTIONS.addIgnoreAttributes(new String[]{"flag"});
    }

    private String name;
    private TabularReportColumns columns = new TabularReportColumns();
    private Flags flags = new Flags();

    public AbstractTabularReport()
    {
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public TabularReportColumns getColumns()
    {
        return columns;
    }

    public TabularReport.Flags createFlags()
    {
        return new Flags();
    }

    public TabularReportColumn getColumn(int i)
    {
        return columns.getColumn(i);
    }

    public TabularReport.Flags getFlags()
    {
        return flags;
    }

    public void setFlags(TabularReport.Flags flags)
    {
        this.flags.copy(flags);
    }

    public void finalizeContents()
    {
        for(int c = 0; c < columns.size(); c++)
        {
            TabularReportColumn colDefn = columns.getColumn(c);
            colDefn.finalizeContents(this);

            if(colDefn.getFlags().flagIsSet(TabularReportColumn.Flags.HAS_PLACEHOLDERS))
                flags.setFlag(Flags.HAS_PLACE_HOLDERS);
        }
    }

    public TabularReportColumn createColumn()
    {
        return new GeneralColumn();
    }

    public void addColumn(TabularReportColumn column)
    {
        column.setColIndex(columns.size());
        columns.add(column);
    }

    public void finalizeConstruction(XdmParseContext pc, Object element, String elementName) throws DataModelException
    {
        // if a record add caption/url was provided but no banner was created, go ahead and generate a banner automatically
        // banners are automatically hidden by record-viewer and shown by record-editor skins
        //TODO: move this into Sparx (record editor skin)
/*
        if(frame != null && banner == null)
        {
            ValueSource recordAddCaption = frame.getRecordAddCaption();
            ValueSource recordAddUrl = frame.getRecordAddUrl();
            if(recordAddCaption != null && recordAddUrl != null)
            {
                banner = new TabularReportBanner();
                banner.addItem(new ReportBanner.Item(recordAddCaption, recordAddUrl, ValueSources.getInstance().getValueSource("config-expr:${sparx.shared.images-url}/design/action-edit-add.gif", ValueSources.VSNOTFOUNDHANDLER_THROW_EXCEPTION)));
            }
        }
*/

        finalizeContents();
    }

    /**
     * Replace contents from rowData using the String row as a template. Each
     * occurrence of ${#} will be replaced with rowNum and occurrences of ${x}
     * where x is a number between 0 and rowData.length will be replaced with
     * the contents of rowData[x]. TODO: this function needs to be
     * improved from both an elegance and performance perspective.
     */

    public String replaceOutputPatterns(TabularReportValueContext rc, TabularReportDataSource ds, final String source)
    {
        StringBuffer sb = new StringBuffer();
        int prev = 0, lastCharPos = source.length() - 1;
        boolean encode = false;
        boolean encrypt = false;

        int pos = source.indexOf("{", prev);
        if(pos > 0 && pos < lastCharPos)
        {
            switch(source.charAt(pos - 1))
            {
                case '$':
                    encode = false;
                    encrypt = false;
                    pos--;
                    break;
                case '%':
                    encode = true;
                    encrypt = false;
                    pos--;
                    break;
                case '^':
                    encode = false;
                    encrypt = true;
                    pos--;
                    break;
                default:
                    pos = -1;
            }
        }
        else
            pos = -1;

        while(pos >= 0)
        {
            if(pos > 0)
            {
                // append the substring before the '$' or '%' character
                sb.append(source.substring(prev, pos));
            }

            int endExpr = source.indexOf('}', pos);
            if(endExpr < 0)
            {
                throw new RuntimeException("Syntax error in: " + source);
            }
            String expression = source.substring(pos + 2, endExpr);

            if(expression.equals("#"))
                sb.append(ds.getActiveRowNumber());
            else
            {
                try
                {
                    int colIndex = Integer.parseInt(expression);
                    if(encrypt)
                    {
                        ValueSource vs = ValueSources.getInstance().getValueSource("encrypt:" + columns.getColumn(colIndex).getFormattedData(rc, ds, TabularReportColumn.GETDATAFLAG_FOR_URL), ValueSources.VSNOTFOUNDHANDLER_NULL);
                        if(vs == null)
                            sb.append("Invalid: '" + expression + "'");
                        else
                            sb.append(URLEncoder.encode(vs.getTextValue(rc)));
                    }
                    else if(encode)
                        sb.append(URLEncoder.encode(columns.getColumn(colIndex).getFormattedData(rc, ds, TabularReportColumn.GETDATAFLAG_FOR_URL)));
                    else
                        sb.append(columns.getColumn(colIndex).getFormattedData(rc, ds, TabularReportColumn.GETDATAFLAGS_DEFAULT));
                }
                catch(NumberFormatException e)
                {
                    ValueSource vs = ValueSources.getInstance().getValueSource(expression, ValueSources.VSNOTFOUNDHANDLER_NULL);
                    if(vs == null)
                        sb.append("Invalid: '" + expression + "'");
                    else
                        sb.append(vs.getTextValue(rc));
                }
            }

            prev = endExpr + 1;

            pos = source.indexOf("{", prev);
            if(pos > 0 && pos < lastCharPos)
            {
                switch(source.charAt(pos - 1))
                {
                    case '$':
                        encode = false;
                        encrypt = false;
                        pos--;
                        break;
                    case '%':
                        encode = true;
                        encrypt = false;
                        pos--;
                        break;
                    case '^':
                        encode = false;
                        encrypt = true;
                        pos--;
                        break;
                    default:
                        pos = -1;
                }
            }
            else
                pos = -1;
        }

        if(prev < source.length()) sb.append(source.substring(prev));
        return sb.toString();
    }

    public void makeStateChanges(TabularReportValueContext rc, TabularReportDataSource ds)
    {
        List listeners = rc.getListeners();
        for(int i = 0; i < listeners.size(); i++)
            ((TabularReportContextListener) listeners.get(i)).makeReportStateChanges(rc, ds);
    }
}