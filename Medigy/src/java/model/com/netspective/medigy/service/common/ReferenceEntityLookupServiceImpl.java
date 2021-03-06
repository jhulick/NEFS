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
 */
package com.netspective.medigy.service.common;

import com.netspective.medigy.model.party.PartyRole;
import com.netspective.medigy.reference.custom.party.ContactMechanismPurposeType;
import com.netspective.medigy.reference.custom.person.EthnicityType;
import com.netspective.medigy.reference.custom.person.PersonRoleType;
import com.netspective.medigy.reference.custom.insurance.InsurancePolicyType;
import com.netspective.medigy.reference.type.GenderType;
import com.netspective.medigy.reference.type.LanguageType;
import com.netspective.medigy.reference.type.MaritalStatusType;
import com.netspective.medigy.util.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;

public class ReferenceEntityLookupServiceImpl implements ReferenceEntityLookupService
{

    public InsurancePolicyType getInsurancePolicyType(final String code) throws UnknownReferenceTypeException
    {
        InsurancePolicyType type = InsurancePolicyType.Cache.getEntity(code);
        if (type == null)
        {
            final Criteria criteria = HibernateUtil.getSession().createCriteria(InsurancePolicyType.class);
            criteria.add(Restrictions.eq("code", code));
            type = (InsurancePolicyType) criteria.uniqueResult();
            if (type == null)
                throw new UnknownReferenceTypeException();
        }
        return type;
    }

    /**
     * Gets the language type based on the code
     *
     * @param code      Language code
     * @return
     */
    public LanguageType getLanguageType(final String code) throws UnknownReferenceTypeException
    {
        final LanguageType type = LanguageType.Cache.getEntity(code);
        if (type == null)
            throw new UnknownReferenceTypeException();
        return type;
    }

    /**
     * Gets the gender type based on the code
     * @param genderCode
     * @return
     */
    public GenderType getGenderType(final String genderCode) throws UnknownReferenceTypeException
    {
        final GenderType type = GenderType.Cache.getEntity(genderCode);
        if (type == null)
            throw new UnknownReferenceTypeException();
        return type;
    }

    /**
     * Gets the ethnicity type based on the code. This will look at static cached items
     * first and only when no match is found it will query the underlying DB to get the
     * reference entity.
     *
     * @param ethnicityCode
     * @return
     * @throws UnknownReferenceTypeException
     */
    public EthnicityType getEthnicityType(final String ethnicityCode) throws UnknownReferenceTypeException
    {
        EthnicityType type = EthnicityType.Cache.getEntity(ethnicityCode);
        if (type == null)
        {
            final Criteria criteria = HibernateUtil.getSession().createCriteria(EthnicityType.class);
            criteria.add(Expression.eq("code", ethnicityCode));
            type = (EthnicityType) criteria.uniqueResult();
            if (type == null)
                throw new UnknownReferenceTypeException();
        }
        return type;
    }

    /**
     * Gets the marital status type based on the code.
     * @param statusCode
     * @return
     * @throws UnknownReferenceTypeException
     */
    public MaritalStatusType getMaritalStatusType(String statusCode) throws UnknownReferenceTypeException
    {
        final MaritalStatusType type = MaritalStatusType.Cache.getEntity(statusCode);
        if (type == null)
            throw new UnknownReferenceTypeException();
        return type;
    }

    public PartyRole getPersonRole(String roleCode) throws UnknownReferenceTypeException
    {
        PersonRoleType  type = PersonRoleType.Cache.getEntity(roleCode);
        if (type == null)
        {
            final Criteria criteria = HibernateUtil.getSession().createCriteria(PersonRoleType.class);
            criteria.add(Expression.eq("code", roleCode));
            type = (PersonRoleType) criteria.uniqueResult();
            if (type == null)
                throw new UnknownReferenceTypeException();
        }
        PartyRole role = new PartyRole();
        role.setType(type);
        return role;
    }

    public ContactMechanismPurposeType getContactMechanismPurposeType(String purposeCode) throws UnknownReferenceTypeException
    {
        ContactMechanismPurposeType  type = ContactMechanismPurposeType.Cache.getEntity(purposeCode);
        if (type == null)
        {
            final Criteria criteria = HibernateUtil.getSession().createCriteria(ContactMechanismPurposeType.class);
            criteria.add(Expression.eq("code", purposeCode));
            type = (ContactMechanismPurposeType) criteria.uniqueResult();
            if (type == null)
                throw new UnknownReferenceTypeException();
        }
        return type;
    }
}
