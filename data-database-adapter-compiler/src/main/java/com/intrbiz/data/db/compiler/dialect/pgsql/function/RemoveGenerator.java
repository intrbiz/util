package com.intrbiz.data.db.compiler.dialect.pgsql.function;

import java.io.IOException;

import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.dialect.function.SQLFunctionGenerator;
import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Column;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.function.RemoveInfo;
import com.intrbiz.data.db.compiler.util.SQLWriter;

public class RemoveGenerator implements SQLFunctionGenerator
{
    @Override
    public void writeCreateFunctionBody(SQLDialect dialect, SQLWriter to, Function function) throws IOException
    {
        to.writeln("DECLARE");
        to.writeln("BEGIN");
        //
        this.generateDelete(dialect, to, function);
        to.writeln("  RETURN NEXT 1;");
        to.writeln("  RETURN;");
        //
        to.writeln("END;");
    }

    protected void generateDelete(SQLDialect dialect, SQLWriter to, Function function) throws IOException
    {
        RemoveInfo info = (RemoveInfo) function.getIntrospectionInformation();
        Table table = info.getTable();
        to.write("  DELETE FROM ").writeid(table.getSchema(), table.getName());
        // pkey
        to.write(" WHERE ");
        boolean ns = false;
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
