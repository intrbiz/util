package com.intrbiz.data.db.compiler.dialect.pgsql.function;

import com.intrbiz.Util;
import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.dialect.function.SQLFunctionGenerator;
import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Order;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.function.GetterInfo;
import com.intrbiz.data.db.compiler.util.SQLCommand;

public class GetterGenerator implements SQLFunctionGenerator
{    
    @Override
    public void writeCreateFunctionBody(SQLDialect dialect, SQLCommand to, Function function)
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
    
    protected void generateQuery(SQLDialect dialect, SQLCommand to, Function function)
    {
        GetterInfo info = (GetterInfo) function.getIntrospectionInformation();
        Table table = function.getTable();
        // build the SQL query
        to.write("SELECT ").writeColumnNameList(table.getColumns()).write(" FROM ").writeid(table.getSchema(), table.getName());
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
                    if (arg.isOptional())
                    {
                        to.write("(");
                        to.writeid(arg.getShadowOf().getName()).write(" = ").writeid("p_" + arg.getName());
                        to.write(" OR ");
                        to.writeid("p_" + arg.getName()).write(" IS NULL");
                        to.write(")");
                    }
                    else
                    {
                        to.writeid(arg.getShadowOf().getName()).write(" = ").writeid("p_" + arg.getName());
                    }
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
    
    public SQLCommand writefunctionBindingSQL(SQLDialect dialect, Function function)
    {
        GetterInfo info = (GetterInfo) function.getIntrospectionInformation();
        SQLCommand to = new SQLCommand();
        to.write("SELECT ").writeColumnNameList(function.getTable().getColumns()).write(" FROM ").writeid(function.getSchema(), function.getName()).write("(");
        boolean ns = false;
        for (Argument arg : function.getArguments())
        {
            if (ns) to.write(", ");
            to.write("?::").write(arg.getType().getSQLType());
            ns = true;
        }
        to.write(")");
        // order
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
        return to;
    }
}
