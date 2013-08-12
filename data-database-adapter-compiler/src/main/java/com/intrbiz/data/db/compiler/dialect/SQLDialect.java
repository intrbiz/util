package com.intrbiz.data.db.compiler.dialect;

import java.io.IOException;
import java.lang.annotation.Annotation;

import com.intrbiz.data.db.compiler.dialect.function.SQLFunctionGenerator;
import com.intrbiz.data.db.compiler.dialect.type.SQLType;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Schema;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.Type;
import com.intrbiz.data.db.compiler.util.SQLCommand;
import com.intrbiz.data.db.compiler.util.SQLScript;

public interface SQLDialect
{
    String getDialectName();
    
    //
    
    void registerFunctionGenerator(Class<? extends Annotation> type, SQLFunctionGenerator generator);
    
    //
    
    String getOwner();
    
    void setOwner(String owner);
    
    //
    
    SQLType getType(Class<?> javaClass);
    
    //
    
    SQLScript writeCreateSchema(Schema schema) throws IOException;
    
    SQLScript writeCreateTable(Table table) throws IOException;
    
    SQLScript writeCreateType(Type type) throws IOException;
    
    SQLScript writeCreateFunction(Function function) throws IOException;
    
    //
    
    SQLScript writeCreateSchemaNameFunction(Schema schema) throws IOException;
    
    SQLScript writeCreateSchemaVersionFunction(Schema schema) throws IOException;
    
    String getSchemaNameQuery(Schema schema);
    
    String getSchemaVersionQuery(Schema schema);
    
    //
    
    SQLCommand getFunctionCallQuery(Function function);
}
