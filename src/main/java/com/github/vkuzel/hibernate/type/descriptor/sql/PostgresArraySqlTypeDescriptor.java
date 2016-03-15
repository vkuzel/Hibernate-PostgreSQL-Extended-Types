package com.github.vkuzel.hibernate.type.descriptor.sql;

import com.github.vkuzel.hibernate.type.descriptor.java.PostgresArrayTypeDescriptor;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.BasicBinder;
import org.hibernate.type.descriptor.sql.BasicExtractor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

import java.sql.*;
import java.util.List;

public class PostgresArraySqlTypeDescriptor implements SqlTypeDescriptor {

    public static final PostgresArraySqlTypeDescriptor INSTANCE = new PostgresArraySqlTypeDescriptor();

    @Override
    public int getSqlType() {
        return Types.ARRAY;
    }

    private int getSqlType(Object array) {
        if (PostgresArrayTypeDescriptor.PgUnknownTypeArray.class.isInstance(array)) {
            return Types.OTHER;
        } else {
            return getSqlType();
        }
    }

    @Override
    public boolean canBeRemapped() {
        return true;
    }

    @Override
    public <X> ValueBinder<X> getBinder(JavaTypeDescriptor<X> javaTypeDescriptor) {
        return new BasicBinder<X>(javaTypeDescriptor, this) {
            @Override
            protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options) throws SQLException {
                if (value == null) {
                    st.setNull(index, getSqlType());
                } else if (PostgresArrayTypeDescriptor.class.isInstance(javaTypeDescriptor)) {
                    Object array = ((PostgresArrayTypeDescriptor) javaTypeDescriptor).unwrap(st.getConnection(), (List) value);
                    st.setObject(index, array, getSqlType(array));
                }
            }
        };
    }

    @Override
    public <X> ValueExtractor<X> getExtractor(JavaTypeDescriptor<X> javaTypeDescriptor) {
        return new BasicExtractor<X>(javaTypeDescriptor, this) {
            @Override
            protected X doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
                return wrap(rs.getObject(name), options);
            }

            @Override
            protected X doExtract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {
                return wrap(statement.getObject(index), options);
            }

            @Override
            protected X doExtract(CallableStatement statement, String name, WrapperOptions options) throws SQLException {
                return wrap(statement.getObject(name), options);
            }

            private X wrap(Object value, WrapperOptions options) {
                if (value == null) { // TODO Move this to javaTypeDescriptor...
                    return null;
                }
                return javaTypeDescriptor.wrap(value, options);
            }
        };
    }
}
