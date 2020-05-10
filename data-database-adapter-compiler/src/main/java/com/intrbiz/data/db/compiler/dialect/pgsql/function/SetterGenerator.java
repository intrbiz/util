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
        to.writeln("BEGIN");
        this.generateInsert(dialect, to, function);
        if (info.isUpsert() && function.getTable().getPrimaryKey() != null)
        {
            this.generateConflict(dialect, to, function);
        }
        to.write(";");
        to.writeln("  RETURN NEXT 1;");
        to.writeln("  RETURN;");
        to.writeln("END;");
    }
    
    protected void generateInsert(SQLDialect dialect, SQLCommand to, Function function)
    {
        Table table = function.getTable();
        to.write("  INSERT INTO").writeid(table.getSchema(), table.getName()).write(" (").writeColumnNameList(table.getColumns()).write(")");
        to.write(" VALUES ");
        to.writeln("(").writeArgumentNameList(function.getArguments()).writeln(")");
    }
    
    protected void generateConflict(SQLDialect dialect, SQLCommand to, Function function)
    {
        Table table = function.getTable();
        if (table.getPrimaryKey() == null)
            throw new RuntimeException("Cannot generate upsert for setter " + function.getName() + " as the table " + table.getName() + " has no primary key!");
        to.write("  ON CONFLICT");
        if (table.getPartitioning() == null)
        {
            to.write(" ON CONSTRAINT ").writeid(table.getPrimaryKey().getName()).writeln();
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
