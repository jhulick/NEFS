package auto.dal.db.vo.impl;


public class OrgVO
implements auto.dal.db.vo.Org
{
    
    public java.util.Date getBusinessEndTime()
    {
        return businessEndTime;
    }
    
    public java.util.Date getBusinessStartTime()
    {
        return businessStartTime;
    }
    
    public java.lang.String getCrSessId()
    {
        return crSessId;
    }
    
    public java.util.Date getCrStamp()
    {
        return crStamp;
    }
    
    public java.lang.Integer getEmployees()
    {
        return employees;
    }
    
    public java.lang.String getOrgAbbrev()
    {
        return orgAbbrev;
    }
    
    public java.lang.String getOrgCode()
    {
        return orgCode;
    }
    
    public java.lang.Long getOrgId()
    {
        return orgId;
    }
    
    public java.lang.String getOrgName()
    {
        return orgName;
    }
    
    public java.lang.Integer getRecStatId()
    {
        return recStatId;
    }
    
    public java.lang.String getTimeZone()
    {
        return timeZone;
    }
    
    public void setBusinessEndTime(java.util.Date businessEndTime)
    {
        this.businessEndTime = businessEndTime;
    }
    
    public void setBusinessStartTime(java.util.Date businessStartTime)
    {
        this.businessStartTime = businessStartTime;
    }
    
    public void setCrSessId(java.lang.String crSessId)
    {
        this.crSessId = crSessId;
    }
    
    public void setCrStamp(java.util.Date crStamp)
    {
        this.crStamp = crStamp;
    }
    
    public void setEmployees(java.lang.Integer employees)
    {
        this.employees = employees;
    }
    
    public void setOrgAbbrev(java.lang.String orgAbbrev)
    {
        this.orgAbbrev = orgAbbrev;
    }
    
    public void setOrgCode(java.lang.String orgCode)
    {
        this.orgCode = orgCode;
    }
    
    public void setOrgId(java.lang.Long orgId)
    {
        this.orgId = orgId;
    }
    
    public void setOrgName(java.lang.String orgName)
    {
        this.orgName = orgName;
    }
    
    public void setRecStatId(java.lang.Integer recStatId)
    {
        this.recStatId = recStatId;
    }
    
    public void setTimeZone(java.lang.String timeZone)
    {
        this.timeZone = timeZone;
    }
    private java.util.Date businessEndTime;
    private java.util.Date businessStartTime;
    private java.lang.String crSessId;
    private java.util.Date crStamp;
    private java.lang.Integer employees;
    private java.lang.String orgAbbrev;
    private java.lang.String orgCode;
    private java.lang.Long orgId;
    private java.lang.String orgName;
    private java.lang.Integer recStatId;
    private java.lang.String timeZone;
}