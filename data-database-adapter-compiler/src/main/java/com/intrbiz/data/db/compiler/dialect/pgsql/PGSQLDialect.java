package com.intrbiz.data.db.compiler.dialect.pgsql;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.dialect.function.SQLFunctionGenerator;
import com.intrbiz.data.db.compiler.dialect.pgsql.function.GetterGenerator;
import com.intrbiz.data.db.compiler.dialect.pgsql.function.RemoveGenerator;
import com.intrbiz.data.db.compiler.dialect.pgsql.function.SetterGenerator;
import com.intrbiz.data.db.compiler.dialect.type.SQLArrayType;
import com.intrbiz.data.db.compiler.dialect.type.SQLEnumType;
import com.intrbiz.data.db.compiler.dialect.type.SQLSimpleType;
import com.intrbiz.data.db.compiler.dialect.type.SQLType;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.Deferable;
import com.intrbiz.data.db.compiler.meta.PartitionMode;
import com.intrbiz.data.db.compiler.meta.SQLGetter;
import com.intrbiz.data.db.compiler.meta.SQLRemove;
import com.intrbiz.data.db.compiler.meta.SQLSetter;
import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Column;
import com.intrbiz.data.db.compiler.model.ForeignKey;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Index;
import com.intrbiz.data.db.compiler.model.Partition;
import com.intrbiz.data.db.compiler.model.Partitioning;
import com.intrbiz.data.db.compiler.model.Schema;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.Type;
import com.intrbiz.data.db.compiler.model.Unique;
import com.intrbiz.data.db.compiler.model.function.UserDefinedInfo;
import com.intrbiz.data.db.compiler.util.SQLCommand;
import com.intrbiz.data.db.compiler.util.SQLScript;

public class PGSQLDialect extends SQLDialect
{
    private Logger logger = Logger.getLogger(PGSQLDialect.class);
    
    private static final SQLType TYPE_TEXT = new SQLSimpleType("TEXT", "String", String.class);
    
    private static final SQLType TYPE_TEXT_ARRAY = new SQLArrayType("TEXT[]", "text" /* Type name must be lower case in postgresql */, String.class);

    private static final SQLType TYPE_INTEGER = new SQLSimpleType("INTEGER", "Int", int.class, Integer.class);

    private static final SQLType TYPE_BIGINT = new SQLSimpleType("BIGINT", "Long", long.class, Long.class);

    private static final SQLType TYPE_FLOAT = new SQLSimpleType("REAL", "Float", float.class, Float.class);

    private static final SQLType TYPE_DOUBLE = new SQLSimpleType("DOUBLE PRECISION", "Double", double.class, Double.class);

    private static final SQLType TYPE_BOOLEAN = new SQLSimpleType("BOOLEAN", "Boolean", boolean.class, Boolean.class);

    private static final SQLType TYPE_DATE = new SQLSimpleType("DATE", "Date", Date.class, Date.class);

    private static final SQLType TYPE_TIMESTAMP = new SQLSimpleType("TIMESTAMP WITH TIME ZONE", "Timestamp", Timestamp.class);

    private static final SQLType TYPE_UUID = new SQLSimpleType("UUID", "Object", UUID.class)
    {
        public void addImports(Set<String> imports)
        {
            imports.add(UUID.class.getCanonicalName());
        }

        public String getBinding(int idx)
        {
            return "(UUID)" + super.getBinding(idx);
        }
    };
    
    /* PostgreSQL handle converting to java.util.UUID for us :D */
    private static final SQLType TYPE_UUID_ARRAY = new SQLArrayType("UUID[]", "uuid" /* Type name must be lower case in postgresql */, UUID.class);
    
    private static final SQLType TYPE_MACADDR = new SQLSimpleType("MACADDR", "String", String.class);
    
    private static final SQLType TYPE_CIDR = new SQLSimpleType("CIDR", "String", String.class);
    
    private static final SQLType TYPE_INET = new SQLSimpleType("INET", "String", String.class);
    
    private static final SQLType TYPE_JSON = new SQLSimpleType("JSON", "String", String.class);
    
    private static final SQLType TYPE_JSONB = new SQLSimpleType("JSONB", "String", String.class);
    
    private static final SQLType TYPE_BYTEA = new SQLSimpleType("BYTEA", "Bytes", byte[].class);

    public PGSQLDialect()
    {
        super("PGSQL", "postgres");
        // register default generators
        this.registerFunctionGenerator(SQLGetter.class, new GetterGenerator());
        this.registerFunctionGenerator(SQLSetter.class, new SetterGenerator());
        this.registerFunctionGenerator(SQLRemove.class, new RemoveGenerator());
    }

    @SuppressWarnings("unchecked")
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
        else if (byte[].class == javaClass) 
            return TYPE_BYTEA;
        else if (Enum.class.isAssignableFrom(javaClass))
            return new SQLEnumType((Class<? extends Enum<?>>) javaClass);
        throw new RuntimeException("Cannot get SQL type for java class: " + javaClass.getCanonicalName());
    }
    
    public SQLType getType(String sqlType)
    {
        if ("TEXT".equalsIgnoreCase(sqlType) || "VARCHAR".equalsIgnoreCase(sqlType))
            return TYPE_TEXT;
        if ("TEXT[]".equalsIgnoreCase(sqlType) || "VARCHAR[]".equalsIgnoreCase(sqlType))
            return TYPE_TEXT_ARRAY;
        else if ("INT".equalsIgnoreCase(sqlType) || "INTERGER".equalsIgnoreCase(sqlType) || "INT4".equalsIgnoreCase(sqlType))
            return TYPE_INTEGER;
        else if ("BIGINT".equalsIgnoreCase(sqlType) || "INT8".equalsIgnoreCase(sqlType))
            return TYPE_BIGINT;
        else if ("REAL".equalsIgnoreCase(sqlType))
            return TYPE_FLOAT;
        else if ("DOUBLE PRECISION".equalsIgnoreCase(sqlType))
            return TYPE_DOUBLE;
        else if ("BOOLEAN".equalsIgnoreCase(sqlType))
            return TYPE_BOOLEAN;
        else if ("TIMESTAMP WITH TIME ZONE".equalsIgnoreCase(sqlType)) 
            return TYPE_TIMESTAMP;
        else if ("DATE".equalsIgnoreCase(sqlType)) 
            return TYPE_DATE;
        else if ("UUID".equalsIgnoreCase(sqlType)) 
            return TYPE_UUID;
        else if ("UUID[]".equalsIgnoreCase(sqlType)) 
            return TYPE_UUID_ARRAY;
        else if ("MACADDR".equalsIgnoreCase(sqlType)) 
            return TYPE_MACADDR;
        else if ("INET".equalsIgnoreCase(sqlType)) 
            return TYPE_INET;
        else if ("CIDR".equalsIgnoreCase(sqlType)) 
            return TYPE_CIDR;
        else if ("JSON".equalsIgnoreCase(sqlType)) 
            return TYPE_JSON;
        else if ("BYTEA".equalsIgnoreCase(sqlType)) 
            return TYPE_BYTEA;
        else if ("JSONB".equalsIgnoreCase(sqlType)) 
            return TYPE_JSONB;
        throw new RuntimeException("The SQL type: " + sqlType + " is not supported");
    }

    //

    @Override
    public SQLScript writeCreateSchema(Schema schema)
    {
        SQLScript s = new SQLScript();
        s.command().write("CREATE SCHEMA ").writeid(schema.getName()).write(" AUTHORIZATION ").write(this.getOwner());
        return s;
    }
    
    @Override
    public SQLScript writeCreatePartitionedTable(Table table)
    {
        Partitioning parting = table.getPartitioning();
        Partition upperPart = parting.getPartitions().get(0);
        Partition lowerPart = parting.getPartitions().get(parting.getPartitions().size() - 1);
        boolean parentPrimaryKey = table.getPrimaryKey() != null && table.getPrimaryKey().findColumn(lowerPart.getOn().get(0).getName()) != null;
        //
        SQLScript s = new SQLScript();
        SQLCommand to = s.command();
        to.write("CREATE TABLE ").writeid(table.getSchema(), table.getName()).writeln();
        to.writeln("(");
        // attributes
        boolean ns = false;
        for (Column col : table.getColumns())
        {
            if (ns) to.writeln(",");
            to.write("    ").writeid(col.getName()).write(" ").write(col.getType().getSQLType());
            if (col.isNotNull()) to.write(" NOT NULL");
            ns = true;
        }
        // primary key
        if (parentPrimaryKey && table.getPrimaryKey() != null)
        {
            if (ns) to.writeln(",");
            to.write("    CONSTRAINT ").writeid(table.getPrimaryKey().getName()).write(" PRIMARY KEY");
            to.write(" (").writeColumnNameList(table.getPrimaryKey().getColumns()).write(")");
            ns = true;
        }
        // unique constraints
        for (Unique unq : table.getUniques())
        {
            if (ns) to.writeln(",");
            to.write("    CONSTRAINT ").writeid(unq.getName()).write(" UNIQUE");
            to.write(" (").writeColumnNameList(unq.getColumns()).write(")");
            ns = true;
        }
        to.writeln();
        to.writeln(")");
        to.write("PARTITION BY ").write(this.getPgsqlPartitionMode(upperPart.getMode())).write(" (").writeColumnNameList(upperPart.getOn()).writeln(")");
        //
        s.command().write("ALTER TABLE ").writeid(table.getSchema(), table.getName()).write(" OWNER TO ").write(this.getOwner());
        return s;
    }
    
    private String getPgsqlPartitionMode(PartitionMode mode)
    {
        switch (mode)
        {
            case HASH:  return "HASH";
            case RANGE: return "RANGE";
            case LIST:  return "LIST";
            // unsupported
            case NONE:
            default: break;
        }
        throw new RuntimeException("The partition mode " + mode + " is not supported by PostgreSQL");
    }
    
    public SQLScript writeCreatePartitionedTableHelpers(Table table)
    {
        SQLScript s = new SQLScript();
        Partitioning parting = table.getPartitioning();
        Partition lowerPart = parting.getPartitions().get(parting.getPartitions().size() - 1);
        boolean parentPrimaryKey = table.getPrimaryKey() != null && table.getPrimaryKey().findColumn(lowerPart.getOn().get(0).getName()) != null;
        for (int i = 0; i < parting.getPartitions().size(); i++)
        {
            Partition part = parting.getPartitions().get(i);
            Partition subPart = (i + 1) < parting.getPartitions().size() ? parting.getPartitions().get(i + 1) : null;
            // function to create child partitions
            createPartitionHelper(table, i, part, subPart, parentPrimaryKey, s);
        }
        return s;
    }
    
    protected void createPartitionHelper(Table table, int depth, Partition part, Partition subPart, boolean parentPrimaryKey, SQLScript s)
    {
        SQLCommand to = s.command();
        to.write("CREATE OR REPLACE FUNCTION ").writeid(table.getSchema(), "create_" + table.getName() + "_partition_" + depth).write("(");
        //
        if (depth > 0)
        {
            to.write("p_parent_table_name TEXT, ");    
        }
        to.write("p_suffix TEXT, ");
        if (part.getMode() == PartitionMode.RANGE)
        {
            to.write("p_from ").write(part.getOn().get(0).getType().getSQLType()).write(", ");
            to.write("p_to ").write(part.getOn().get(0).getType().getSQLType()).write(", ");
        }
        else if (part.getMode() == PartitionMode.HASH)
        {
            to.write("p_modulus INTEGER, ");
            to.write("p_remainder INTEGER, ");
        }
        else if (part.getMode() == PartitionMode.LIST)
        {
            to.write("p_values ").write(part.getOn().get(0).getType().getSQLType()).write("[], ");
        }
        to.write("p_exclude_indexes TEXT[]");
        //
        to.write(")").writeln();
        to.write("RETURNS TEXT AS ").writeln("$BODY$").writeln();
        to.write("DECLARE").writeln();
        to.write("  v_up_tbl_name TEXT;").writeln();
        to.write("  v_tbl_name TEXT;").writeln();
        to.write("  v_con_name TEXT;").writeln();
        to.write("  v_idx_name TEXT;").writeln();
        to.write("BEGIN").writeln();
        to.write("  -- Create the partition").writeln();
        if (depth > 0)
        {
            to.write("  v_up_tbl_name := p_parent_table_name;").writeln();   
        }
        else
        {
            to.write("  v_up_tbl_name := quote_ident('").write(table.getSchema().getName()).write("') || '.' || quote_ident('").write(table.getName()).writeln("');").writeln();
        }
        to.write("  v_tbl_name := quote_ident('").write(table.getSchema().getName()).write("') || '.' || quote_ident('").write(table.getName()).write("_' || p_suffix);").writeln();
        to.write("  EXECUTE 'CREATE TABLE ' || v_tbl_name || ' PARTITION OF ' || v_up_tbl_name || ").writeln();
        if (part.getMode() == PartitionMode.RANGE)
        {
            to.write("          ' FOR VALUES FROM (' || quote_literal(p_from) || ') TO (' || quote_literal(p_to) || ')' || ").writeln();
        }
        else if (part.getMode() == PartitionMode.HASH)
        {
            to.write("          ' FOR VALUES WITH (MODULUS ' || p_modulus || ', REMAINDER ' || p_remainder || ')' || ").writeln();
        }
        else if (part.getMode() == PartitionMode.LIST)
        {
            
            to.write("          ' FOR VALUES IN (' || (SELECT string_agg(quote_literal(v.e), ', ') FROM unnest(p_values) v(e)) || ')' || ").writeln();
        }
        if (subPart != null)
        {
            to.write("          ' PARTITION BY ").write(this.getPgsqlPartitionMode(subPart.getMode())).write(" (").writeColumnNameList(subPart.getOn()).write(")' || ").writeln();
        }
        to.write("          ';';").writeln();
        // stuff that is only on leaf partitions
        if (subPart == null)
        {
            if ((! parentPrimaryKey) && table.getPrimaryKey() != null)
            {
                to.write("  -- Create primary key").writeln();
                to.write("  v_con_name := quote_ident('").writeid(table.getPrimaryKey().getName()).write("_' || p_suffix);").writeln();
                to.write("  EXECUTE 'ALTER TABLE ' || v_tbl_name || ").writeln();
                to.write("          ' ADD CONSTRAINT ' || v_con_name  ||").writeln();
                to.write("          ' PRIMARY KEY (").writeColumnNameList(table.getPrimaryKey().getColumns()).write(");';").writeln();
            }
            // indexes
            // partition index
            if (part.isIndexOn())
            {
                to.write("  -- Create an index on the partitioned column").writeln();
                to.write("  v_idx_name := quote_ident('").write(table.getName()).write("_' || p_suffix || '_pt');").writeln();
                to.write("  EXECUTE 'CREATE INDEX ' || v_idx_name || ").writeln();
                to.write("          ' ON ' || v_tbl_name || ").writeln();
                to.write("          '  USING ").write(Util.coalesceEmpty(part.getIndexOnUsing(), "btree")).write("(").writeColumnNameList(part.getOn()).write(");';").writeln();
            }
            // custom indexes
            for (Index index : table.getIndexes())
            {
                to.write("  -- Create index ").write(index.getName()).writeln();
                to.write("  IF p_exclude_indexes IS NULL OR NOT ('").write(index.getName()).write("' = ANY(p_exclude_indexes)) THEN").writeln();
                to.write("    v_idx_name := quote_ident('").write(index.getName()).write("_' || p_suffix);").writeln();
                to.write("    EXECUTE 'CREATE INDEX ' || v_idx_name || ").writeln();
                to.write("            ' ON ' || v_tbl_name || ").writeln();
                to.write("            '  USING ").write(Util.coalesceEmpty(index.getUsing(), "btree")).write("(' || ");
                if (Util.isEmpty(index.getExpression()))
                {
                    to.write("'").writeColumnNameList(index.getColumns()).write("'");
                }
                else
                {
                    to.write("$IDX$").write(index.getExpression()).write("$IDX$");
                }
                to.write(" || ');';").writeln();
                to.write("  END IF;").writeln();
            }
        }
        to.write("  RETURN v_tbl_name;").writeln();
        to.write("END").writeln();
        to.write("$BODY$").writeln();
        to.write("LANGUAGE plpgsql").writeln();
        //
        to = s.command().write("ALTER FUNCTION ").writeid(table.getSchema(), "create_" + table.getName() + "_partition_" + depth).write("(");
        if (depth > 0)
        {
            to.write("TEXT, ");    
        }
        to.write("TEXT, ");
        if (part.getMode() == PartitionMode.RANGE)
        {
            to.write(part.getOn().get(0).getType().getSQLType()).write(", ");
            to.write(part.getOn().get(0).getType().getSQLType()).write(", ");
        }
        else if (part.getMode() == PartitionMode.HASH)
        {
            to.write("INTEGER, ");
            to.write("INTEGER, ");
        }
        else if (part.getMode() == PartitionMode.LIST)
        {
            to.write(part.getOn().get(0).getType().getSQLType()).write("[], ");
        }
        to.write("TEXT[])");
        to.write(" OWNER TO ").write(this.getOwner());
    }

    @Override
    public SQLScript writeCreateTable(Table table)
    {
        SQLScript s = new SQLScript();
        SQLCommand to = s.command();
        to.write("CREATE TABLE ").writeid(table.getSchema(), table.getName()).writeln();
        to.writeln("(");
        // attributes
        boolean ns = false;
        for (Column col : table.getColumns())
        {
            if (ns) to.writeln(",");
            to.write("    ").writeid(col.getName()).write(" ").write(col.getType().getSQLType());
            if (col.isNotNull()) to.write(" NOT NULL");
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
        // unique constraints
        for (Unique unq : table.getUniques())
        {
            if (ns) to.writeln(",");
            to.write("    CONSTRAINT ").writeid(unq.getName()).write(" UNIQUE");
            to.write(" (").writeColumnNameList(unq.getColumns()).write(")");
            ns = true;
        }
        to.writeln();
        to.write(")");
        //
        s.command().write("ALTER TABLE ").writeid(table.getSchema(), table.getName()).write(" OWNER TO ").write(this.getOwner());
        //
        return s;
    }
    
    @Override
    public SQLScript writeCreateIndex(Table table, Index idx)
    {
        SQLScript s = new SQLScript();
        SQLCommand to = s.command();
        to.write("CREATE INDEX ").writeid(idx.getName()).write(" ON ").writeid(table.getSchema(), table.getName()).writeln();
        to.write("  USING ").write(Util.coalesceEmpty(idx.getUsing(), "btree")).write("(");
        if (Util.isEmpty(idx.getExpression()))
        {
            boolean ns = false;
            for (Column col : idx.getColumns())
            {
                if (ns) to.write(",");
                to.writeid(col.getName());
                ns = true;
            }
        }
        else
        {
            to.write(idx.getExpression());
        }
        to.write(")");
        return s;
    }
    
    @Override
    public SQLScript writeAlterTableAddForeignKey(Table table, ForeignKey key)
    {
        SQLScript s = new SQLScript();
        SQLCommand to = s.command();
        to.write("ALTER TABLE ").writeid(table.getSchema(), table.getName());
        to.write(" ADD CONSTRAINT ").writeid(key.getName()).write(" FOREIGN KEY");
        to.write(" (").writeColumnNameList(key.getColumns()).write(")");
        to.write(" REFERENCES ").writeid(key.getReferences().getSchema(), key.getReferences().getName());
        to.write(" (").writeColumnNameList(key.getOn()).write(")");
        to.write(" ON DELETE ").write(this.writeForeignKeyAction(key.getOnDelete()));
        to.write(" ON UPDATE ").write(this.writeForeignKeyAction(key.getOnUpdate()));
        to.write(" ").write(this.writeForeignKeyDeferable(key.getDeferable()));
        return s;
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
    
    public SQLScript writeAlterTableAddColumn(Table table, Column col)
    {
        SQLScript s = new SQLScript();
        SQLCommand to = s.command();
        to.write("ALTER TABLE ").writeid(table.getSchema(), table.getName());
        to.write(" ADD COLUMN ").writeid(col.getName()).write(" ").write(col.getType().getSQLType());
        if (col.isNotNull()) to.write(" NOT NULL");
        return s;
    }

    @Override
    public SQLScript writeCreateType(Type type)
    {
        SQLScript s = new SQLScript();
        SQLCommand to = s.command();
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
        to.writeln(")");
        //
        s.command().write("ALTER TYPE ").writeid(type.getSchema(), type.getName()).write(" OWNER TO ").write(this.getOwner());
        //
        return s;
    }
    
    @Override
    public SQLScript writeAlterTypeAddColumn(Type type, Column col)
    {
        SQLScript s = new SQLScript();
        SQLCommand to = s.command();
        to.write("ALTER TYPE ").writeid(type.getSchema(), type.getName());
        to.write(" ADD ATTRIBUTE ").writeid(col.getName()).write(" ").write(col.getType().getSQLType());
        return s;
    }

    @Override
    public SQLScript writeCreateFunction(Function function)
    {
        // user defined or generated?
        if (function.getIntrospectionInformation() instanceof UserDefinedInfo && ((UserDefinedInfo) function.getIntrospectionInformation()).hasUserDefined())
        {
            logger.trace("Using user defined function " + function.getSchema().getName() + "." + function.getName());
            String[] sql = ((UserDefinedInfo) function.getIntrospectionInformation()).getUserDefined(this.getDialectName());
            if (sql == null || sql.length == 0) throw new RuntimeException("No user defined SQL provided for function " + function.getName() + " for dialect " + this.getDialectName() + " yet other there is SQL for other dialects");
            // copy the definition
            return new SQLScript(sql);
        }
        else
        {
            logger.trace("Creating function " + function.getSchema().getName() + "." + function.getName());
            //
            SQLScript s = new SQLScript();
            SQLCommand to = s.command();
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
            to.write("LANGUAGE plpgsql");
            
            to = s.command();
            to.write("ALTER FUNCTION ").writeid(function.getSchema(), function.getName()).write("(");
            ns = false;
            for (Argument arg : function.getArguments())
            {
                if (ns) to.write(", ");
                to.write(arg.getType().getSQLType());
                ns = true;
            }
            to.write(") OWNER TO ").write(this.getOwner());
            return s;
        }
    }
    
    @Override
    public SQLScript writeCreateSchemaNameFunction(Schema schema)
    {
        SQLScript s = new SQLScript();
        SQLCommand to = s.command();
        to.write("CREATE OR REPLACE FUNCTION ").writeid(schema.getName()).writeln("._get_module_name()");
        to.writeln("RETURNS TEXT AS");
        to.writeln("$BODY$");
        to.write("  SELECT '").write(schema.getName()).writeln("'::TEXT;");
        to.writeln("$BODY$");
        to.write("LANGUAGE sql IMMUTABLE");
        //
        s.command().write("ALTER FUNCTION ").writeid(schema.getName()).write("._get_module_name() OWNER TO ").write(this.getOwner());
        return s;
    }
    
    @Override
    public SQLScript writeCreateSchemaVersionFunction(Schema schema)
    {
        SQLScript s = new SQLScript();
        SQLCommand to = s.command();
        to.write("CREATE OR REPLACE FUNCTION ").writeid(schema.getName()).writeln("._get_module_version()");
        to.writeln("RETURNS TEXT AS");
        to.writeln("$BODY$");
        to.write("  SELECT '").write(schema.getVersion().toString()).writeln("'::TEXT;");
        to.writeln("$BODY$");
        to.write("LANGUAGE sql IMMUTABLE");
        //
        s.command().write("ALTER FUNCTION ").writeid(schema.getName()).write("._get_module_version() OWNER TO ").write(this.getOwner());
        return s;
    }
    
    @Override
    public String getSchemaNameQuery(Schema schema)
    {
        return "SELECT \"" + schema.getName() + "\"._get_module_name()";
    }
    
    @Override
    public String getSchemaVersionQuery(Schema schema)
    {
        return "SELECT \"" + schema.getName() + "\"._get_module_version()";
    }

    @Override
    public SQLCommand getFunctionCallQuery(Function function)
    {
        SQLFunctionGenerator generator = this.getFunctionGenerator(function.getFunctionType().annotationType());
        if (generator != null) return generator.writefunctionBindingSQL(this, function);
        throw new RuntimeException("Cannot generate calling SQL query for method: " + function.getDefinition());
    }
}
