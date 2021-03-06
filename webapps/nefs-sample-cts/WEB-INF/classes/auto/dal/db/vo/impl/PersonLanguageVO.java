package auto.dal.db.vo.impl;


public class PersonLanguageVO
implements auto.dal.db.vo.PersonLanguage
{
    
    public java.lang.String getCrSessId()
    {
        return crSessId;
    }
    
    public java.util.Date getCrStamp()
    {
        return crStamp;
    }
    
    public java.lang.String getLanguage()
    {
        return language;
    }
    
    public java.lang.Integer getLanguageId()
    {
        return languageId;
    }
    
    public int getLanguageIdInt()
    {
        return getLanguageIdInt(-1);
    }
    
    public int getLanguageIdInt(int defaultValue)
    {
        return languageId != null ? languageId.intValue() : defaultValue;
    }
    
    public java.lang.Long getPersonId()
    {
        return personId;
    }
    
    public long getPersonIdLong()
    {
        return getPersonIdLong(-1);
    }
    
    public long getPersonIdLong(long defaultValue)
    {
        return personId != null ? personId.longValue() : defaultValue;
    }
    
    public java.lang.Integer getRecStatId()
    {
        return recStatId;
    }
    
    public int getRecStatIdInt()
    {
        return getRecStatIdInt(-1);
    }
    
    public int getRecStatIdInt(int defaultValue)
    {
        return recStatId != null ? recStatId.intValue() : defaultValue;
    }
    
    public java.lang.String getSystemId()
    {
        return systemId;
    }
    
    public void setCrSessId(java.lang.String crSessId)
    {
        this.crSessId = crSessId;
    }
    
    public void setCrStamp(java.util.Date crStamp)
    {
        this.crStamp = crStamp;
    }
    
    public void setLanguage(java.lang.String language)
    {
        this.language = language;
    }
    
    public void setLanguageId(java.lang.Integer languageId)
    {
        this.languageId = languageId;
    }
    
    public void setLanguageIdInt(int languageId)
    {
        this.languageId = new java.lang.Integer(languageId);
    }
    
    public void setPersonId(java.lang.Long personId)
    {
        this.personId = personId;
    }
    
    public void setPersonIdLong(long personId)
    {
        this.personId = new java.lang.Long(personId);
    }
    
    public void setRecStatId(java.lang.Integer recStatId)
    {
        this.recStatId = recStatId;
    }
    
    public void setRecStatIdInt(int recStatId)
    {
        this.recStatId = new java.lang.Integer(recStatId);
    }
    
    public void setSystemId(java.lang.String systemId)
    {
        this.systemId = systemId;
    }
    private java.lang.String crSessId;
    private java.util.Date crStamp;
    private java.lang.String language;
    private java.lang.Integer languageId;
    private java.lang.Long personId;
    private java.lang.Integer recStatId;
    private java.lang.String systemId;
}