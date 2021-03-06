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
package com.netspective.axiom.schema.diagram;

import com.netspective.axiom.schema.Column;
import com.netspective.axiom.schema.ColumnValues;
import com.netspective.axiom.schema.ForeignKey;
import com.netspective.axiom.schema.Row;
import com.netspective.axiom.schema.Rows;
import com.netspective.axiom.schema.Table;
import com.netspective.commons.diagram.GraphvizDiagramNode;
import com.netspective.commons.value.Value;

public class SchemaDiagramTableDataNodeGenerator implements GraphvizSchemaDiagramTableNodeGenerator
{
    private String name;

    private int maxRowsToShow = 5;
    private String entityTableAttrs = "BORDER=\"1\" CELLSPACING=\"0\" CELLBORDER=\"0\"";
    private String tableNameBgColor = "bisque";

    public SchemaDiagramTableDataNodeGenerator(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public GraphvizDiagramNode generateTableNode(GraphvizSchemaDiagramGenerator generator, GraphvizSchemaDiagramGeneratorFilter filter, Table table)
    {
        StringBuffer dataRowsHtml = new StringBuffer();
        Rows dataRows = table.getData();
        int colSpan = 0;
        if(dataRows != null && dataRows.size() > 0)
        {
            int count = dataRows.size() > maxRowsToShow ? maxRowsToShow : dataRows.size();
            for(int i = 0; i < count; i++)
            {
                dataRowsHtml.append("<TR>");
                Row row = dataRows.getRow(i);
                ColumnValues values = row.getColumnValues();
                colSpan = values.size();
                for(int j = 0; j < colSpan; j++)
                {
                    Value value = values.getByColumnIndex(j);
                    dataRowsHtml.append("<TD ALIGN=\"LEFT\">");
                    dataRowsHtml.append(value.getTextValueOrDefault(" "));
                    dataRowsHtml.append("</TD>");
                }
                dataRowsHtml.append("</TR>\n");
            }

            if(count < dataRows.size())
                dataRowsHtml.append("        <TR><TD COLSPAN=\"" + colSpan + "\">(Only " + count + " of " + dataRows.size() + " shown)</TD></TR>\n");
        }

        StringBuffer tableNodeLabel = new StringBuffer("<<TABLE " + entityTableAttrs + ">\n");
        tableNodeLabel.append("        <TR><TD COLSPAN=\"" + colSpan + "\" BGCOLOR=\"" + tableNameBgColor + "\">" + table.getName() + "</TD></TR>\n");
        if(dataRowsHtml.length() > 0)
            tableNodeLabel.append(dataRowsHtml);
        tableNodeLabel.append("    </TABLE>>");

        GraphvizDiagramNode result = new GraphvizDiagramNode(generator.getGraphvizDiagramGenerator(), table.getName());
        result.setLabel(tableNodeLabel.toString());
        result.setShape("plaintext");
        result.setFontName("Courier");

        return result;
    }

    public String getEdgeSourceElementAndPort(GraphvizSchemaDiagramGenerator generator, ForeignKey foreignKey)
    {
        final Column firstSourceColumn = foreignKey.getSourceColumns().getFirst();
        return firstSourceColumn.getTable().getName();
    }

    public String getEdgeDestElementAndPort(GraphvizSchemaDiagramGenerator generator, ForeignKey foreignKey)
    {
        final Column firstReferenceColumn = foreignKey.getReference().getColumns().getFirst();
        return firstReferenceColumn.getTable().getName();
    }
}
