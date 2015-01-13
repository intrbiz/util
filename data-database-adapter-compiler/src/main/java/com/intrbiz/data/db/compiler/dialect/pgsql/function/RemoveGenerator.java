package com.intrbiz.data.db.compiler.dialect.pgsql.function;

import com.intrbiz.Util;
import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.dialect.function.SQLFunctionGenerator;
import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.function.RemoveInfo;
import com.intrbiz.data.db.compiler.util.SQLCommand;

public class RemoveGenerator implements SQLFunctionGenerator
{
    @Override
    public void writeCreateFunctionBody(SQLDialect dialect, SQLCommand to, Function function)
    {
        RemoveInfo info = (RemoveInfo) function.getIntrospectionInformation();
        //
        to.writeln("DECLARE");
        to.writeln("BEGIN");
        //
        if (info.hasQuery())
        {
            String query = info.getQuery(dialect.getDialectName());
            if (Util.isEmpty(query)) throw new RuntimeException("The function " + function.getName() + " has no query for the dialect " + dialect.getDialectName());
            to.write(query);
            to.writeln(";");
        }
        else
        {
            this.generateDelete(dialect, to, function);
        }
        //
        to.writeln("  RETURN NEXT 1;");
        to.writeln("  RETURN;");
        //
        to.writeln("END;");
    }

    protected void generateDelete(SQLDialect dialect, SQLCommand to, Function function)
    {
        Table table = function.getTable();
        to.write("  DELETE FROM ").writeid(table.getSchema(), table.getName());
        // pkey
        to.write(" WHERE ");
        boolean ns = false;
        for (Argument arg : function.getArguments())
        {
            if (ns) to.write(" AND ");
            // cheat
            to.writeid(arg.getShadowOf().getName()).write(" = ").writeid("p_" + arg.getName());
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
