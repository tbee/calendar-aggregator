package org.tbee.webstack.postgresql;

import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.type.SqlTypes;

public class CustomPostgreSQLDialect extends PostgreSQLDialect {

    @Override
    protected String columnType(int sqlTypeCode) {
        // Map CLOB to TEXT so the @Lob annotation works correctly
        if (sqlTypeCode == SqlTypes.CLOB) {
            return "text";
        }
        return super.columnType(sqlTypeCode);
    }
}