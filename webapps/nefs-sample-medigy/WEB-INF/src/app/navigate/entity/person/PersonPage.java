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
 * $Id: PersonPage.java,v 1.7 2004-04-27 04:15:22 aye.thu Exp $
 */

package app.navigate.entity.person;

import app.navigate.entity.EntityPage;
import app.navigate.entity.EntityRedirectorPage;
import auto.dal.db.dao.PersonTable;
import com.netspective.axiom.ConnectionContext;
import com.netspective.axiom.schema.column.type.DateColumn;
import com.netspective.axiom.schema.column.type.EnumerationIdRefColumn;
import com.netspective.axiom.schema.column.type.TextColumn;
import com.netspective.commons.value.GenericValue;
import com.netspective.commons.value.PresentationValue;
import com.netspective.commons.value.Value;
import com.netspective.commons.value.ValueContext;
import com.netspective.commons.value.ValueSource;
import com.netspective.commons.value.source.AbstractValueSource;
import com.netspective.commons.value.source.StaticValueSource;
import com.netspective.sparx.navigate.NavigationContext;
import com.netspective.sparx.navigate.NavigationPath;
import com.netspective.sparx.navigate.NavigationTree;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;

public abstract class PersonPage extends EntityPage implements PersonSubtypePage
{
    private static Log log = LogFactory.getLog(EntityRedirectorPage.class);
    private static ValueSource RETAIN_PARAMS = new StaticValueSource(ActivePerson.PARAMNAME_PERSON_ID);

    public class PersonPageState extends EntityPageState
    {
        private ActivePerson activePerson;

        public ActivePerson getActivePerson()
        {
            return activePerson;
        }

        public void setActivePerson(ActivePerson activePerson)
        {
            this.activePerson = activePerson;
        }
    }

    public class PersonPageHeadingValueSource extends AbstractValueSource
    {
        public boolean hasValue(ValueContext vc)
        {
            return true;
        }

        public Value getValue(ValueContext vc)
        {
            return ((PersonPageState) ((NavigationContext) vc).getActiveState()).getActivePerson().getPerson().getCompleteName();
        }

        public PresentationValue getPresentationValue(ValueContext vc)
        {
            return new PresentationValue(getValue(vc));
        }
    }

    public class PersonProfilePageSubHeadingValueSource extends AbstractValueSource
    {
        public boolean hasValue(ValueContext vc)
        {
            return true;
        }

        public Value getValue(ValueContext vc)
        {
            PersonPageState personPageState = ((PersonPageState) ((NavigationContext) vc).getActiveState());
            PersonTable.Record activePerson = personPageState.getActivePerson().getPerson();
            TextColumn.TextColumnValue ssn = activePerson.getSsn();
            DateColumn.DateColumnValue birthDate = activePerson.getBirthDate();


            EnumerationIdRefColumn.EnumerationIdRefValue maritalStatus = activePerson.getMaritalStatusId();
            EnumerationIdRefColumn.EnumerationIdRefValue gender = activePerson.getGenderId();
            TextColumn.TextColumnValue ethnicityId = activePerson.getEthnicityId();
            TextColumn.TextColumnValue languageId = activePerson.getLanguageId();
            // ASSUMING one ethnicity and one language
            String ethnicity = "";
            String language = "";
            /*
            if (ethnicityId != null && ethnicityId.getReferencedEnumRow() != null)
                ethnicity = ethnicityId.getReferencedEnumRow().getCaption();
            if (languageId != null && languageId.getReferencedEnumRow() != null)
                language = languageId.getReferencedEnumRow().getCaption();
             */

            return new GenericValue("<table class=\"sub-heading-items-table\">\n" +
                    "<tr><td class=\"sub-heading-item\"><span class=\"sub-heading-item-caption\">Category:</span> " + getEntitySubTypeName() + "</td>" +
                    "<td class=\"sub-heading-item\"><span class=\"sub-heading-item-caption\">SSN:</span> " +
                    ssn.getTextValueOrBlank() + "</td>" +
                    "<td class=\"sub-heading-item\"><span class=\"sub-heading-item-caption\">Birth Date:</span> " +
                    birthDate.getTextValueOrBlank() + "</td> " +
                    "<td class=\"sub-heading-item\"><span class=\"sub-heading-item-caption\">Marital Status:</span> " +
                    maritalStatus.getReferencedEnumRow().getCaption() + "</td>" +
                    "<td class=\"sub-heading-item\"><span class=\"sub-heading-item-caption\">Gender:</span> " +
                    gender.getReferencedEnumRow().getCaption() + "</td>" +
                    "<td class=\"sub-heading-item\"><span class=\"sub-heading-item-caption\">Ethnicity:</span> " +
                    ethnicity + "</td>" +
                    "<td class=\"sub-heading-item\"><span class=\"sub-heading-item-caption\">Language:</span> " +
                    language + "</td></tr>\n" +
                    "</table>\n");
        }

        public PresentationValue getPresentationValue(ValueContext vc)
        {
            return new PresentationValue(getValue(vc));
        }
    }

    private ValueSource profile;

    public PersonPage(NavigationTree owner)
    {
        super(owner);
        setRequireRequestParam(ActivePerson.PARAMNAME_PERSON_ID);
        setRetainParams(RETAIN_PARAMS);
        setHeading(new PersonPageHeadingValueSource());
        setSubHeading(new PersonProfilePageSubHeadingValueSource());
    }

    public ValueSource getProfile()
    {
        return profile;
    }

    public void setProfile(ValueSource profile)
    {
        this.profile = profile;
    }

    public NavigationPath.State constructState()
    {
        return new PersonPageState();
    }

    public boolean isValid(NavigationContext nc)
    {
        if(! super.isValid(nc))
            return false;

        ActivePerson activePerson;

        // if we're coming from a redirector then it means that we may not need to rerun our queries
        PersonRedirectorPage.EntitySubtypeRedirectInfo esri = PersonRedirectorPage.getEntitySubtypeRedirectInfo(nc, ActivePerson.getPersonIdParamValue(nc));
        if(esri != null)
            activePerson = (ActivePerson) esri.getData();
        else
        {
            // if we get here it means we need to run our queries to get the active person
            ConnectionContext cc;
            try
            {
                cc = nc.getConnection(null, false);
            }
            catch (Exception e)
            {
                log.error(e);
                throw new NestableRuntimeException(e);
            }

            try
            {
                activePerson = new ActivePerson(nc, cc);
            }
            catch (Exception e)
            {
                log.error(e);
                throw new NestableRuntimeException(e);
            }
            finally
            {
                try
                {
                    cc.close();
                }
                catch (SQLException e)
                {
                    log.error(e);
                    throw new NestableRuntimeException(e);
                }
            }
        }

        ((PersonPageState) nc.getActiveState()).setActivePerson(activePerson);
        return true;
    }
}
