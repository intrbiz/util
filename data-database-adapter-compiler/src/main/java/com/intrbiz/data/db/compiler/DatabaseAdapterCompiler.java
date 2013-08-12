package com.intrbiz.data.db.compiler;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Schema;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.Type;
import com.intrbiz.data.db.compiler.util.SQLWriter;
import com.intrbiz.util.compiler.CompilerTool;
import com.intrbiz.util.compiler.model.JavaClass;
import com.intrbiz.util.compiler.model.JavaMethod;
import com.intrbiz.util.compiler.model.JavaParameter;

public class DatabaseAdapterCompiler
{
    public static final DatabaseAdapterCompiler defaultPGSQLCompiler()
    {
        return new DatabaseAdapterCompiler(new PGSQLDialect());
    }

    private final SQLDialect dialect;

    private final SQLIntrospector introspector;
    
    private Map<Class<? extends Annotation>, FunctionCompiler> functionCompilers = new IdentityHashMap<Class<? extends Annotation>, FunctionCompiler>();

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

    public void registerFunctionGenerator(Class<? extends Annotation> type, SQLFunctionGenerator generator)
    {
        this.dialect.registerFunctionGenerator(type, generator);
    }

    public void registerFunctionIntrospector(Class<? extends Annotation> type, SQLFunctionIntrospector introspector)
    {
        this.introspector.registerFunctionIntrospector(type, introspector);
    }
    
    public void registerFunctionCompiler(Class<? extends Annotation> type, FunctionCompiler compiler)
    {
        this.functionCompilers.put(type, compiler);
    }
    
    protected FunctionCompiler getFunctionCompiler(Class<? extends Annotation> type)
    {
        return this.functionCompilers.get(type);
    }

    public void compileSchema(Class<? extends DatabaseAdapter> cls) throws IOException
    {
        Schema schema = this.introspector.buildSchema(this.dialect, cls);
        //
        SQLWriter to = new SQLWriter(new PrintWriter(System.out));
        //
        to.writeln("BEGIN;");
        to.writeln();
        //
        this.dialect.writeCreateSchema(to, schema);
        // info functions
        this.dialect.writeCreateSchemaNameFunction(to, schema);
        this.dialect.writeCreateSchemaVersionFunction(to, schema);
        //
        for (Table table : schema.getTables())
        {
            this.dialect.writeCreateTable(to, table);
        }
        //
        for (Type type : schema.getTypes())
        {
            this.dialect.writeCreateType(to, type);
        }
        //
        for (Function function : schema.getFunctions())
        {
            this.dialect.writeCreateFunction(to, function);
        }
        //
        to.writeln();
        to.writeln("COMMIT;");
        to.flush();
    }

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

    protected Class<?> compileAdapterImplementation(Class<? extends DatabaseAdapter> cls)
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
        impl.addImport(UUID.class.getCanonicalName());
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
        JavaMethod jm = impl.newMethod("String", "_getDatabaseModuleName");
        jm.append("try\r\n");
        jm.append("{\r\n");
        jm.append("  return this.connection.use(new DatabaseCall<String>() {\r\n");
        jm.append("    public String run(final Connection with) throws SQLException {\r\n");
        jm.append("      try (PreparedStatement stmt = with.prepareStatement(\"" + escapeString(this.dialect.getSchemaNameSQL(schema)) + "\"))\r\n");
        jm.append("      {\r\n");
        jm.append("        try (ResultSet rs = stmt.executeQuery())\r\n");
        jm.append("        {\r\n");
        jm.append("          if (rs.next()) return rs.getString(1);\r\n");
        jm.append("        }\r\n");
        jm.append("      }\r\n");
        jm.append("      return null;\r\n");
        jm.append("    }\r\n");
        jm.append("  });\r\n");
        jm.append("}\r\n");
        jm.append("catch (Exception e)\r\n");
        jm.append("{\r\n");
        jm.append("/* Eat */\r\n");
        jm.append("}\r\n");
        jm.append("return null;\r\n");
    }
    
    protected void compileSchemaVersion(JavaClass impl, Schema schema)
    {
        JavaMethod jm = impl.newMethod("String", "_getDatabaseModuleVersion");
        jm.append("try\r\n");
        jm.append("{\r\n");
        jm.append("  return this.connection.use(new DatabaseCall<String>() {\r\n");
        jm.append("    public String run(final Connection with) throws SQLException {\r\n");
        jm.append("      try (PreparedStatement stmt = with.prepareStatement(\"" + escapeString(this.dialect.getSchemaVersionSQL(schema)) + "\"))\r\n");
        jm.append("      {\r\n");
        jm.append("        try (ResultSet rs = stmt.executeQuery())\r\n");
        jm.append("        {\r\n");
        jm.append("          if (rs.next()) return rs.getString(1);\r\n");
        jm.append("        }\r\n");
        jm.append("      }\r\n");
        jm.append("      return null;\r\n");
        jm.append("    }\r\n");
        jm.append("  });\r\n");
        jm.append("}\r\n");
        jm.append("catch (Exception e)\r\n");
        jm.append("{\r\n");
        jm.append("/* Eat */\r\n");
        jm.append("}\r\n");
        jm.append("return null;\r\n");
    }

    protected void compileMethodBinding(JavaMethod method, Function function)
    {
        FunctionCompiler compiler = this.getFunctionCompiler(function.getFunctionType().annotationType());
        if (compiler != null) compiler.compileFunctionBinding(this, method, function);
    }

    public static String escapeString(String str)
    {
        return str.replace("\"", "\\\"");
    }
}
