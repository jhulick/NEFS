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
package com.netspective.medigy.model.person;

import com.netspective.medigy.model.common.AbstractTopLevelEntity;
import com.netspective.medigy.model.health.HealthCareEpisode;
import com.netspective.medigy.reference.custom.person.IncidentType;

import javax.persistence.Id;
import javax.persistence.GeneratorType;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;

@Entity
public class Incident extends AbstractTopLevelEntity
{
    private Long incidentId;
    private Date incidentDate;
    private String description;
    private Boolean employerRelatedInd;
    private IncidentType type;

    private Set<HealthCareEpisode> healthCareEpisodes = new HashSet<HealthCareEpisode>();

    @Id(generate = GeneratorType.AUTO)
    public Long getIncidentId()
    {
        return incidentId;
    }

    protected void setIncidentId(final Long incidentId)
    {
        this.incidentId = incidentId;
    }

    @Column
    public Date getIncidentDate()
    {
        return incidentDate;
    }

    public void setIncidentDate(final Date incidentDate)
    {
        this.incidentDate = incidentDate;
    }

    @Column(length = 1000)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    @Column(nullable = false)
    public Boolean getEmployerRelatedInd()
    {
        return employerRelatedInd;
    }

    public void setEmployerRelatedInd(final Boolean employerRelatedInd)
    {
        this.employerRelatedInd = employerRelatedInd;
    }

    @ManyToOne
    @JoinColumn(name = "incident_type_id")
    public IncidentType getType()
    {
        return type;
    }

    public void setType(final IncidentType type)
    {
        this.type = type;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "incident_id")
    public Set<HealthCareEpisode> getHealthCareEpisodes()
    {
        return healthCareEpisodes;
    }

    public void setHealthCareEpisodes(final Set<HealthCareEpisode> healthCareEpisodes)
    {
        this.healthCareEpisodes = healthCareEpisodes;
    }
}
