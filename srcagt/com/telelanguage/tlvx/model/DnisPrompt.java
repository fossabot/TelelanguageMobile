package com.telelanguage.tlvx.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

/**
 * DnisPrompt
 */
@Entity
@Table(name = "dnis_prompt_t")
public class DnisPrompt
{
    private Long dnisPromptId;
    private Dnis dnis;
    private Prompt prompt;
    private DnisPromptType dnisPromptType;
    private int playOrder;

    @Id
    @GeneratedValue
    @Column(name = "dnisPromptId")
    public Long getId()
    {
        return dnisPromptId;
    }
    
    public void setId(Long dnisPromptId) {
    	this.dnisPromptId = dnisPromptId;
    }

    @ManyToOne(optional = true)
    @JoinColumn(nullable = true, name = "dnisId")
    public Dnis getDnis()
    {
        return dnis;
    }

    public void setDnis(Dnis dnis)
    {
        this.dnis = dnis;
    }

    @ManyToOne(optional = true)
    @JoinColumn(nullable = true, name = "promptId")
    public Prompt getPrompt()
    {
        return prompt;
    }

    public void setPrompt(Prompt prompt)
    {
        this.prompt = prompt;
    }

    @Column(nullable = false, name = "dnisPromptType")
    @Type(type = "com.telelanguage.tlvx.model.EnumOrdinalEnhancedUserType", parameters =
            {@Parameter(
                          name = "enumClassName",
                          value = "com.telelanguage.tlvx.model.DnisPromptType"
            )})
    public DnisPromptType getDnisPromptType()
    {
        return dnisPromptType;
    }

    public void setDnisPromptType(DnisPromptType dnisPromptType)
    {
        this.dnisPromptType = dnisPromptType;
    }

    @Column(nullable = false)
    public int getPlayOrder()
    {
        return playOrder;
    }

    public void setPlayOrder(int playOrder)
    {
        this.playOrder = playOrder;
    }
}
