package com.intrbiz.data.db.compiler.introspector.function;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.intrbiz.data.db.DatabaseAdapter;
import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.dialect.type.SQLCompositeType;
import com.intrbiz.data.db.compiler.dialect.type.SQLType;
import com.intrbiz.data.db.compiler.introspector.SQLIntrospector;
import com.intrbiz.data.db.compiler.meta.SQLGetter;
import com.intrbiz.data.db.compiler.meta.SQLLimit;
import com.intrbiz.data.db.compiler.meta.SQLOffset;
import com.intrbiz.data.db.compiler.meta.SQLOrder;
import com.intrbiz.data.db.compiler.meta.SQLParam;
import com.intrbiz.data.db.compiler.meta.SQLQuery;
import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Column;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Order;
import com.intrbiz.data.db.compiler.model.Schema;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.Type;
import com.intrbiz.data.db.compiler.model.function.GetterInfo;
import com.intrbiz.data.db.compiler.util.TextUtil;

public class GetterIntrospector implements SQLFunctionIntrospector
{
    @Override
    public Function buildFunction(SQLIntrospector introspector, SQLDialect dialect, Method method, Annotation sqlFunction, Class<? extends DatabaseAdapter> cls, Schema schema)
    {
        Function function = new Function(schema, TextUtil.camelCaseToUnderscores(method.getName()), method, sqlFunction);
        GetterInfo info = new GetterInfo();
        function.setIntrospectionInformation(info);
        //
        SQLGetter getter = (SQLGetter) sqlFunction;
        //
        // return type should be a table type
        if (void.class == method.getReturnType() || method.getReturnType() == null) throw new RuntimeException("The method " + method + " must return something.");
        function.setReturnsList(SQLIntrospector.returnsList(method));
        Class<?> returnType = SQLIntrospector.functionReturnType(method);
        Type type = introspector.buildType(dialect, returnType, schema);
        Table table = introspector.buildTable(dialect, returnType, schema);
        function.setReturnType(new SQLCompositeType(type, returnType));
        info.setTable(table);
        // arguments
        Class<?>[] argTypes = method.getParameterTypes();
        Annotation[][] argAnnotations = method.getParameterAnnotations();
        int idx = 0;
        for (int i = 0; i < argTypes.length; i++)
        {
            SQLParam param = SQLIntrospector.getParameterAnnotation(argAnnotations[i], SQLParam.class);
            SQLOffset offset = SQLIntrospector.getParameterAnnotation(argAnnotations[i], SQLOffset.class);
            SQLLimit limit = SQLIntrospector.getParameterAnnotation(argAnnotations[i], SQLLimit.class);
            //
            if (param != null)
            {
                Class<?> argType = argTypes[i];
                SQLType sqlType = dialect.getType(argType);
                // find the column
                Column col = table.findColumn(param.value());
                if (col == null) throw new RuntimeException("The parameter " + param.value() + " of method " + method + " has no corresponding column in table " + table.getName());
                // add the argument
                function.addArgument(new Argument(idx++, param.value(), sqlType, argType, col));
                info.setParameterised(true);
            }
            else if (offset != null)
            {
                if (long.class != argTypes[i] && Long.class != argTypes[i]) throw new RuntimeException("The SQLOffest parameter must be an long");
                function.addArgument(new Argument(idx++, "offset", dialect.getType(long.class), long.class));
                info.setOffset(true);
            }
            else if (limit != null)
            {
                if (long.class != argTypes[i] && Long.class != argTypes[i]) throw new RuntimeException("The SQLLimit parameter must be a long");
                function.addArgument(new Argument(idx++, "limit", dialect.getType(long.class), long.class));
                info.setLimit(true);
            }
            else
            {
                throw new RuntimeException("The parameter " + i + " of method " + method + " is not annotated!");
            }
        }
        // order by
        for (SQLOrder order : getter.orderBy())
        {
            Column col = table.findColumn(order.value());
            if (col == null) throw new RuntimeException("Cannot order by column " + order.value() + " it does not exist on " + table.getName() + " on method " + method);
            info.addOrderBy(new Order(col, order.direction(), order.nulls()));
        }
        // custom query
        for (SQLQuery query : getter.query())
        {
            info.addQuery(query.dialect(), query.value());
        }
        //
        return function;
    }
}
