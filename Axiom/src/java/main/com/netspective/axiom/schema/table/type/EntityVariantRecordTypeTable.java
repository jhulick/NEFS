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
package com.netspective.axiom.schema.table.type;

import com.netspective.axiom.schema.Column;
import com.netspective.axiom.schema.Row;
import com.netspective.axiom.schema.Rows;
import com.netspective.axiom.schema.Schema;
import com.netspective.axiom.schema.table.BasicTable;
import com.netspective.commons.xdm.XmlDataModelSchema;

public class EntityVariantRecordTypeTable extends BasicTable
{
    public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options();

    static
    {
        XML_DATA_MODEL_SCHEMA_OPTIONS.setIgnorePcData(true);
        XML_DATA_MODEL_SCHEMA_OPTIONS.addAliases("data", new String[]{"types"});
    }

    /* These will be setup by the XDM tags in schema.xml */
    private String typeIdColName;
    private String typeNameColName;
    private String typeJavaClassColName;
    private String typeDefaultColName;
    private String typeDescrColName;

    public EntityVariantRecordTypeTable(Column parentColumn)
    {
        super(parentColumn);
    }

    public EntityVariantRecordTypeTable(Schema schema)
    {
        super(schema);
    }

    public boolean isApplicationTable()
    {
        return false;
    }

    public Row createRow()
    {
        return new EntityVariantRecordTypeTableRow(this, getTypes());
    }

    public Rows createRows()
    {
        return new EntityVariantRecordTypeTableRows(this);
    }

    public EntityVariantRecordTypeTableRows getTypes()
    {
        return (EntityVariantRecordTypeTableRows) getData();
    }

    public String getTypeDefaultColName()
    {
        return typeDefaultColName;
    }

    public void setTypeDefaultColName(String typeDefaultColName)
    {
        this.typeDefaultColName = typeDefaultColName;
    }

    public String getTypeDescrColName()
    {
        return typeDescrColName;
    }

    public void setTypeDescrColName(String typeDescrColName)
    {
        this.typeDescrColName = typeDescrColName;
    }

    public String getTypeIdColName()
    {
        return typeIdColName;
    }

    public void setTypeIdColName(String typeIdColName)
    {
        this.typeIdColName = typeIdColName;
    }

    public String getTypeJavaClassColName()
    {
        return typeJavaClassColName;
    }

    public void setTypeJavaClassColName(String typeJavaClassColName)
    {
        this.typeJavaClassColName = typeJavaClassColName;
    }

    public String getTypeNameColName()
    {
        return typeNameColName;
    }

    public void setTypeNameColName(String typeNameColName)
    {
        this.typeNameColName = typeNameColName;
    }
}