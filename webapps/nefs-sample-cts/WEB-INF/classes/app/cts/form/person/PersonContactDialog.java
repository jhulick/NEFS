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
 * @author Aye Thu
 */

/**
 * $Id: PersonContactDialog.java,v 1.2 2003-10-22 06:48:21 aye.thu Exp $
 */
package app.cts.form.person;

import com.netspective.sparx.form.DialogContext;
import com.netspective.sparx.form.DialogContextUtils;
import com.netspective.sparx.form.field.type.DateTimeField;
import com.netspective.sparx.Project;
import com.netspective.axiom.sql.Query;
import com.netspective.axiom.sql.QueryResultSet;
import com.netspective.axiom.sql.ResultSetUtils;
import com.netspective.axiom.ConnectionContext;
import com.netspective.commons.value.GenericValue;

import javax.servlet.http.HttpServletRequest;
import javax.naming.NamingException;
import java.sql.SQLException;
import java.util.Map;
import java.io.IOException;
import java.io.Writer;

import auto.dal.db.DataAccessLayer;
import auto.dal.db.vo.PersonContact;
import auto.dal.db.vo.impl.PersonContactVO;
import auto.dal.db.dao.person.PersonContactTable;
import auto.dal.db.dao.PersonTable;
import auto.dcb.cts.PersonContactInfoContext;
import auto.id.sql.schema.db.enum.RecordStatus;
import app.cts.AppAuthenticatedUser;


/**
 * Class for handling action of adding/updating/deleting contact information
 */
public class PersonContactDialog extends com.netspective.sparx.form.Dialog
{
    public PersonContactDialog(Project project)
    {
        super(project);
    }

    public void populateValues(DialogContext dc, int formatType)
    {
        super.populateValues(dc, formatType);
        if(dc.getAuthenticatedUser() == null)
            return;

        HttpServletRequest request = dc.getHttpRequest();
        String idParam = request.getParameter("id");
        if(dc.isInitialEntry() && (dc.editingData() || dc.deletingData()))
        {
            DialogContext.DialogFieldStates fieldStates = dc.getFieldStates();
            Query query = dc.getSqlManager().getQuery(auto.id.sql.query.Cts.GET_CONTACT_INFO_BY_ID);
            try
            {
                QueryResultSet qrs = query.execute(dc, new Object[] { idParam }, false);
                Map responses = ResultSetUtils.getInstance().getResultSetSingleRowAsMap(qrs.getResultSet());

                fieldStates.getState("contactType.method_type").getValue().setValue(responses.get("method_type"));
                fieldStates.getState("method_value").getValue().setValue(responses.get("method_value"));
                qrs.close(true);
                qrs = null;
            }
            catch (NamingException e)
            {
                e.printStackTrace();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param writer
     * @param dc
     * @throws IOException
     */
    public void execute(Writer writer, DialogContext dc) throws IOException
    {
        ConnectionContext cc = null;
        try
        {
            // get a connection from the default data source
            cc = dc.getConnection(null, true);
             // get the dialog context bean specific to our dialog so we can retrieve values
            PersonContactInfoContext pcic = new PersonContactInfoContext(dc);

            // grab the singleton  data access object (DAO)
            PersonTable personTable = DataAccessLayer.getInstance().getPersonTable();
            PersonContactTable pcTable = personTable.getPersonContactTable();
            try
            {
                if (dc.addingData())
                {
                    PersonContact pContact = new PersonContactVO();
                    pContact.setRecStatId(new Integer(RecordStatus.ACTIVE));
                    pContact.setMethodValue(pcic.getMethodValue().getTextValue());
                    pContact.setMethodType(Integer.valueOf(pcic.getContactType_methodType().getTextValue()));
                    String personId = (String) ((AppAuthenticatedUser) dc.getAuthenticatedUser()).getAttribute("person_id");
                    pContact.setParentId(Long.valueOf(personId));

                    PersonContactTable.Record record = pcTable.createRecord();
                    record.setValues(pContact);
                    record.insert(cc);
                    cc.getConnection().commit();
                }
                else if (dc.editingData())
                {
                    PersonContactTable.Record record = pcTable.getRecordByPrimaryKey(cc, pcic.getId().getTextValue());
                    //DialogContextUtils.getInstance().populateRowWithFieldValues(dc, record.getRow());
                    record.setMethodValue(pcic.getMethodValue());
                    record.update(cc);
                    cc.getConnection().commit();
                }
                else if (dc.deletingData())
                {
                    PersonContactTable.Record record = pcTable.getRecordByPrimaryKey(cc, pcic.getId().getTextValue());
                    record.delete(cc);
                    cc.getConnection().commit();
                }
            }
            catch (SQLException se)
            {
                // failed to add/edit/delete record
                se.printStackTrace();
                getLog().error("Error in data perspective " + dc.getPerspectives(), se);
                handlePostExecuteException(writer, dc, "Error in data perspective, rolling back.", se);
                try
                {
                    cc.getConnection().rollback();
                }
                catch (Exception e)
                {
                    handlePostExecuteException(writer, dc, "Unable to rollback after error", e);
                }
                return;
            }
            handlePostExecute(writer, dc);

        }
        catch (Exception e)
        {
            // failed to get a database connection
            e.printStackTrace();
            handlePostExecuteException(writer, dc, "Unable to get connection", e);
        }
        finally
        {
            try
            {
                if(cc != null)
                    cc.close();
            }
            catch (SQLException se)
            {
                handlePostExecuteException(writer, dc, "Unable to close connection", se);
            }
        }
    }
}
