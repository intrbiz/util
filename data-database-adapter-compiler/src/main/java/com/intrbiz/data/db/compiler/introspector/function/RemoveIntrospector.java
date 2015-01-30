package com.intrbiz.data.db.compiler.introspector.function;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.dialect.type.SQLType;
import com.intrbiz.data.db.compiler.introspector.SQLIntrospector;
import com.intrbiz.data.db.compiler.meta.SQLParam;
import com.intrbiz.data.db.compiler.meta.SQLQuery;
import com.intrbiz.data.db.compiler.meta.SQLRemove;
import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Column;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Schema;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.function.RemoveInfo;

public class RemoveIntrospector implements SQLFunctionIntrospector
{
    @Override
    public Function buildFunction(SQLIntrospector introspector, SQLDialect dialect, Method method, Annotation sqlFunction, Class<?> cls, Schema schema)
    {
        SQLRemove remove = (SQLRemove) sqlFunction;
        SQLIntrospector.assertSQLTable(remove.table());
        //
        Function function = new Function(schema, remove.name(), method, sqlFunction);
        RemoveInfo info = new RemoveInfo();
        function.setIntrospectionInformation(info);
        // must return void
        if (void.class != method.getReturnType()) throw new RuntimeException("The method " + method + " must return void.");
        // table type
        Class<?> tableCls = remove.table();
        Table table = introspector.buildTable(dialect, tableCls);
        function.setTable(table);
        // the arguments are SQLParams to delete by
        Class<?>[] argTypes = method.getParameterTypes();
        Annotation[][] argAnnotations = method.getParameterAnnotations();
        int idx = 0;
        for (int i = 0; i < argTypes.length; i++)
        {
            SQLParam param = SQLIntrospector.getParameterAnnotation(argAnnotations[i], SQLParam.class);
            //
            if (param != null)
            {
                Class<?> argType = argTypes[i];
                SQLType sqlType = dialect.getType(argType);
                // find the column
                Column col = table.findColumn(param.value());
                if (col == null) throw new RuntimeException("The parameter " + param.value() + " of method " + method + " has no corresponding column in table " + table.getName() + ".");
                // add the argument
                Argument arg = new Argument(idx++, param.value(), sqlType, argType, col);
                arg.setOptional(param.optional());
                function.addArgument(arg);
            }
            else
            {
                throw new RuntimeException("The parameter " + i + " of method " + method + " is not annotated!");
            }
        }
        // custom query
        for (SQLQuery query : remove.query())
        {
            info.addQuery(query.dialect(), query.value());
        }
        return function;
    }
}
