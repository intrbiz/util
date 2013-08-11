package com.intrbiz.data.db.compiler.introspector.function;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.intrbiz.data.db.DatabaseAdapter;
import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.introspector.SQLIntrospector;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Schema;

public interface SQLFunctionIntrospector
{
    Function buildFunction(SQLIntrospector introspector, SQLDialect dialect, Method method, Annotation sqlFunction, Class<? extends DatabaseAdapter> cls, Schema schema);
}
