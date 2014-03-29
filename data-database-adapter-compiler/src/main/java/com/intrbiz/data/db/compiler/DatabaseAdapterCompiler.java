package com.intrbiz.data.db.compiler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.data.DataException;
import com.intrbiz.data.DataManager.DatabaseAdapterFactory;
import com.intrbiz.data.db.DatabaseAdapter;
import com.intrbiz.data.db.DatabaseConnection;
import com.intrbiz.data.db.DatabaseConnection.DatabaseCall;
import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.dialect.function.SQLFunctionGenerator;
import com.intrbiz.data.db.compiler.dialect.pgsql.PGSQLDialect;
import com.intrbiz.data.db.compiler.function.FunctionCompiler;
import com.intrbiz.data.db.compiler.function.GetterCompiler;
import com.intrbiz.data.db.compiler.function.RemoveCompiler;
import com.intrbiz.data.db.compiler.function.SetterCompiler;
import com.intrbiz.data.db.compiler.introspector.SQLIntrospector;
import com.intrbiz.data.db.compiler.introspector.function.SQLFunctionIntrospector;
import com.intrbiz.data.db.compiler.meta.SQLGetter;
import com.intrbiz.data.db.compiler.meta.SQLRemove;
import com.intrbiz.data.db.compiler.meta.SQLSetter;
import com.intrbiz.data.db.compiler.meta.ScriptType;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Patch;
import com.intrbiz.data.db.compiler.model.Schema;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.Type;
import com.intrbiz.data.db.compiler.model.Version;
import com.intrbiz.data.db.compiler.util.SQLCommand;
import com.intrbiz.data.db.compiler.util.SQLScript;
import com.intrbiz.data.db.compiler.util.SQLScriptSet;
import com.intrbiz.util.compiler.CompilerTool;
import com.intrbiz.util.compiler.model.JavaClass;
import com.intrbiz.util.compiler.model.JavaField;
import com.intrbiz.util.compiler.model.JavaMethod;
import com.intrbiz.util.compiler.model.JavaParameter;
import com.intrbiz.util.compiler.util.JavaUtil;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;

public class DatabaseAdapterCompiler
{
    public static final DatabaseAdapterCompiler defaultPGSQLCompiler()
    {
        return new DatabaseAdapterCompiler(new PGSQLDialect());
    }
    
    public static final DatabaseAdapterCompiler defaultPGSQLCompiler(String defaultOwner)
    {
        return new DatabaseAdapterCompiler(new PGSQLDialect()).setDefaultOwner(defaultOwner);
    }

    private final SQLDialect dialect;

    private final SQLIntrospector introspector;

    private Map<Class<? extends Annotation>, FunctionCompiler> functionCompilers = new IdentityHashMap<Class<? extends Annotation>, FunctionCompiler>();
    
    // options
    
    private boolean withMetrics = true;

    private DatabaseAdapterCompiler(SQLDialect dialect, SQLIntrospector introspector)
    {
        super();
        this.dialect = dialect;
        this.introspector = introspector;
        // default function compilers
        this.registerFunctionCompiler(SQLGetter.class, new GetterCompiler());
        this.registerFunctionCompiler(SQLSetter.class, new SetterCompiler());
        this.registerFunctionCompiler(SQLRemove.class, new RemoveCompiler());
    }

    private DatabaseAdapterCompiler(SQLDialect dialect)
    {
        this(dialect, new SQLIntrospector());
    }

    public SQLDialect getDialect()
    {
        return dialect;
    }

    public SQLIntrospector getIntrospector()
    {
        return introspector;
    }

    public DatabaseAdapterCompiler registerFunctionGenerator(Class<? extends Annotation> type, SQLFunctionGenerator generator)
    {
        this.dialect.registerFunctionGenerator(type, generator);
        return this;
    }

    public DatabaseAdapterCompiler registerFunctionIntrospector(Class<? extends Annotation> type, SQLFunctionIntrospector introspector)
    {
        this.introspector.registerFunctionIntrospector(type, introspector);
        return this;
    }

    public DatabaseAdapterCompiler registerFunctionCompiler(Class<? extends Annotation> type, FunctionCompiler compiler)
    {
        this.functionCompilers.put(type, compiler);
        return this;
    }

    protected FunctionCompiler getFunctionCompiler(Class<? extends Annotation> type)
    {
        return this.functionCompilers.get(type);
    }
    
    // owner
    
    public String getOwner()
    {
        return this.dialect.getOwner();
    }
    
    public DatabaseAdapterCompiler setOwner(String owner)
    {
        this.dialect.setOwner(owner);
        return this;
    }
    
    public String getDefaultOwner()
    {
        return this.getDefaultOwner();
    }
    
    public DatabaseAdapterCompiler setDefaultOwner(String owner)
    {
        this.dialect.setDefaultOwner(owner);
        return this;
    }
    
    // options
    
    public boolean isWithMetrics()
    {
        return this.withMetrics;
    }
    
    public DatabaseAdapterCompiler setWithMetrics(boolean withMetrics)
    {
        this.withMetrics = withMetrics;
        return this;
    }

    // schema
    public SQLScriptSet compileInstallSchema(Class<? extends DatabaseAdapter> cls)
    {
        Schema schema = this.introspector.buildSchema(this.dialect, cls);
        //
        SQLScriptSet set = new SQLScriptSet();
        //
        set.add(this.dialect.writeCreateSchema(schema));
        // info functions
        set.add(this.dialect.writeCreateSchemaNameFunction(schema));
        set.add(this.dialect.writeCreateSchemaVersionFunction(schema));
        // create all the tables
        for (Table table : schema.getTables())
        {
            set.add(this.dialect.writeCreateTable(table));
        }
        // add all foreign keys
        for (Table table : schema.getTables())
        {
            set.add(this.dialect.writeCreateTableForeignKeys(table));
        }
        //
        for (Type type : schema.getTypes())
        {
            set.add(this.dialect.writeCreateType(type));
        }
        //
        for (Function function : schema.getFunctions())
        {
            set.add(this.dialect.writeCreateFunction(function));
        }
        // add any install patches for this version
        for (Patch patch : schema.getPatches())
        {
            if (ScriptType.INSTALL == patch.getType() && patch.getVersion().isBeforeOrEqual(schema.getVersion()))
            {
                set.add(patch.getScript());
            }
        }
        return set;
    }

    public SQLScriptSet compileUpgradeSchema(Class<? extends DatabaseAdapter> cls, Version installedVersion)
    {
        Schema schema = this.introspector.buildSchema(this.dialect, cls);
        //
        if (installedVersion.isAfter(schema.getVersion())) throw new RuntimeException("Cannot upgrade a schema to a previous version (" + installedVersion + " => " + schema.getVersion() + ").");
        //
        SQLScriptSet set = new SQLScriptSet();
        // upgrade the version info function
        set.add(this.dialect.writeCreateSchemaVersionFunction(schema));
        // install any new tables
        for (Table table : schema.getTables())
        {
            if (table.getSince().isAfter(installedVersion))
            {
                set.add(this.dialect.writeCreateTable(table));
            }
        }
        // install foreign keys for any new tables
        for (Table table : schema.getTables())
        {
            if (table.getSince().isAfter(installedVersion))
            {
                set.add(this.dialect.writeCreateTableForeignKeys(table));
            }
        }
        // install any new types
        for (Type type : schema.getTypes())
        {
            if (type.getSince().isAfter(installedVersion))
            {
                set.add(this.dialect.writeCreateType(type));
            }
        }
        // run any table / type upgrade scripts
        for (Patch patch : schema.getPatches())
        {
            if (ScriptType.UPGRADE == patch.getType() && patch.getVersion().isAfter(installedVersion))
            {
                set.add(patch.getScript());
            }
        }
        // update or install functions
        for (Function function : schema.getFunctions())
        {
            set.add(this.dialect.writeCreateFunction(function));
        }
        return set;
    }

    public boolean isSchemaInstalled(DatabaseConnection database, Class<? extends DatabaseAdapter> cls)
    {
        Schema schema = this.introspector.buildSchema(this.dialect, cls);
        String installedName = database.getDatabaseModuleName(this.dialect.getSchemaNameQuery(schema));
        return installedName != null;
    }

    public Version getInstalledVersion(DatabaseConnection database, Class<? extends DatabaseAdapter> cls)
    {
        Schema schema = this.introspector.buildSchema(this.dialect, cls);
        String installedVersion = database.getDatabaseModuleVersion(this.dialect.getSchemaVersionQuery(schema));
        return installedVersion == null ? null : new Version(installedVersion);
    }

    public boolean isSchemaUptoDate(DatabaseConnection database, Class<? extends DatabaseAdapter> cls)
    {
        Schema schema = this.introspector.buildSchema(this.dialect, cls);
        Version installedVersion = this.getInstalledVersion(database, cls);
        return schema.getVersion().isBeforeOrEqual(installedVersion);
    }

    public void executeSchema(DatabaseConnection database, final SQLScriptSet schemaScript)
    {
        final Logger logger = Logger.getLogger(DatabaseAdapterCompiler.class);
        database.execute(new DatabaseCall<Void>()
        {
            public Void run(final Connection with) throws SQLException
            {
                for (SQLScript script : schemaScript.getScripts())
                {
                    for (SQLCommand command : script.getCommands())
                    {
                        try (Statement stmt = with.createStatement())
                        {
                            if (logger.isTraceEnabled()) logger.trace("Executing: " + command.toString() + ";");
                            stmt.execute(command.toString());
                        }
                    }
                }
                return null;
            }
        });
    }

    public void installSchema(DatabaseConnection database, Class<? extends DatabaseAdapter> cls)
    {
        SQLScriptSet set = this.compileInstallSchema(cls);
        this.executeSchema(database, set);
    }

    public void upgradeSchema(DatabaseConnection database, Class<? extends DatabaseAdapter> cls)
    {
        // get the installed version
        Version installedVersion = this.getInstalledVersion(database, cls);
        // get the upgrade script
        SQLScriptSet set = this.compileUpgradeSchema(cls, installedVersion);
        // execute it
        this.executeSchema(database, set);
    }

    /**
     * Install or upgrade the given database to the current version
     * @param database
     * @param cls
     */
    public void install(DatabaseConnection database, Class<? extends DatabaseAdapter> cls)
    {
        Logger logger = Logger.getLogger(DatabaseAdapterCompiler.class);
        try
        {
            // check if the schema is installed
            if (!this.isSchemaInstalled(database, cls))
            {
                Schema schema = this.introspector.buildSchema(this.dialect, cls);
                logger.info("Installing database schema version " + schema.getVersion() + ".");
                this.installSchema(database, cls);
            }
            else
            {
                // check the installed schema is upto date
                if (!this.isSchemaUptoDate(database, cls))
                {
                    Schema schema = this.introspector.buildSchema(this.dialect, cls);
                    Version installedVersion = this.getInstalledVersion(database, cls);
                    logger.info("Upgrading database schema from version " + installedVersion + " to version " + schema.getVersion() + ".");
                    this.upgradeSchema(database, cls);
                }
                else
                {
                    Schema schema = this.introspector.buildSchema(this.dialect, cls);
                    Version installedVersion = this.getInstalledVersion(database, cls);
                    logger.info("The installed database schema is upto date: schema version " + installedVersion + " adapter version " + schema.getVersion() + ".");
                }
            }
        }
        catch (DataException e)
        {
            logger.error("Error installing database schema", e);
        }
    }

    // adapter class

    @SuppressWarnings("unchecked")
    public <T extends DatabaseAdapter> DatabaseAdapterFactory<T> compileAdapterFactory(Class<T> cls)
    {
        // compile the actual adapter implementation
        Class<?> impl = this.compileAdapterImplementation(cls);
        // compile the factory
        JavaClass fact = new JavaClass(cls.getPackage().getName(), cls.getSimpleName() + "ImplFactory");
        fact.addImport(DatabaseAdapterFactory.class.getCanonicalName());
        fact.addImport(DatabaseConnection.class.getCanonicalName());
        fact.addImport(impl.getCanonicalName());
        fact.addSuperInterface(DatabaseAdapterFactory.class.getSimpleName());
        fact.newMethod(impl.getSimpleName(), "create", new JavaParameter("DatabaseConnection", "connection")).append("return new " + impl.getSimpleName() + "(connection);");
        //
        try
        {
            Class<?> factoryClass = CompilerTool.getInstance().defineClass(fact);
            if (factoryClass == null) throw new RuntimeException("Failed to compile adapter factory class");
            return (DatabaseAdapterFactory<T>) factoryClass.newInstance();
        }
        catch (InstantiationException | IllegalAccessException | ClassNotFoundException e)
        {
            throw new RuntimeException("Failed to create factory class", e);
        }
    }

    public Class<?> compileAdapterImplementation(Class<? extends DatabaseAdapter> cls)
    {
        Schema schema = this.introspector.buildSchema(this.dialect, cls);
        // the implementation class
        JavaClass impl = new JavaClass(cls.getPackage().getName(), cls.getSimpleName() + "Impl");
        // some imports
        impl.addImport(cls.getCanonicalName());
        impl.addImport(DataException.class.getCanonicalName());
        impl.addImport(DatabaseConnection.class.getCanonicalName());
        impl.addImport(List.class.getCanonicalName());
        impl.addImport(LinkedList.class.getCanonicalName());
        impl.addImport(DatabaseCall.class.getCanonicalName());
        impl.addImport(Connection.class.getCanonicalName());
        impl.addImport(PreparedStatement.class.getCanonicalName());
        impl.addImport(ResultSet.class.getCanonicalName());
        impl.addImport(SQLException.class.getCanonicalName());
        impl.addImport(Exception.class.getCanonicalName());
        // super class
        impl.setSuperClass(cls.getSimpleName());
        // default constructor
        impl.newConstructor(new JavaParameter("DatabaseConnection", "connection")).append("super(connection);");
        // info functions
        this.compileSchemaName(impl, schema);
        this.compileSchemaVersion(impl, schema);
        // the database functions
        for (Function function : schema.getFunctions())
        {
            if (Modifier.isAbstract(function.getDefinition().getModifiers()))
            {
                JavaMethod method = impl.newMethod(function.getDefinition());
                // fixup generics
                if (function.isReturnsList() && function.getReturnType() != null)
                {
                    method.setReturnType("List<" + function.getReturnType().getDefaultJavaType().getSimpleName() + ">");
                    impl.addImport(function.getReturnType().getDefaultJavaType().getCanonicalName());
                }
                // compile the binding
                this.compileMethodBinding(method, function);
            }
        }
        // compile!
        try
        {
            Class<?> implCls = CompilerTool.getInstance().defineClass(impl);
            if (implCls == null) throw new RuntimeException("Failed to compile adapter implementation class");
            return implCls;
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Failed to compile adapter implementation", e);
        }
    }

    protected void compileSchemaName(JavaClass impl, Schema schema)
    {
        JavaMethod jm = impl.newMethod("String", "getDatabaseModuleName");
        jm.append("return this.connection.getDatabaseModuleName(\"").append(JavaUtil.escapeString(this.dialect.getSchemaNameQuery(schema)) + "\");\r\n");

    }

    protected void compileSchemaVersion(JavaClass impl, Schema schema)
    {
        JavaMethod jm = impl.newMethod("String", "getDatabaseModuleVersion");
        jm.append("return this.connection.getDatabaseModuleName(\"").append(JavaUtil.escapeString(this.dialect.getSchemaVersionQuery(schema)) + "\");\r\n");
    }

    protected void compileMethodBinding(JavaMethod method, Function function)
    {
        FunctionCompiler compiler = this.getFunctionCompiler(function.getFunctionType().annotationType());
        if (compiler != null) compiler.compileFunctionBinding(this, method, function);
    }
    
    public static JavaField addMetricField(JavaMethod method, Function function)
    {
        method.getJavaClass().addImport(Timer.class.getCanonicalName());
        method.getJavaClass().addImport(Metrics.class.getCanonicalName());
        method.getJavaClass().addImport(TimeUnit.class.getCanonicalName());
        JavaField metricField = method.getJavaClass().newUniqueField(Timer.class.getSimpleName(), function.getName());
        metricField.setValue("Metrics.newTimer(" + function.getSchema().getDefinition().getSimpleName() + ".class, \"" + JavaUtil.escapeString(function.getSignature()) + "\", TimeUnit.MICROSECONDS, TimeUnit.SECONDS)");
        return metricField;
    }
    
    /**
     * Quick and dirty utility to output schemas etc
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception
    {
        String action = args[0];
        String clsName = args[1];
        //
        Class<? extends DatabaseAdapter> cls = (Class<? extends DatabaseAdapter>) Class.forName(clsName);
        //
        if ("install".equals(action))
        {
            DatabaseAdapterCompiler compiler = DatabaseAdapterCompiler.defaultPGSQLCompiler();
            SQLScriptSet script = compiler.compileInstallSchema(cls);
            System.out.println(script.toString());
        }
        else if ("upgrade".equals(action))
        {
            Version installedVersion = new Version(args[2]);
            DatabaseAdapterCompiler compiler = DatabaseAdapterCompiler.defaultPGSQLCompiler();
            SQLScriptSet script = compiler.compileUpgradeSchema(cls, installedVersion);
            System.out.println(script.toString());
        }
    }
}
