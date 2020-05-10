package com.intrbiz.data.db.compiler.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SQLPartition
{
    /**
     * The table partitioning mode
     */
    PartitionMode mode() default PartitionMode.RANGE;
    
    /**
     * The column names to partition on
     */
    String[] on();
    
    /**
     * Should partitions have an index on the partitioned column
     */
    boolean indexOn() default false;
    
    /**
     * That index type should be used when indexing the partitioned column
     */
    String indexOnUsing() default "";
}
