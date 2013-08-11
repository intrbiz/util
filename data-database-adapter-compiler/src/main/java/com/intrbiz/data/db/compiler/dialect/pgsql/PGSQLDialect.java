package com.intrbiz.data.db.compiler.dialect.pgsql;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.dialect.function.SQLFunctionGenerator;
import com.intrbiz.data.db.compiler.dialect.pgsql.function.GetterGenerator;
import com.intrbiz.data.db.compiler.dialect.pgsql.function.RemoveGenerator;
import com.intrbiz.data.db.compiler.dialect.pgsql.function.SetterGenerator;
import com.intrbiz.data.db.compiler.dialect.type.SQLSimpleType;
import com.intrbiz.data.db.compiler.dialect.type.SQLType;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.Deferable;
import com.intrbiz.data.db.compiler.meta.SQLGetter;
import com.intrbiz.data.db.compiler.meta.SQLRemove;
import com.intrbiz.data.db.compiler.meta.SQLSetter;
import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Column;
import com.intrbiz.data.db.compiler.model.ForeignKey;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Schema;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.Type;
import com.intrbiz.data.db.compiler.util.SQLWriter;

public class PGSQLDialect implements SQLDialect
{
    private static final SQLType TYPE_TEXT = new SQLSimpleType("TEXT", "String", String.class);

    private static final SQLType TYPE_INTEGER = new SQLSimpleType("INTEGER", "Int", int.class, Integer.class);

    private static final SQLType TYPE_BIGINT = new SQLSimpleType("BIGINT", "Long", long.class, Long.class);

    private static final SQLType TYPE_FLOAT = new SQLSimpleType("REAL", "Float", float.class, Float.class);

    private static final SQLType TYPE_DOUBLE = new SQLSimpleType("DOUBLE PRECISION", "Double", double.class, Double.class);

    private static final SQLType TYPE_BOOLEAN = new SQLSimpleType("BOOLEAN", "Boolean", boolean.class, Boolean.class);

    private static final SQLType TYPE_DATE = new SQLSimpleType("DATE", "Date", Date.class, Date.class);

    private static final SQLType TYPE_TIMESTAMP = new SQLSimpleType("TIMESTAMP WITH TIME ZONE", "Timestamp", Timestamp.class);

    private static final SQLType TYPE_UUID = new SQLSimpleType("UUID", "String", UUID.class)
    {
        public String setBinding(String p, int idx, String value)
        {
            return p + "stmt.setString(" + idx + ", " + value + " == null ? null : " + value + ".toString());\r\n";
        }

        public String getBinding(int idx)
        {
            return "rs.getString(" + idx + ") == null ? null : UUID.fromString(rs.getString(" + idx + "))";
        }
    };

    //

    private Map<Class<? extends Annotation>, SQLFunctionGenerator> functionGenerators = new IdentityHashMap<Class<? extends Annotation>, SQLFunctionGenerator>();

    //

    private String owner = "postgres";

    public PGSQLDialect()
    {
        super();
        // register default generators
        this.registerFunctionGenerator(SQLGetter.class, new GetterGenerator());
        this.registerFunctionGenerator(SQLSetter.class, new SetterGenerator());
        this.registerFunctionGenerator(SQLRemove.class, new RemoveGenerator());
    }

    @Override
    public void registerFunctionGenerator(Class<? extends Annotation> type, SQLFunctionGenerator generator)
    {
        this.functionGenerators.put(type, generator);
    }

    protected SQLFunctionGenerator getFunctionGenerator(Class<? extends Annotation> type)
    {
        return this.functionGenerators.get(type);
    }

    //

    @Override
    public String getDialectName()
    {
        return "PGSQL";
    }

    //

    @Override
    public String getOwner()
    {
        return this.owner;
    }

    @Override
    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    //

    @Override
    public SQLType getType(Class<?> javaClass)
    {
        if (String.class == javaClass)
            return TYPE_TEXT;
        else if (int.class == javaClass || Integer.class == javaClass)
            return TYPE_INTEGER;
        else if (long.class == javaClass || Long.class == javaClass)
            return TYPE_BIGINT;
        else if (float.class == javaClass || Float.class == javaClass)
            return TYPE_FLOAT;
        else if (double.class == javaClass || Double.class == javaClass)
            return TYPE_DOUBLE;
        else if (boolean.class == javaClass || Boolean.class == javaClass)
            return TYPE_BOOLEAN;
        else if (Timestamp.class == javaClass) 
            return TYPE_TIMESTAMP;
        else if (Date.class == javaClass) 
            return TYPE_DATE;
        else if (UUID.class == javaClass) 
            return TYPE_UUID;
        throw new RuntimeException("Cannot get SQL type for java class: " + javaClass.getCanonicalName());
    }

    //

    @Override
    public void writeCreateSchema(SQLWriter to, Schema schema) throws IOException
    {
        to.write("CREATE SCHEMA ").writeid(schema.getName()).write(" AUTHORIZATION ").write(this.getOwner()).writeln(";");
        to.writeln();
        to.flush();
    }

    @Override
    public void writeCreateTable(SQLWriter to, Table table) throws IOException
    {
        to.write("CREATE TABLE ").writeid(table.getSchema(), table.getName()).writeln();
        to.writeln("(");
        // attributes
        boolean ns = false;
        for (Column col : table.getColumns())
        {
            if (ns) to.writeln(",");
            to.write("    ").writeid(col.getName()).write(" ").write(col.getType().getSQLType());
            ns = true;
        }
        // primary key
        if (table.getPrimaryKey() != null)
        {
            if (ns) to.writeln(",");
            to.write("    CONSTRAINT ").writeid(table.getPrimaryKey().getName()).write(" PRIMARY KEY");
            to.write(" (").writeColumnNameList(table.getPrimaryKey().getColumns()).write(")");
            ns = true;
        }
        // constraints
        for (ForeignKey key : table.getForeignKeys())
        {
            if (ns) to.writeln(",");
            to.write("    CONSTRAINT ").writeid(key.getName()).write(" FOREIGN KEY");
            to.write(" (").writeColumnNameList(key.getColumns()).write(")");
            to.write(" REFERENCES ").writeid(key.getReferences().getSchema(), key.getReferences().getName());
            to.write(" (").writeColumnNameList(key.getOn()).write(")");
            to.write(" ON DELETE ").write(this.writeForeignKeyAction(key.getOnDelete()));
            to.write(" ON UPDATE ").write(this.writeForeignKeyAction(key.getOnUpdate()));
            to.write(" ").write(this.writeForeignKeyDeferable(key.getDeferable()));
            ns = true;
        }
        to.writeln();
        to.writeln(");");
        //
        to.write("ALTER TABLE ").writeid(table.getSchema(), table.getName()).write(" OWNER TO ").write(this.getOwner()).writeln(";");
        //
        to.writeln();
        to.flush();
    }

    protected String writeForeignKeyAction(Action a)
    {
        if (Action.CASCADE == a)
            return "CASCADE";
        else if (Action.RESTRICT == a)
            return "RESTRICT";
        else if (Action.SET_DEFAULT == a)
            return "SET DEFAULT";
        else if (Action.SET_NULL == a) return "SET NULL";
        return "NO ACTION";
    }

    protected String writeForeignKeyDeferable(Deferable d)
    {
        if (Deferable.DEFERRABLE == d)
            return "DEFERRABLE";
        else if (Deferable.INITIALLY_IMMEDIATE == d)
            return "INITIALLY IMMEDIATE";
        else if (Deferable.INITIALLY_DEFERRED == d) return "INITIALLY DEFERRED";
        return "NOT DEFERRABLE";
    }

    @Override
    public void writeCreateType(SQLWriter to, Type type) throws IOException
    {
        to.write("CREATE TYPE ").writeid(type.getSchema(), type.getName()).writeln(" AS");
        to.writeln("(");
        // attributes
        boolean ns = false;
        for (Column col : type.getColumns())
        {
            if (ns) to.writeln(",");
            to.write("    ").writeid(col.getName()).write(" ").write(col.getType().getSQLType());
            ns = true;
        }
        to.writeln();
        to.writeln(");");
        //
        to.write("ALTER TYPE ").writeid(type.getSchema(), type.getName()).write(" OWNER TO ").write(this.getOwner()).writeln(";");
        //
        to.writeln();
        to.flush();
    }

    @Override
    public void writeCreateFunction(SQLWriter to, Function function) throws IOException
    {
        to.write("CREATE OR REPLACE FUNCTION ").writeid(function.getSchema(), function.getName()).write("(");
        boolean ns = false;
        for (Argument arg : function.getArguments())
        {
            if (ns) to.write(", ");
            to.writeid("p_" + arg.getName()).write(" ").write(arg.getType().getSQLType());
            ns = true;
        }
        to.writeln(")");
        to.write("RETURNS SETOF ");
        if (function.getReturnType() == null)
        {
            // default to return int;
            to.write("INTEGER");
        }
        else
        {
            to.write(function.getReturnType().getSQLType());
        }
        to.writeln(" AS").writeln("$BODY$");
        //
        SQLFunctionGenerator generator = this.getFunctionGenerator(function.getFunctionType().annotationType());
        if (generator != null) generator.writeCreateFunctionBody(this, to, function);
        //
        to.writeln("$BODY$");
        to.writeln("LANGUAGE plpgsql;");
        to.write("ALTER FUNCTION ").writeid(function.getSchema(), function.getName()).write("(");
        ns = false;
        for (Argument arg : function.getArguments())
        {
            if (ns) to.write(", ");
            to.write(arg.getType().getSQLType());
            ns = true;
        }
        to.write(") OWNER TO ").write(this.getOwner()).writeln(";");
        to.writeln();
        to.flush();
    }

    public String functionBindingSQL(Function function)
    {
        StringWriter sw = new StringWriter();
        SQLWriter to = new SQLWriter(sw);
        //
        SQLFunctionGenerator generator = this.getFunctionGenerator(function.getFunctionType().annotationType());
        try
        {
            if (generator != null) generator.writefunctionBindingSQL(this, to, function);
        }
        catch (IOException e)
        {
        }
        //
        to.flush();
        to.close();
        return sw.toString();
    }
}
