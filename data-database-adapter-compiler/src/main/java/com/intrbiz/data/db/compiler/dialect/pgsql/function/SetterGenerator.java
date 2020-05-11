package com.intrbiz.data.db.compiler.dialect.pgsql.function;

import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.dialect.function.SQLFunctionGenerator;
import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Column;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.function.SetterInfo;
import com.intrbiz.data.db.compiler.util.SQLCommand;

public class SetterGenerator implements SQLFunctionGenerator
{
    @Override
    public void writeCreateFunctionBody(SQLDialect dialect, SQLCommand to, Function function)
    {
        SetterInfo info = (SetterInfo) function.getIntrospectionInformation();
        Table table = function.getTable();
        if (table.getPartitioning() == null)
        {
            this.writeInsertOnConflict(dialect, to, function, info);
        }
        else
        {
            boolean parentPrimaryKey = table.getPrimaryKey() != null && table.getPartitioning().isParentPrimaryKey(table.getPrimaryKey());
            if (parentPrimaryKey)
            {
                // use insert on conflict when the primary key can be defined upon the top level partitioned table
                this.writeInsertOnConflict(dialect, to, function, info);
            }
            else
            {
                this.writeUpsert(dialect, to, function, info);
            }
        }
    }
    
    protected void writeInsertOnConflict(SQLDialect dialect, SQLCommand to, Function function, SetterInfo info)
    {
        Table table = function.getTable();
        to.writeln("BEGIN");
        to.write("  INSERT INTO").writeid(table.getSchema(), table.getName()).write(" (").writeColumnNameList(table.getColumns()).write(")");
        to.write(" VALUES ");
        to.writeln("(").writeArgumentNameList(function.getArguments()).writeln(")");
        if (info.isUpsert())
        {
            if (table.getPrimaryKey() == null)
                throw new RuntimeException("Cannot generate conflict clause for setter " + function.getName() + " as the table " + table.getName() + " has no primary key!");
            //
            to.write("  ON CONFLICT ");
            if (table.getPartitioning() == null)
            {
                to.write("ON CONSTRAINT ").writeid(table.getPrimaryKey().getName()).writeln();
            }
            else
            {
                to.write("(").writeColumnNameList(table.getPrimaryKey().getColumns()).write(")").writeln();
            }
            to.write("   DO UPDATE SET ");
            // set cols
            boolean ns = false;
            for (Column col : table.getNonPrimaryColumns())
            {
                if (ns) to.write(", ");
                // cheat
                to.writeid(col.getName()).write(" = ").writeid("p_" + col.getName());
                ns = true;
            }
        }
        to.write(";");
        to.writeln("  RETURN NEXT 1;");
        to.writeln("  RETURN;");
        to.writeln("END;");
    }
    
    public void writeUpsert(SQLDialect dialect, SQLCommand to, Function function, SetterInfo info)
    {
        to.writeln("DECLARE");
        if (info.isUpsert()) to.writeln("  _i INTEGER;");
        to.writeln("BEGIN");
        //
        if (info.isUpsert())
        {
            to.writeln("  FOR _i IN 0..100 LOOP");
            to.write("  ");
            this.generateUpdate(dialect, to, function);
            to.writeln("    IF found THEN");
            to.writeln("      RETURN NEXT 1;");
            to.writeln("      RETURN;");
            to.writeln("    END IF;");
            to.writeln("    BEGIN");
            to.write("    ");
            this.generateInsert(dialect, to, function);
            to.writeln("      RETURN NEXT 1;");
            to.writeln("      RETURN;");
            to.writeln("    EXCEPTION");
            to.writeln("      WHEN unique_violation THEN");
            to.writeln("        /* nothing */");
            to.writeln("    END;");
            to.writeln("  END LOOP;");
            to.writeln("  RAISE SQLSTATE 'UPS01' USING MESSAGE = 'Failed to upsert';");
        }
        else
        {
            this.generateInsert(dialect, to, function);
            to.writeln("  RETURN NEXT 1;");
            to.writeln("  RETURN;");
        }
        //
        to.writeln("END;");
    }
    
    protected void generateInsert(SQLDialect dialect, SQLCommand to, Function function)
    {
        Table table = function.getTable();
        to.write("  INSERT INTO").writeid(table.getSchema(), table.getName()).write(" (").writeColumnNameList(table.getColumns()).write(")");
        to.write(" VALUES ");
        to.write("(").writeArgumentNameList(function.getArguments()).writeln(");");
    }
    
    protected void generateUpdate(SQLDialect dialect, SQLCommand to, Function function)
    {
        Table table = function.getTable();
        if (table.getPrimaryKey() == null)
            throw new RuntimeException("Cannot generate upsert for setter " + function.getName() + " as the table " + table.getName() + " has no primary key!");
        to.write("  UPDATE ").writeid(table.getSchema(), table.getName());
        // set cols
        to.write(" SET ");
        boolean ns = false;
        for (Column col : table.getNonPrimaryColumns())
        {
            if (ns) to.write(", ");
            // cheat
            to.writeid(col.getName()).write(" = ").writeid("p_" + col.getName());
            ns = true;
        }
        // pkey
        to.write(" WHERE ");
        ns = false;
        for (Column col : table.getPrimaryKey().getColumns())
        {
            if (ns) to.write(" AND ");
            // cheat
            to.writeid(col.getName()).write(" = ").writeid("p_" + col.getName());
            ns = true;
        }
        to.writeln(";");
    }
    
    public SQLCommand writefunctionBindingSQL(SQLDialect dialect, Function function)
    {
        SQLCommand to = new SQLCommand();
        to.write("SELECT * FROM ").writeid(function.getSchema(), function.getName()).write("(");
        boolean ns = false;
        for (Argument arg : function.getArguments())
        {
            if (ns) to.write(", ");
            to.write("?::").write(arg.getType().getSQLType());
            ns = true;
        }
        to.write(")");
        return to;
    }
}
