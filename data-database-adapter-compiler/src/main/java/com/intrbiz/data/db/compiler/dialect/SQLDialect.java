package com.intrbiz.data.db.compiler.dialect;

import java.lang.annotation.Annotation;
import java.util.IdentityHashMap;
import java.util.Map;

import com.intrbiz.Util;
import com.intrbiz.data.db.compiler.dialect.function.SQLFunctionGenerator;
import com.intrbiz.data.db.compiler.dialect.type.SQLType;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Schema;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.Type;
import com.intrbiz.data.db.compiler.util.SQLCommand;
import com.intrbiz.data.db.compiler.util.SQLScript;

public abstract class SQLDialect
{
    public static String DATABASE_OWNER = "database.owner";
    
    protected final String dialect;
    
    protected String defaultOwner;
    
    protected String owner;
    
    protected Map<Class<? extends Annotation>, SQLFunctionGenerator> functionGenerators = new IdentityHashMap<Class<? extends Annotation>, SQLFunctionGenerator>();
    
    public SQLDialect(String dialect, String defaultOwner)
    {
        this.dialect = dialect;
        this.defaultOwner = defaultOwner;
    }
    
    public final String getDialectName()
    {
        return this.dialect;
    }
    
    public void registerFunctionGenerator(Class<? extends Annotation> type, SQLFunctionGenerator generator)
    {
        this.functionGenerators.put(type, generator);
    }

    protected SQLFunctionGenerator getFunctionGenerator(Class<? extends Annotation> type)
    {
        return this.functionGenerators.get(type);
    }
    
    // Schema owner
    
    public final String getOwner()
    {
        // use the explicit owner
        if (!Util.isEmpty(this.owner)) return this.owner;
        // look at a system property
        String dbOwner = System.getProperty(DATABASE_OWNER);
        if (! Util.isEmpty(dbOwner)) return dbOwner;
        // use the default
        return this.defaultOwner;
    }
    
    public final void setOwner(String owner)
    {
        this.owner = owner;
    }
    
    public final String getDefaultOwner()
    {
        return this.defaultOwner;
    }
    
    public final void setDefaultOwner(String owner)
    {
        this.defaultOwner = owner;
    }
    
    // SQL types
    
    public abstract SQLType getType(Class<?> javaClass);
    
    // DDL
    
    public abstract SQLScript writeCreateSchema(Schema schema);
    
    public abstract SQLScript writeCreateTable(Table table);
    
    public abstract SQLScript writeCreateType(Type type);
    
    public abstract SQLScript writeCreateFunction(Function function);
    
    // Module info
    
    public abstract SQLScript writeCreateSchemaNameFunction(Schema schema);
    
    public abstract SQLScript writeCreateSchemaVersionFunction(Schema schema);
    
    public abstract String getSchemaNameQuery(Schema schema);
    
    public abstract String getSchemaVersionQuery(Schema schema);
    
    // Function call SQL
    
    public abstract SQLCommand getFunctionCallQuery(Function function);
}
