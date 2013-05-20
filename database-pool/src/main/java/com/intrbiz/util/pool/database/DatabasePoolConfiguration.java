package com.intrbiz.util.pool.database;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.configuration.Configuration;

@XmlType(name="database-pool")
@XmlRootElement(name="database-pool")
public class DatabasePoolConfiguration extends Configuration
{
    private String driver;
    
    private String url;
    
    private String username;
    
    private String password;
    
    private int minIdle = 2;
    
    private int maxIdle = 5;
    
    private long maxWait = 1000;
    
    private int maxActive = 50;
    
    private boolean testOnBorrow = true;
    
    private boolean testOnReturn = false;
    
    private boolean testWhileIdle = true;
    
    private String validationSql = "SELECT 1";
    
    public DatabasePoolConfiguration()
    {
    }

    @XmlElement(name="driver")
    public String getDriver()
    {
        return driver;
    }

    public void setDriver(String driver)
    {
        this.driver = driver;
    }

    @XmlElement(name="url")
    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    @XmlElement(name="username")
    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    @XmlElement(name="password")
    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @XmlAttribute(name="min-idle")
    public int getMinIdle()
    {
        return minIdle;
    }

    public void setMinIdle(int minIdle)
    {
        this.minIdle = minIdle;
    }

    @XmlAttribute(name="max-idle")
    public int getMaxIdle()
    {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle)
    {
        this.maxIdle = maxIdle;
    }

    @XmlAttribute(name="max-wait")
    public long getMaxWait()
    {
        return maxWait;
    }

    public void setMaxWait(long maxWait)
    {
        this.maxWait = maxWait;
    }

    @XmlAttribute(name="max-active")
    public int getMaxActive()
    {
        return maxActive;
    }

    public void setMaxActive(int maxActive)
    {
        this.maxActive = maxActive;
    }

    @XmlAttribute(name="test-on-borrow")
    public boolean isTestOnBorrow()
    {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow)
    {
        this.testOnBorrow = testOnBorrow;
    }

    @XmlAttribute(name="test-on-return")
    public boolean isTestOnReturn()
    {
        return testOnReturn;
    }

    public void setTestOnReturn(boolean testOnReturn)
    {
        this.testOnReturn = testOnReturn;
    }

    @XmlAttribute(name="test-while-idle")
    public boolean isTestWhileIdle()
    {
        return testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle)
    {
        this.testWhileIdle = testWhileIdle;
    }

    @XmlElement(name="validation-sql")
    public String getValidationSql()
    {
        return validationSql;
    }

    public void setValidationSql(String validationSql)
    {
        this.validationSql = validationSql;
    }
}
