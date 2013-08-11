package com.intrbiz.data.db.compiler.dialect.pgsql.function;

import java.io.IOException;

import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.dialect.function.SQLFunctionGenerator;
import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Column;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.function.SetterInfo;
import com.intrbiz.data.db.compiler.util.SQLWriter;

public class SetterGenerator implements SQLFunctionGenerator
{
    @Override
    public void writeCreateFunctionBody(SQLDialect dialect, SQLWriter to, Function function) throws IOException
    {
        SetterInfo info = (SetterInfo) function.getIntrospectionInformation();
        //
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
    
    protected void generateInsert(SQLDialect dialect, SQLWriter to, Function function) throws IOException
    {
        SetterInfo info = (SetterInfo) function.getIntrospectionInformation();
        Table table = info.getTable();
        to.write("  INSERT INTO").writeid(table.getSchema(), table.getName()).write(" (").writeColumnNameList(table.getColumns()).write(")");
        to.write(" VALUES ");
        to.write("(").writeArgumentNameList(function.getArguments()).writeln(");");
    }
    
    protected void generateUpdate(SQLDialect dialect, SQLWriter to, Function function) throws IOException
    {
        SetterInfo info = (SetterInfo) function.getIntrospectionInformation();
        Table table = info.getTable();
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
    
    public void writefunctionBindingSQL(SQLDialect dialect, SQLWriter to, Function function) throws IOException
    {
        to.write("SELECT * FROM ").writeid(function.getSchema(), function.getName()).write("(");
        boolean ns = false;
        for (Argument arg : function.getArguments())
        {
            if (ns) to.write(", ");
            to.write("?::").write(arg.getType().getSQLType());
            ns = true;
        }
        to.write(")");
    }
}
