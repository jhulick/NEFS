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
package com.netspective.medigy.model.insurance;

import com.netspective.medigy.model.TestCase;
import com.netspective.medigy.model.org.Organization;
import com.netspective.medigy.model.party.Party;
import com.netspective.medigy.model.person.Person;
import com.netspective.medigy.reference.custom.insurance.CoverageType;
import com.netspective.medigy.reference.custom.insurance.InsurancePolicyType;
import com.netspective.medigy.reference.custom.org.OrganizationClassificationType;
import com.netspective.medigy.util.HibernateUtil;

import java.util.Date;

public class TestInsurance extends TestCase
{

    public void testInsurance()
    {
        // create the insurance company
        final Organization blueCross = new Organization();
        blueCross.setOrganizationName("Blue Cross Blue Shield");
        blueCross.addPartyClassification(OrganizationClassificationType.Cache.INSURANCE.getEntity());

        final Person johnDoe = new Person();
        johnDoe.setFirstName("John");
        johnDoe.setLastName("Doe");

        final Person patient = new Person();
        patient.setLastName("Doe");
        patient.setFirstName("Jane");

        HibernateUtil.getSession().save(blueCross);
        HibernateUtil.getSession().save(johnDoe);
        HibernateUtil.commitTransaction();

        final InsurancePolicy individualPolicy = new InsurancePolicy();
        individualPolicy.setType(InsurancePolicyType.Cache.INDIVIDUAL_INSURANCE_POLICY.getEntity());
        individualPolicy.setInsuranceProvider(blueCross);
        individualPolicy.setAgreementDate(new Date());
        individualPolicy.setPolicyHolder(johnDoe);
        individualPolicy.addInsuredDependent(patient);
        individualPolicy.setPolicyNumber("12345");

        HibernateUtil.getSession().save(individualPolicy);
        HibernateUtil.commitTransaction();

        final InsurancePolicy newPolicy = (InsurancePolicy) HibernateUtil.getSession().load(InsurancePolicy.class, individualPolicy.getPolicyId());
        assertEquals("12345", newPolicy.getPolicyNumber());
        assertEquals(InsurancePolicyType.Cache.INDIVIDUAL_INSURANCE_POLICY.getEntity(),
                newPolicy.getType());
        assertEquals(blueCross, newPolicy.getInsuranceProvider());
        assertEquals(johnDoe, newPolicy.getInsuredContractHolder());
        HibernateUtil.closeSession();


    }

    public void testGroupInsurance()
    {

        final Organization acmeCompany = new Organization();
        acmeCompany.setOrganizationName("ACME Company");

        final Organization anthemInsurance = new Organization();
        anthemInsurance.setOrganizationName("Anthem");

        final InsurancePolicy groupPolicy = new InsurancePolicy();
        groupPolicy.setType(InsurancePolicyType.Cache.GROUP_INSURANCE_POLICY.getEntity());
        groupPolicy.setInsuranceProvider(anthemInsurance);
        groupPolicy.setPolicyNumber("12345");

        final Group acmeDevelopers = new Group();
        acmeDevelopers.setInsuredOrganization(acmeCompany);
        acmeDevelopers.setDescription("ACME Developer Group");
        acmeDevelopers.getInsurancePolicies().add(groupPolicy);

        final Enrollment currentYearEnrollment = new Enrollment();
        currentYearEnrollment.setEnrolledDate(new Date());
        currentYearEnrollment.setGroup(acmeDevelopers);

        final CoverageType medicalCoverageType = new CoverageType();
        medicalCoverageType.setCode("Med");
        medicalCoverageType.setLabel("Medical");
        medicalCoverageType.setParty(Party.Cache.SYS_GLOBAL_PARTY.getEntity());

        final CoverageType dentalCoverageType = new CoverageType();
        dentalCoverageType.setCode("Den");
        dentalCoverageType.setLabel("Dental");
        dentalCoverageType.setParty(Party.Cache.SYS_GLOBAL_PARTY.getEntity());


        final EnrollmentElection dentalElection = new EnrollmentElection();
        dentalElection.setCoverageType(dentalCoverageType);
        dentalElection.setEnrollment(currentYearEnrollment);
        currentYearEnrollment.getElections().add(dentalElection);


        Person johnDoe = new Person();
        johnDoe.setLastName("Doe");
        johnDoe.setFirstName("John");

        currentYearEnrollment.setInsuredContractHolder(johnDoe);




    }
}
