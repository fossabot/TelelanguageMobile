package com.telelanguage.tlvx.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "user_t")
public class User
{
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private boolean emailChangePending;
    private boolean activated;
    private boolean enabled;
    private String nickname;
    private String code;
    
    @Id
    @GeneratedValue
    @Column(name = "userId")
    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }
    
    @Column(nullable = false, length = 50)
    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    @Column(nullable = false, length = 100)
    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    @Column(nullable = false, length = 255, unique = true)
    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    @Column(nullable = false, length = 512)
    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @Column(nullable = false)
    @Type(type="yes_no")
    public boolean isEmailChangePending()
    {
        return emailChangePending;
    }

    public void setEmailChangePending(boolean emailChangePending)
    {
        this.emailChangePending = emailChangePending;
    }

    @Column(nullable = false)
    @Type(type="yes_no")
    public boolean isActivated()
    {
        return activated;
    }

    public void setActivated(boolean activated)
    {
        this.activated = activated;
    }

    @Column(nullable = false)
    @Type(type="yes_no")
    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @Column(nullable = false, length = 255, unique = true)
    public String getNickname()
    {
        return nickname;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }

    @Column(length = 255)
    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }
}
