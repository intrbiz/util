package com.intrbiz.data.db.compiler.function;

import com.intrbiz.data.db.compiler.DatabaseAdapterCompiler;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.util.compiler.model.JavaMethod;

public interface FunctionCompiler
{
    void compileFunctionBinding(DatabaseAdapterCompiler compiler, JavaMethod method, Function function);
}
