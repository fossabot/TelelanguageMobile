package com.telelanguage.video.service;

import java.text.DateFormat;
import java.util.Date;

public class NumericDate
{
    // JWT's NumericDate says that "non-integer values can be represented"
    // https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-32#section-2
    // I always just assumed that it could only be integers (maybe b/c of the former IntDate name )
    // but looking at the text again it looks like maybe fractional values has always been possible.
    // I'm not sure I see value in truly supporting sub-second accuracy (right now, anyway) but do want to
    // ensure that we handle such values reasonably, if we receive them. The  testNonIntegerNumericDates test
    // in JwtClaimsSetTest checks that we don't fail and just truncate the sub-second part.

    private long value;
    private static final long CONVERSION = 1000L;

    private NumericDate(long value)
    {
        this.value = value;
    }

    public static NumericDate now()
    {
        return fromMilliseconds(System.currentTimeMillis());
    }

    public static NumericDate fromSeconds(long secondsFromEpoch)
    {
        return new NumericDate(secondsFromEpoch);
    }

    public static NumericDate fromMilliseconds(long millisecondsFromEpoch)
    {
        return fromSeconds(millisecondsFromEpoch / CONVERSION);
    }

    public void addSeconds(long seconds)
    {
        value += seconds;
    }

    /**
     * Returns a numeric value representing the number of seconds from
     * 1970-01-01T0:0:0Z UTC until the given UTC date/time
     * @return value
     */
    public long getValue()
    {
        return value;
    }
    
    public void setValue(long value)
    {
        this.value = value; 
    }

    public long getValueInMillis()
    {
        return getValue() * CONVERSION;  
    }

    public boolean isBefore(NumericDate when)
    {
        return value < when.getValue();
    }

    public boolean isOnOrAfter(NumericDate when)
    {
        return !isBefore(when);
    }

    public boolean isAfter(NumericDate when)
    {
        return value > when.getValue();
    }

    @Override
    public String toString()
    {
        DateFormat df  = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG);
        StringBuilder sb = new StringBuilder();
        Date date = new Date(getValueInMillis());
        sb.append("NumericDate").append("{").append(getValue()).append(" -> ").append(df.format(date)).append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object other)
    {
        return (this == other) || ((other instanceof NumericDate) && (value == ((NumericDate) other).value));
    }

    @Override
    public int hashCode()
    {
        return (int) (value ^ (value >>> 32));
    }
}
