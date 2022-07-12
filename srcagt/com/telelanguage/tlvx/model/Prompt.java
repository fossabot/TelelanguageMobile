package com.telelanguage.tlvx.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Prompt
 */
@Entity
@Table(name = "prompt_t")
public class Prompt
{
    private Long promptId;
    private String fileUrl;
    private String ttsText;

    @Id
    @GeneratedValue
    @Column(name = "promptId")
    public Long getId()
    {
        return promptId;
    }
    
    public void setId(Long promptId) {
    	this.promptId = promptId;
    }

    @Column(length = 255)
    public String getFileUrl()
    {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
    }

    @Column(length = 4096)
    public String getTtsText()
    {
        return ttsText;
    }

    public void setTtsText(String ttsText)
    {
        this.ttsText = ttsText;
    }
}
