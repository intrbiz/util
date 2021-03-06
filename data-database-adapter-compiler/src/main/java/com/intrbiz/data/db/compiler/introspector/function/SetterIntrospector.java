package com.intrbiz.data.db.compiler.introspector.function;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.introspector.SQLIntrospector;
import com.intrbiz.data.db.compiler.meta.SQLSetter;
import com.intrbiz.data.db.compiler.meta.SQLUserDefined;
import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Column;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Schema;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.function.SetterInfo;
import com.intrbiz.data.db.compiler.util.UserDefinedUtil;

public class SetterIntrospector implements SQLFunctionIntrospector
{
    @Override
    public Function buildFunction(SQLIntrospector introspector, SQLDialect dialect, Method method, Annotation sqlFunction, Class<?> cls, Schema schema)
    {
        SQLSetter setter = (SQLSetter) sqlFunction;
        SQLIntrospector.assertSQLTable(setter.table());
        //
        Function function = new Function(schema, setter.name(), method, sqlFunction);
        SetterInfo info = new SetterInfo();
        function.setIntrospectionInformation(info);
        info.setUpsert(setter.upsert());
        // must return void
        if (void.class != method.getReturnType()) throw new RuntimeException("The method " + method + " must return void.");
        // must be one argument
        if (method.getParameterTypes().length != 1) throw new RuntimeException("The method " + method + " must have a single parameter of type " + setter.table().getCanonicalName() + ".");
        // check the argument type
        if (setter.table() != method.getParameterTypes()[0]) throw new RuntimeException("The method " + method + " must have a single parameter of type " + setter.table().getCanonicalName() + ".");
        // the table type
        Table table = introspector.buildTable(dialect, setter.table());
        function.setTable(table);
        // create arguments for every column in the table
        int idx = 0;
        for (Column col : table.getColumns())
        {
            Argument arg = new Argument(idx++, col.getName(), col.getType(), col.getDefinition().getType());
            arg.setShadowOf(col);
            function.addArgument(arg);
        }
        // user defined
        for (SQLUserDefined user : setter.userDefined())
        {
            info.addUserDefined(user.dialect(), UserDefinedUtil.buildSQL(cls, user));
        }
        //
        return function;
    }
}
