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

import java.text.Format;
import java.util.List;

import com.netspective.commons.command.Command;
import com.netspective.commons.command.CommandNotFoundException;
import com.netspective.commons.report.tabular.calc.ColumnDataCalculator;
import com.netspective.commons.value.ValueSource;
import com.netspective.commons.value.source.RedirectValueSource;
import com.netspective.commons.xdm.XdmBitmaskedFlagsAttribute;
import com.netspective.commons.xdm.XdmEnumeratedAttribute;

/**
 * Interface for handling the report columns.
 */
public interface TabularReportColumn
{
    public static final Flags.FlagDefn[] FLAG_DEFNS = new Flags.FlagDefn[]
    {
        new Flags.FlagDefn(Flags.ACCESS_XDM, "HIDDEN", Flags.HIDDEN, "If set, the column is not displayed in the report."),
        new Flags.FlagDefn(Flags.ACCESS_XDM, "HAS_COMMAND", Flags.HAS_COMMAND),
        new Flags.FlagDefn(Flags.ACCESS_PRIVATE, "HAS_PLACEHOLDERS", Flags.HAS_PLACEHOLDERS),
        new Flags.FlagDefn(Flags.ACCESS_PRIVATE, "HAS_OUTPUT_PATTERN", Flags.HAS_OUTPUT_PATTERN),
        new Flags.FlagDefn(Flags.ACCESS_PRIVATE, "HAVE_CONDITIONALS", Flags.HAVE_CONDITIONALS),
        new Flags.FlagDefn(Flags.ACCESS_XDM, "PREVENT_WORD_WRAP", Flags.PREVENT_WORD_WRAP, "If set, the wordwrap option is turned off for the data in this column.  Otherwise, the column data is wrapped to fit the column width."),
        new Flags.FlagDefn(Flags.ACCESS_XDM, "ALLOW_SORT", Flags.ALLOW_SORT),
        new Flags.FlagDefn(Flags.ACCESS_XDM, "SORTED_ASCENDING", Flags.SORTED_ASCENDING, "If set, a link image is displayed, in the column heading, for sorting the report in ascending order based on this column."),
        new Flags.FlagDefn(Flags.ACCESS_XDM, "SORTED_DESCENDING", Flags.SORTED_DESCENDING, "If set, a link image is displayed, in the column heading, for sorting the report in descending order based on this column.")
    };

    public class Flags extends XdmBitmaskedFlagsAttribute
    {
        public static final int HIDDEN = 1;
        public static final int HAS_COMMAND = HIDDEN * 2;
        public static final int HAS_PLACEHOLDERS = HAS_COMMAND * 2;
        public static final int HAS_OUTPUT_PATTERN = HAS_PLACEHOLDERS * 2;
        public static final int HAVE_CONDITIONALS = HAS_OUTPUT_PATTERN * 2;
        public static final int PREVENT_WORD_WRAP = HAVE_CONDITIONALS * 2;
        public static final int ALLOW_SORT = PREVENT_WORD_WRAP * 2;
        public static final int SORTED_ASCENDING = ALLOW_SORT * 2;
        public static final int SORTED_DESCENDING = SORTED_ASCENDING * 2;
        public static final int ESCAPE_HTML = SORTED_DESCENDING * 2;
        public static final int START_CUSTOM = ESCAPE_HTML * 2;

        public FlagDefn[] getFlagsDefns()
        {
            return FLAG_DEFNS;
        }
    }

    static public final int GETDATAFLAGS_DEFAULT = 0;
    static public final int GETDATAFLAG_DO_CALC = 1;
    static public final int GETDATAFLAG_FOR_URL = GETDATAFLAG_DO_CALC * 2;

    static public final int ALIGN_LEFT = 0;
    static public final int ALIGN_CENTER = 1;
    static public final int ALIGN_RIGHT = 2;

    static public final String PLACEHOLDER_COLDATA = "${.}";
    static public final String PLACEHOLDER_OPEN = "${";
    static public final String PLACEHOLDER_CLOSE = "}";

    static public class AlignStyle extends XdmEnumeratedAttribute
    {
        public AlignStyle()
        {
        }

        public AlignStyle(int valueIndex)
        {
            super(valueIndex);
        }

        public String[] getValues()
        {
            return new String[]{"left", "center", "right"};
        }
    }

    public TabularReportColumnState constructState(TabularReportValueContext rc);

    public int getDataType();

    public void setDataType(int value);

    public boolean isColIndexSet();

    public int getColIndex();

    public void setColIndex(int value);

    public ValueSource getHeading();

    /**
     * Sets the heading of the column.
     *
     * @param value value source object containing the column heading
     */
    public void setHeading(ValueSource value);

    public Command getHeadingCommand();

    public void setHeadingCommand(String command) throws CommandNotFoundException;

    public RedirectValueSource getRedirect();

    /**
     * Associates a redirect value (URL/URI) with the column data. When a value
     * in this column is clicked by the user, the page is redirected to this associated
     * URL/URI.
     *
     * @param redirect value source object containing the URL/URI to redirect to
     */
    public void setRedirect(RedirectValueSource redirect);

    public int getWidth();

    /**
     * Sets the column width.
     *
     * @param value column width
     */
    public void setWidth(int value);

    public int getAlign();

    /**
     * Sets the alignment option for column data.
     *
     * @param value alignment style for the column data. For example, right, left, center.
     */
    public void setAlign(AlignStyle value);

    public Flags getFlags();

    public void setFlags(Flags flags);

    public String getCalcCmd();

    public void setCalc(String value);

    public Format getFormatter();

    public void setFormatter(Format value);

    public void setFormat(String value);

    public String getOutput();

    public void setOutput(String value);

    public String resolvePattern(String srcStr);

    public String getBreak();

    public void setBreak(String header);

    public String getFormattedData(TabularReportValueContext rc, TabularReportDataSource ds, int flags);

    public String getFormattedData(TabularReportValueContext rc, ColumnDataCalculator calc);

    public List getConditionals();

    public void importFromColumn(TabularReportColumn rc);

    public void finalizeContents(TabularReport report);
}