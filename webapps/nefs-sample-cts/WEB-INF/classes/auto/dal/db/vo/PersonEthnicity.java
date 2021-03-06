package auto.dal.db.vo;


public interface PersonEthnicity
{
    
    public java.lang.String getCrSessId();
    
    public java.util.Date getCrStamp();
    
    public java.lang.String getEthnicity();
    
    public java.lang.Integer getEthnicityId();
    
    public int getEthnicityIdInt();
    
    public int getEthnicityIdInt(int defaultValue);
    
    public java.lang.Long getPersonId();
    
    public long getPersonIdLong();
    
    public long getPersonIdLong(long defaultValue);
    
    public java.lang.Integer getRecStatId();
    
    public int getRecStatIdInt();
    
    public int getRecStatIdInt(int defaultValue);
    
    public java.lang.String getSystemId();
    
    public void setCrSessId(java.lang.String crSessId);
    
    public void setCrStamp(java.util.Date crStamp);
    
    public void setEthnicity(java.lang.String ethnicity);
    
    public void setEthnicityId(java.lang.Integer ethnicityId);
    
    public void setEthnicityIdInt(int ethnicityId);
    
    public void setPersonId(java.lang.Long personId);
    
    public void setPersonIdLong(long personId);
    
    public void setRecStatId(java.lang.Integer recStatId);
    
    public void setRecStatIdInt(int recStatId);
    
    public void setSystemId(java.lang.String systemId);
}