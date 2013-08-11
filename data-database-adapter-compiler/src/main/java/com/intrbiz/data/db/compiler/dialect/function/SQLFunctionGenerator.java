package com.intrbiz.data.db.compiler.dialect.function;

import java.io.IOException;

import com.intrbiz.data.db.compiler.dialect.SQLDialect;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.util.SQLWriter;

public interface SQLFunctionGenerator
{
    void writeCreateFunctionBody(SQLDialect dialect, SQLWriter to, Function function) throws IOException;
    
    void writefunctionBindingSQL(SQLDialect dialect, SQLWriter to, Function function) throws IOException;
}
