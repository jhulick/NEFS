package auto.dal.db.vo;


public interface Org
{
    
    public java.util.Date getBusinessEndTime();
    
    public java.util.Date getBusinessStartTime();
    
    public java.lang.String getCrSessId();
    
    public java.util.Date getCrStamp();
    
    public java.lang.Integer getEmployees();
    
    public java.lang.String getOrgAbbrev();
    
    public java.lang.String getOrgCode();
    
    public java.lang.Long getOrgId();
    
    public java.lang.String getOrgName();
    
    public java.lang.Integer getRecStatId();
    
    public java.lang.String getTimeZone();
    
    public void setBusinessEndTime(java.util.Date businessEndTime);
    
    public void setBusinessStartTime(java.util.Date businessStartTime);
    
    public void setCrSessId(java.lang.String crSessId);
    
    public void setCrStamp(java.util.Date crStamp);
    
    public void setEmployees(java.lang.Integer employees);
    
    public void setOrgAbbrev(java.lang.String orgAbbrev);
    
    public void setOrgCode(java.lang.String orgCode);
    
    public void setOrgId(java.lang.Long orgId);
    
    public void setOrgName(java.lang.String orgName);
    
    public void setRecStatId(java.lang.Integer recStatId);
    
    public void setTimeZone(java.lang.String timeZone);
}