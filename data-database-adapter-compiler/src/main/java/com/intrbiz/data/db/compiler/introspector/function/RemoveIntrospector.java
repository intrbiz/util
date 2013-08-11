package com.intrbiz.data.db.compiler.introspector.function;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.intrbiz.data.db.DatabaseAdapter;
import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.introspector.SQLIntrospector;
import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Column;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Schema;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.function.RemoveInfo;
import com.intrbiz.data.db.compiler.util.TextUtil;

public class RemoveIntrospector implements SQLFunctionIntrospector
{
    @Override
    public Function buildFunction(SQLIntrospector introspector, SQLDialect dialect, Method method, Annotation sqlFunction, Class<? extends DatabaseAdapter> cls, Schema schema)
    {
        Function function = new Function(schema, TextUtil.camelCaseToUnderscores(method.getName()), method, sqlFunction);
        RemoveInfo info = new RemoveInfo();
        function.setIntrospectionInformation(info);
        // must return void
        if (void.class != method.getReturnType()) throw new RuntimeException("The method " + method + " must return void.");
        // must be one argument
        if (method.getParameterTypes().length != 1) throw new RuntimeException("The method " + method + " must only have one parameter.");
        // the table type
        Class<?> tableCls = method.getParameterTypes()[0];
        if (! SQLIntrospector.isSQLTable(tableCls)) throw new RuntimeException("The single parameter of method " + method + " must be an SQLTable.");
        Table table = introspector.buildTable(dialect, tableCls, schema);
        info.setTable(table);
        // create arguments for every primary column in the table
        int idx = 0;
        for (Column col : table.getPrimaryKey().getColumns())
        {
            Argument arg = new Argument(idx++, col.getName(), col.getType(), col.getDefinition().getType());
            arg.setShadowOf(col);
            function.addArgument(arg);
        }
        //
        return function;
    }
}
