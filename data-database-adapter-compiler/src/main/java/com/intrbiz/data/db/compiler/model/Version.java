package com.intrbiz.data.db.compiler.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intrbiz.data.db.compiler.meta.SQLVersion;

public class Version implements Comparable<Version>
{
    private static final Pattern VERSION_FORMAT = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)");

    public final int major;

    public final int minor;

    public final int patch;

    public Version(int major, int minor, int patch)
    {
        super();
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public Version(String version)
    {
        Matcher m = VERSION_FORMAT.matcher(version);
        if (m.matches())
        {
            this.major = Integer.parseInt(m.group(1));
            this.minor = Integer.parseInt(m.group(2));
            this.patch = Integer.parseInt(m.group(3));
        }
        else
        {
            throw new IllegalArgumentException("Invalid version string");
        }
    }
    
    public Version(SQLVersion version)
    {
        this.major = version.major();
        this.minor = version.minor();
        this.patch = version.patch();
    }
    
    /**
     * Is this version before the given version
     * @param other
     * @return
     */
    public boolean isBefore(Version other)
    {
        return this.compareTo(other) < 0;
    }
    
    public boolean isBeforeOrEqual(Version other)
    {
        return this.compareTo(other) <= 0;
    }
    
    /**
     * Is this version after the given version
     * @param other
     * @return
     */
    public boolean isAfter(Version other)
    {
        return this.compareTo(other) > 0;
    }
    
    public boolean isAfterOrEqual(Version other)
    {
        return this.compareTo(other) >= 0;
    }

    @Override
    public int compareTo(Version o)
    {
        if (this.major == o.major)
        {
            if (this.minor == o.minor)
            {
                return this.patch - o.patch;
            }
            return this.minor - o.minor;
        }
        return this.major - o.major;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + major;
        result = prime * result + minor;
        result = prime * result + patch;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Version other = (Version) obj;
        if (major != other.major) return false;
        if (minor != other.minor) return false;
        if (patch != other.patch) return false;
        return true;
    }

    public String toString()
    {
        return this.major + "." + this.minor + "." + this.patch;
    }
}
