package com.telelanguage.tlvx.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Dnis
 */
@Entity
@Table(name = "dnis_t")
public class Dnis
{
	private Long dnisId;
    private String dnis;

    /**
     * get the identifier for this object
     *
     * @return the id
     */
    @Id
    @GeneratedValue
    @Column(name = "dnisId")
    public Long getId()
    {
        return dnisId;
    }
    
    public void setId(Long dnisId) {
    	this.dnisId = dnisId;
    }

    @Column(nullable = false, length = 64, unique = true)
    public String getDnis()
    {
        return dnis;
    }

    public void setDnis(String dnis)
    {
        this.dnis = dnis;
    }
}
