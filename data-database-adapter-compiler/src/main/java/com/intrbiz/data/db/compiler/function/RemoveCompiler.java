package com.intrbiz.data.db.compiler.function;

import com.intrbiz.Util;
import com.intrbiz.data.db.compiler.DatabaseAdapterCompiler;
import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Column;
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
        s.append("this.");
        if (compiler.isWithMetrics()) s.append("useTimed(this.").append(metricField.getName()).append(", ");
        else s.append("use(");
        /*s.append("new DatabaseCall<Object>() {\r\n");*/
        s.append("(with) -> {\r\n");
        /*s.append("  public Object run(final Connection with) throws SQLException, DataException {\r\n");*/
        //
        s.append("    try (PreparedStatement stmt = with.prepareStatement(\"").append(JavaUtil.escapeString(compiler.getDialect().getFunctionCallQuery(function).toString())).append("\"))\r\n");
        s.append("    {\r\n");
        // bind params
        int idx = 0;
        for (Argument arg : function.getArguments())
        {

            arg.getType().addImports(method.getJavaClass().getImports());
            s.append("      ").append(arg.getType().setBinding(idx + 1, DatabaseAdapterCompiler.applyAdapter(method.getJavaClass(), Util.nullable(arg.getShadowOf(), Column::getAdapter), false, "p" + idx))).append(";\r\n");
            idx++;
        }
        // execute
        s.append("      stmt.execute();\r\n");
        s.append("    }\r\n");
        s.append("    return null;\r\n");
        //
        /*s.append("  }\r\n");*/
        s.append("});\r\n");
        // clean up the cache
        if (function.isCacheable())
        {
            // TODO optimise for when we remove by primary key
            if (function.isAllArgumentsPrimaryKey())
            {
                s.append("this.getAdapterCache().remove(\"").append(JavaUtil.escapeString(function.getTable().getName())).append(".\"");
                boolean ns = false;
                for (Column column : function.getTable().getPrimaryKey().getColumns())
                {
                    idx = 0;
                    for (Argument arg : function.getArguments())
                    {
                        if (column.equals(arg.getShadowOf()))
                        {
                            if (ns) s.append(" + \".\"");
                            s.append(" + p").append(idx);
                            ns = true;
                            break;
                        }
                        idx++;
                    }
                }
                s.append(");");
            }
            else
            {
                // remove all objects starting with the table prefix
                s.append("this.getAdapterCache().removePrefix(\"").append(JavaUtil.escapeString(function.getTable().getName())).append(".\");\r\n");
            }
        }
        // invalidate
        for (final String invalidate : function.getCacheInvalidate())
        {
            // prefix?
            boolean isPrefix = invalidate.endsWith("*");
            final String expression = isPrefix ? invalidate.substring(0, invalidate.length() - 1) : invalidate;
            // process the expression
            String key = DatabaseAdapterCompiler.compileCacheInvalidationExpression(expression, (col) -> {
                int pidx = 0;
                for (Argument arg : function.getArguments())
                {
                    if (col.equals(arg.getName()))
                    {
                        return "p" + pidx;
                    }
                    pidx ++;
                }
                throw new RuntimeException("Could not find column " + col + ", used by cache invalidation key: " + invalidate);
            });
            // remove
            s.append("this.getAdapterCache().remove").append(isPrefix ? "Prefix" : "").append("(").append(key).append(");\r\n");
        }
    }

    
}
