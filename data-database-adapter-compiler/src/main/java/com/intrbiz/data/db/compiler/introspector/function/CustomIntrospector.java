package com.intrbiz.data.db.compiler.introspector.function;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.introspector.SQLIntrospector;
import com.intrbiz.data.db.compiler.meta.SQLCustom;
import com.intrbiz.data.db.compiler.meta.SQLUserDefined;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Schema;
import com.intrbiz.data.db.compiler.model.function.CustomInfo;
import com.intrbiz.data.db.compiler.util.UserDefinedUtil;

public class CustomIntrospector implements SQLFunctionIntrospector
{
    @Override
    public Function buildFunction(SQLIntrospector introspector, SQLDialect dialect, Method method, Annotation sqlFunction, Class<?> cls, Schema schema)
    {
        SQLCustom custom = (SQLCustom) sqlFunction;
        //
        Function function = new Function(schema, method.getName(), method, sqlFunction);
        CustomInfo info = new CustomInfo();
        function.setIntrospectionInformation(info);
        // user defined
        for (SQLUserDefined user : custom.userDefined())
        {
            info.addUserDefined(user.dialect(), UserDefinedUtil.buildSQL(cls, user));
        }
        if (! info.hasUserDefined()) throw new RuntimeException("A custom SQL function must provide at least one user defined value");
        //
        return function;
    }
}
