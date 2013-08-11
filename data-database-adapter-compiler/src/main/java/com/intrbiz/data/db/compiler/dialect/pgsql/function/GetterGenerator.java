package com.intrbiz.data.db.compiler.dialect.pgsql.function;

import java.io.IOException;

import com.intrbiz.Util;
import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.dialect.function.SQLFunctionGenerator;
import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Order;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.function.GetterInfo;
import com.intrbiz.data.db.compiler.util.SQLWriter;

public class GetterGenerator implements SQLFunctionGenerator
{
    @Override
    public void writeCreateFunctionBody(SQLDialect dialect, SQLWriter to, Function function) throws IOException
    {
        GetterInfo info = (GetterInfo) function.getIntrospectionInformation();
        //
        to.writeln("DECLARE");
        to.writeln("BEGIN");
        //
        to.write("  RETURN QUERY ");
        if (info.hasQuery())
        {
            String query = info.getQuery(dialect.getDialectName());
            if (Util.isEmpty(query)) throw new RuntimeException("The function " + function.getName() + " has no query for the dialect " + dialect.getDialectName());
            to.write(query);
            to.writeln(";");
        }
        else
        {
            this.generateQuery(dialect, to, function);
        }
        //
        to.writeln("END;");
    }
    
    protected void generateQuery(SQLDialect dialect, SQLWriter to, Function function) throws IOException
    {
        GetterInfo info = (GetterInfo) function.getIntrospectionInformation();
        Table table = info.getTable();
        // build the SQL query
        to.write("SELECT * FROM ").writeid(table.getSchema(), table.getName());
        // where clause
        if (info.isParameterised())
        {
            to.write(" WHERE ");
            boolean ns = false;
            for (Argument arg : function.getArguments())
            {
                if (arg.getShadowOf() != null)
                {
                    if (ns) to.write(" AND ");
                    to.writeid(arg.getShadowOf().getName()).write(" = ").writeid("p_" + arg.getName());
                    ns = true;
                }
            }
        }
        // order
        if (! info.getOrderBy().isEmpty())
        {
            to.write(" ORDER BY ");
            boolean ns = false;
            for (Order order : info.getOrderBy())
            {
                if (ns) to.write(", ");
                to.writeid(order.getColumn().getName()).write(" ").write(order.getDirection().toString()).write(" NULLS ").write(order.getNulls().toString());
                ns = true;
            }
        }
        // paging
        if (info.isOffset())
        {
            to.write(" OFFSET p_offset");
        }
        if (info.isLimit())
        {
            to.write(" LIMIT p_limit");
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
        // order
        GetterInfo info = (GetterInfo) function.getIntrospectionInformation();
        if (! info.getOrderBy().isEmpty())
        {
            to.write(" ORDER BY ");
            ns = false;
            for (Order order : info.getOrderBy())
            {
                if (ns) to.write(", ");
                to.writeid(order.getColumn().getName()).write(" ").write(order.getDirection().toString()).write(" NULLS ").write(order.getNulls().toString());
                ns = true;
            }
        }
    }
}
