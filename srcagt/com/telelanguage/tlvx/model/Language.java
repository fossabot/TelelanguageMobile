package com.telelanguage.tlvx.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Language
 */
/*
@FieldResult(name = "pkId", column = "ID"),
@FieldResult(name = "languageName", column = "Language_Name"),
@FieldResult(name = "languageCode", column = "Language_Code"),
@FieldResult(name = "accessCode", column = "Access_Code"),
@FieldResult(name = "deleted", column = "Deleted"),
@FieldResult(name = "promptWave", column = "Prompt_Wave")}))
@NamedNativeQueries({
	@NamedNativeQuery(name = "uspLanguageGet", query = "{call usp_Language_Get(?)}", resultSetMapping = "Languages"),
	@NamedNativeQuery(name = "findLanguagesByName", query = "select * from Languages where Deleted < 1 AND Language_Name like ? order by Language_Name", resultSetMapping = "Languages"),
    @NamedNativeQuery(name = "findLanguageById", query = "select * from Languages where ID = ?", resultSetMapping = "Languages"),
    @NamedNativeQuery(name = "findLanguage", query = "select * from Languages where Language_Name = ?", resultSetMapping = "Languages")
})
*/
@Entity 
@Table(name = "languages")
public class Language implements IsSerializable 
{
	private static final long serialVersionUID = -8299643478529259150L;
	public String pkId;
    public String languageName;
    public String languageCode;
    public String accessCode;
    public boolean deleted;
    public String promptWave;
    
    @Override
    public String toString() { return languageName; }

    @Id
    @Column(name="ID")
    public String getPkId()
    {
        return pkId;
    }

    public void setPkId(String pkId)
    {
        this.pkId = pkId;
    }

    @Column(name="Language_Name")
    public String getLanguageName()
    {
        return languageName;
    }

    public void setLanguageName(String languageName)
    {
        this.languageName = languageName;
    }

    @Column(name="Language_Code")
    public String getLanguageCode()
    {
        return languageCode;
    }

    public void setLanguageCode(String languageCode)
    {
        this.languageCode = languageCode;
    }

    @Column(name="Access_Code")
    public String getAccessCode()
    {
        return accessCode;
    }

    public void setAccessCode(String accessCode)
    {
        this.accessCode = accessCode;
    }

    @Column(name="Deleted", columnDefinition="tinyint")
    public boolean getDeleted()
    {
        return deleted;
    }

    public void setDeleted(boolean deleted)
    {
        this.deleted = deleted;
    }

    @Column(name="promptWave")
    public String getPromptWave()
    {
        return promptWave;
    }

    public void setPromptWave(String promptWave)
    {
        this.promptWave = promptWave;
    }
}
