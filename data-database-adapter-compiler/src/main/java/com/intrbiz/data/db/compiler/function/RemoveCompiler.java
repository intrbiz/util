package com.intrbiz.data.db.compiler.function;

import com.intrbiz.data.db.compiler.DatabaseAdapterCompiler;
import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.util.compiler.model.JavaField;
import com.intrbiz.util.compiler.model.JavaMethod;
import com.intrbiz.util.compiler.util.JavaUtil;

public class RemoveCompiler implements FunctionCompiler
{

    @Override
    public void compileFunctionBinding(DatabaseAdapterCompiler compiler, JavaMethod method, Function function)
    {
        StringBuilder s = method.getCode();
        // metrics
        JavaField  metricField = null;
        if (compiler.isWithMetrics()) metricField = DatabaseAdapterCompiler.addMetricField(method, function);
        //
        s.append("this.connection.");
        if (compiler.isWithMetrics()) s.append("useTimed(this.").append(metricField.getName()).append(", ");
        else s.append("use(");
        s.append("new DatabaseCall<Object>() {\r\n");
        s.append("  public Object run(final Connection with) throws SQLException, DataException {\r\n");
        //
        s.append("    try (PreparedStatement stmt = with.prepareStatement(\"").append(JavaUtil.escapeString(compiler.getDialect().getFunctionCallQuery(function).toString())).append("\"))\r\n");
        s.append("    {\r\n");
        // bind params
        int idx = 0;
        for (Argument arg : function.getArguments())
        {

            arg.getType().addImports(method.getJavaClass().getImports());
            s.append("      ").append(arg.getType().setBinding(idx + 1, "p" + idx)).append(";\r\n");
            idx++;
        }
        // execute
        s.append("      stmt.execute();\r\n");
        s.append("    }\r\n");
        s.append("    return null;\r\n");
        //
        s.append("  }\r\n");
        s.append("});\r\n");
    }

}
