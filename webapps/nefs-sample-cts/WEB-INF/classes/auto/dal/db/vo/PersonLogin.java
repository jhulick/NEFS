package auto.dal.db.vo;


public interface PersonLogin
{
    
    public java.lang.String getCrSessId();
    
    public java.util.Date getCrStamp();
    
    public java.lang.Integer getLoginStatus();
    
    public int getLoginStatusInt();
    
    public int getLoginStatusInt(int defaultValue);
    
    public java.lang.String getPassword();
    
    public java.lang.Long getPersonId();
    
    public long getPersonIdLong();
    
    public long getPersonIdLong(long defaultValue);
    
    public java.lang.Integer getQuantity();
    
    public int getQuantityInt();
    
    public int getQuantityInt(int defaultValue);
    
    public java.lang.Integer getRecStatId();
    
    public int getRecStatIdInt();
    
    public int getRecStatIdInt(int defaultValue);
    
    public java.lang.String getSystemId();
    
    public java.lang.String getUserId();
    
    public void setCrSessId(java.lang.String crSessId);
    
    public void setCrStamp(java.util.Date crStamp);
    
    public void setLoginStatus(java.lang.Integer loginStatus);
    
    public void setLoginStatusInt(int loginStatus);
    
    public void setPassword(java.lang.String password);
    
    public void setPersonId(java.lang.Long personId);
    
    public void setPersonIdLong(long personId);
    
    public void setQuantity(java.lang.Integer quantity);
    
    public void setQuantityInt(int quantity);
    
    public void setRecStatId(java.lang.Integer recStatId);
    
    public void setRecStatIdInt(int recStatId);
    
    public void setSystemId(java.lang.String systemId);
    
    public void setUserId(java.lang.String userId);
}