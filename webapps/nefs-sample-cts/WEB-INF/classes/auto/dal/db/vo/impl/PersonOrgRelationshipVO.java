package auto.dal.db.vo.impl;


public class PersonOrgRelationshipVO
implements auto.dal.db.vo.PersonOrgRelationship
{
    
    public java.lang.String getCrSessId()
    {
        return crSessId;
    }
    
    public java.util.Date getCrStamp()
    {
        return crStamp;
    }
    
    public java.lang.Long getParentId()
    {
        return parentId;
    }
    
    public java.lang.Integer getRecStatId()
    {
        return recStatId;
    }
    
    public java.util.Date getRelBegin()
    {
        return relBegin;
    }
    
    public java.lang.String getRelDescr()
    {
        return relDescr;
    }
    
    public java.util.Date getRelEnd()
    {
        return relEnd;
    }
    
    public java.lang.Long getRelEntityId()
    {
        return relEntityId;
    }
    
    public java.lang.String getRelType()
    {
        return relType;
    }
    
    public java.lang.Integer getRelTypeId()
    {
        return relTypeId;
    }
    
    public java.lang.String getRelationshipCode()
    {
        return relationshipCode;
    }
    
    public java.lang.String getRelationshipName()
    {
        return relationshipName;
    }
    
    public java.lang.Long getSystemId()
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
    
    public void setParentId(java.lang.Long parentId)
    {
        this.parentId = parentId;
    }
    
    public void setRecStatId(java.lang.Integer recStatId)
    {
        this.recStatId = recStatId;
    }
    
    public void setRelBegin(java.util.Date relBegin)
    {
        this.relBegin = relBegin;
    }
    
    public void setRelDescr(java.lang.String relDescr)
    {
        this.relDescr = relDescr;
    }
    
    public void setRelEnd(java.util.Date relEnd)
    {
        this.relEnd = relEnd;
    }
    
    public void setRelEntityId(java.lang.Long relEntityId)
    {
        this.relEntityId = relEntityId;
    }
    
    public void setRelType(java.lang.String relType)
    {
        this.relType = relType;
    }
    
    public void setRelTypeId(java.lang.Integer relTypeId)
    {
        this.relTypeId = relTypeId;
    }
    
    public void setRelationshipCode(java.lang.String relationshipCode)
    {
        this.relationshipCode = relationshipCode;
    }
    
    public void setRelationshipName(java.lang.String relationshipName)
    {
        this.relationshipName = relationshipName;
    }
    
    public void setSystemId(java.lang.Long systemId)
    {
        this.systemId = systemId;
    }
    private java.lang.String crSessId;
    private java.util.Date crStamp;
    private java.lang.Long parentId;
    private java.lang.Integer recStatId;
    private java.util.Date relBegin;
    private java.lang.String relDescr;
    private java.util.Date relEnd;
    private java.lang.Long relEntityId;
    private java.lang.String relType;
    private java.lang.Integer relTypeId;
    private java.lang.String relationshipCode;
    private java.lang.String relationshipName;
    private java.lang.Long systemId;
}