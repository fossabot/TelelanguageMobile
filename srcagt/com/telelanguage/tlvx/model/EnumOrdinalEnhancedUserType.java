package com.telelanguage.tlvx.model;

import java.io.Serializable;
import java.sql.*;
import java.util.EnumSet;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;

public class EnumOrdinalEnhancedUserType
    implements EnhancedUserType, ParameterizedType
{

    public EnumOrdinalEnhancedUserType()
    {
    }

    public void setParameterValues(Properties parameters)
    {
        String enumClassName = parameters.getProperty("enumClassName");
        try
        {
            enumClass = Thread.currentThread().getContextClassLoader().loadClass(enumClassName);
        }
        catch(ClassNotFoundException squash)
        {
            try
            {
                enumClass = getClass().getClassLoader().loadClass(enumClassName);
            }
            catch(ClassNotFoundException classNotFoundException)
            {
                throw new HibernateException("Enum class not found", classNotFoundException);
            }
        }
    }

    public Object assemble(Serializable cached, Object owner)
        throws HibernateException
    {
        return cached;
    }

    public Object deepCopy(Object value)
        throws HibernateException
    {
        return value;
    }

    public Serializable disassemble(Object value)
        throws HibernateException
    {
        return (Enum)value;
    }

    public boolean equals(Object x, Object y)
        throws HibernateException
    {
        return x == y;
    }

    public int hashCode(Object x)
        throws HibernateException
    {
        return x.hashCode();
    }

    public boolean isMutable()
    {
        return false;
    }

    public Object nullSafeGet(ResultSet rs, String names[], Object owner)
        throws HibernateException, SQLException
    {
        int ordinal = rs.getInt(names[0]);
        return rs.wasNull() ? null : EnumSet.allOf(enumClass).toArray()[ordinal];
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index)
        throws HibernateException, SQLException
    {
        if(value == null)
            st.setNull(index, 4);
        else
            st.setInt(index, ((Enum)value).ordinal());
    }

    public Object replace(Object original, Object target, Object owner)
        throws HibernateException
    {
        return original;
    }

    public Class returnedClass()
    {
        return enumClass;
    }

    public int[] sqlTypes()
    {
        return (new int[] {
            4
        });
    }

    public Object fromXMLString(String xmlValue)
    {
        return Enum.valueOf(enumClass, xmlValue);
    }

    public String objectToSQLString(Object value)
    {
        return (new StringBuffer(((Enum)value).ordinal())).toString();
    }

    public String toXMLString(Object value)
    {
        return objectToSQLString(value);
    }

    private Class enumClass;
}
