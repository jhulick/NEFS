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

package com.netspective.medigy.model.party;

import com.netspective.medigy.model.common.AbstractTopLevelEntity;
import com.netspective.medigy.model.invoice.BillingAccountRole;
import com.netspective.medigy.model.invoice.InvoiceRole;
import com.netspective.medigy.model.insurance.InsurancePolicyRole;
import com.netspective.medigy.reference.custom.party.CommunicationEventPurposeType;
import com.netspective.medigy.reference.custom.party.CommunicationEventRoleType;
import com.netspective.medigy.reference.custom.party.FacilityType;
import com.netspective.medigy.reference.custom.party.PartyIdentifierType;
import com.netspective.medigy.reference.custom.party.PartyRelationshipType;
import com.netspective.medigy.reference.custom.party.PartyRoleType;
import com.netspective.medigy.reference.custom.party.PartyClassificationType;
import com.netspective.medigy.reference.type.party.PartyType;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratorType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.ManyToOne;
import java.util.HashSet;
import java.util.Set;

@Entity()
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name = "Party")
public class Party extends AbstractTopLevelEntity
{
    public static final String SYS_GLOBAL_PARTY_NAME = "SYS_GLOBAL_PARTY";

    public enum Cache
    {
        SYS_GLOBAL_PARTY();

        private Party entity;

        public Party getEntity()
        {
            if(entity == null)
                throw new RuntimeException(getClass() + " " + name() + " has not been initialized.");

            return entity;
        }

        public void setEntity(Party entity)
        {
            this.entity = entity;
        }
    }

    private Long partyId;
    private String partyName;
    private PartyType partyType;

    private Set<PartyClassification> partyClassifications = new HashSet<PartyClassification>();
    private Set<PartyRole> partyRoles = new HashSet<PartyRole>();
    private Set<PartyRelationship> fromPartyRelationships = new HashSet<PartyRelationship>();
    private Set<PartyRelationship> toPartyRelationships = new HashSet<PartyRelationship>();
    private Set<PartyQualification> partyQualifications = new HashSet<PartyQualification>();

    protected Set<PartyIdentifier> partyIdentifiers = new HashSet<PartyIdentifier>();
    protected Set<PartyContactMechanism> partyContactMechanisms = new HashSet<PartyContactMechanism>();
    private Set<PartyFacilityRole> partyFacilityRoles = new HashSet<PartyFacilityRole>();
    private Set<CommunicationEventRole> communicationEventRoles = new HashSet<CommunicationEventRole>();
    private Set<BillingAccountRole> billingAccountRoles = new HashSet<BillingAccountRole>();
    private Set<InvoiceRole> invoiceRoles = new HashSet<InvoiceRole>();

    private Set<InsurancePolicyRole> insurancePolicyRoles = new HashSet<InsurancePolicyRole>();

    // All the custom reference entity types
    private Set<PartyIdentifierType> partyIdentifierTypes = new HashSet<PartyIdentifierType>();
    private Set<PartyRoleType> partyRoleTypes = new HashSet<PartyRoleType>();
    private Set<FacilityType> facilityTypes = new HashSet<FacilityType>();
    private Set<PartyRelationshipType> partyRelationshipTypes = new HashSet<PartyRelationshipType>();
    private Set<CommunicationEventPurposeType> communicationEventPurposeTypes = new HashSet<CommunicationEventPurposeType>();
    private Set<CommunicationEventRoleType> communicationEventRoleTypes = new HashSet<CommunicationEventRoleType>();

    public Party()
    {
    }

    public Party(final String partyName)
    {
        this.partyName = partyName;
    }

    @Id(generate=GeneratorType.AUTO)
    public Long getPartyId()
    {
        return partyId;
    }

    protected void setPartyId(final Long partyId)
    {
        this.partyId = partyId;
    }

    @Column(length = 100, nullable = false)
    public String getPartyName()
    {
        return this.partyName;
    }

    public void setPartyName(final String partyName)
    {
        this.partyName = partyName;
    }

    @ManyToOne
    @JoinColumn(name = "party_type_id", nullable = true)
    public PartyType getPartyType()
    {
        return partyType;
    }

    public void setPartyType(final PartyType partyType)
    {
        this.partyType = partyType;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "party_id")
    public Set<PartyClassification> getPartyClassifications()
    {
        return partyClassifications;
    }

    public void setPartyClassifications(final Set<PartyClassification> partyClassifications)
    {
        this.partyClassifications = partyClassifications;
    }

    @Transient
    public void addPartyClassification(final PartyClassificationType type)
    {
        final PartyClassification classification = new PartyClassification();
        classification.setType(type);
        classification.setParty(this);
        getPartyClassifications().add(classification);
    }

    @Transient
    public PartyClassification getPartyClassification(final PartyClassificationType type)
    {
        for (PartyClassification pc : getPartyClassifications())
        {
            if (pc.getType().equals(type))
            return pc;
        }
        return null;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "party_id")
    public Set<PartyRole> getPartyRoles()
    {
        return partyRoles;
    }

    public void setPartyRoles(final Set<PartyRole> partyRoles)
    {
        this.partyRoles = partyRoles;
    }

    @Transient
    public void addPartyRole(final PartyRole role)
    {
        this.partyRoles.add(role);
    }

    @Transient
    public void addPartyRole(final PartyRoleType type)
    {
        final PartyRole partyRole = new PartyRole();
        partyRole.setType(type);
        partyRole.setParty(this);
        getPartyRoles().add(partyRole);
    }

    /**
     * Checks to see if the party has a role with the passed in partyType
     * @param type
     * @return
     */
    @Transient
    public boolean hasPartyRole(final PartyRoleType type)
    {
        for (PartyRole role: this.partyRoles)
        {
            if (role.getType().equals(type))
                return true;
        }
        return false;
    }
    
    @Transient
    public PartyRole getPartyRole(final PartyRoleType type)
    {
        for (PartyRole role: this.partyRoles)
        {
            if (role.getType().equals(type))
                return role;
        }
        return null;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "party_id")
    public Set<PartyIdentifier> getPartyIdentifiers()
    {
        return partyIdentifiers;
    }

    public void setPartyIdentifiers(final Set<PartyIdentifier> partyIdentifiers)
    {
        this.partyIdentifiers = partyIdentifiers;
    }


    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "party_id")
    public Set<PartyContactMechanism> getPartyContactMechanisms()
    {
        return partyContactMechanisms;
    }

    protected void setPartyContactMechanisms(final Set<PartyContactMechanism> partyContactMechanisms)
    {
        this.partyContactMechanisms = partyContactMechanisms;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "party_id")
    public Set<PartyFacilityRole> getPartyFacilityRoles()
    {
        return partyFacilityRoles;
    }

    public void setPartyFacilityRoles(final Set<PartyFacilityRole> partyFacilityRoles)
    {
        this.partyFacilityRoles = partyFacilityRoles;
    }

    @OneToMany(cascade =  CascadeType.ALL)
    @JoinColumn(name = "party_id")
    public Set<PartyIdentifierType> getPartyIdentifierTypes()
    {
        return partyIdentifierTypes;
    }

    public void setPartyIdentifierTypes(final Set<PartyIdentifierType> partyIdentifierTypes)
    {
        this.partyIdentifierTypes = partyIdentifierTypes;
    }

    @OneToMany(cascade =  CascadeType.ALL)
    @JoinColumn(name = "party_id")
    public Set<PartyRoleType> getPartyRoleTypes()
    {
        return partyRoleTypes;
    }

    public void setPartyRoleTypes(final Set<PartyRoleType> partyRoleTypes)
    {
        this.partyRoleTypes = partyRoleTypes;
    }

    @OneToMany(cascade =  CascadeType.ALL)
    @JoinColumn(name = "party_id")
    public Set<FacilityType> getFacilityTypes()
    {
        return facilityTypes;
    }

    public void setFacilityTypes(final Set<FacilityType> facilityTypes)
    {
        this.facilityTypes = facilityTypes;
    }

    @OneToMany(cascade =  CascadeType.ALL)
    @JoinColumn(name = "party_id")
    public Set<PartyRelationshipType> getPartyRelationshipTypes()
    {
        return partyRelationshipTypes;
    }

    public void setPartyRelationshipTypes(final Set<PartyRelationshipType> partyRelationshipTypes)
    {
        this.partyRelationshipTypes = partyRelationshipTypes;
    }

    @OneToMany(cascade =  CascadeType.ALL)
    @JoinColumn(name = "party_id")
    public Set<CommunicationEventPurposeType> getCommunicationEventPurposeTypes()
    {
        return communicationEventPurposeTypes;
    }

    public void setCommunicationEventPurposeTypes(Set<CommunicationEventPurposeType> communicationEventPurposeTypes)
    {
        this.communicationEventPurposeTypes = communicationEventPurposeTypes;
    }

    @OneToMany(cascade =  CascadeType.ALL)
    @JoinColumn(name = "party_id")
    public Set<CommunicationEventRoleType> getCommunicationEventRoleTypes()
    {
        return communicationEventRoleTypes;
    }

    public void setCommunicationEventRoleTypes(final Set<CommunicationEventRoleType> communicationEventRoleTypes)
    {
        this.communicationEventRoleTypes = communicationEventRoleTypes;
    }

    @OneToMany
    @JoinColumn(name = "party_id")
    public Set<CommunicationEventRole> getCommunicationEventRoles()
    {
        return communicationEventRoles;
    }

    public void setCommunicationEventRoles(final Set<CommunicationEventRole> communicationEventRoles)
    {
        this.communicationEventRoles = communicationEventRoles;
    }

    @OneToMany
    @JoinColumn(name = "party_id")
    public Set<BillingAccountRole> getBillingAccountRoles()
    {
        return billingAccountRoles;
    }

    public void setBillingAccountRoles(final Set<BillingAccountRole> billingAccountRoles)
    {
        this.billingAccountRoles = billingAccountRoles;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "party_id")
    public Set<InvoiceRole> getInvoiceRoles()
    {
        return invoiceRoles;
    }

    public void setInvoiceRoles(final Set<InvoiceRole> invoiceRoles)
    {
        this.invoiceRoles = invoiceRoles;
    }

    @OneToMany(mappedBy = "partyFrom")
    public Set<PartyRelationship> getFromPartyRelationships()
    {
        return fromPartyRelationships;
    }

    public void setFromPartyRelationships(final Set<PartyRelationship> fromPartyRelationships)
    {
        this.fromPartyRelationships = fromPartyRelationships;
    }

    @OneToMany(mappedBy = "partyTo")
    public Set<PartyRelationship> getToPartyRelationships()
    {
        return toPartyRelationships;
    }

    public void setToPartyRelationships(final Set<PartyRelationship> toPartyRelationships)
    {
        this.toPartyRelationships = toPartyRelationships;
    }

    @OneToMany(mappedBy = "party")
    public Set<PartyQualification> getPartyQualifications()
    {
        return partyQualifications;
    }

    public void setPartyQualifications(final Set<PartyQualification> partyQualifications)
    {
        this.partyQualifications = partyQualifications;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "party")
    public Set<InsurancePolicyRole> getInsurancePolicyRoles()
    {
        return insurancePolicyRoles;
    }

    public void setInsurancePolicyRoles(final Set<InsurancePolicyRole> insurancePolicyRoles)
    {
        this.insurancePolicyRoles = insurancePolicyRoles;
    }
}
