package com.intrbiz.data.db.compiler.dialect.function;

import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.util.SQLCommand;

public interface SQLFunctionGenerator
{
    void writeCreateFunctionBody(SQLDialect dialect, SQLCommand to, Function function);
    
    SQLCommand writefunctionBindingSQL(SQLDialect dialect, Function function);
}
