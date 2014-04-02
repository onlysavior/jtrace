package com.github.onlysavior.jtrace.store;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-2
 * Time: 下午11:13
 * To change this template use File | Settings | File Templates.
 */
public class MySQLStoreProvider implements TableStroreProvider {
    private static final String CREATE_TABLE_SQL = "aaa";
    private static final String UPDATE_SQL = "bbb";
    private static final String INSERT_SQL = "ccc";
    private static final String SELECT_SQL = "ddd";
    private static final String VISIT_SQL = "eee";

    private Connection connection;
    private PreparedStatement updatePs;
    private PreparedStatement insertPs;
    private PreparedStatement selectPs;

    public void start(String connectionInfo) throws SQLException {
        connection = DriverManager.getConnection(connectionInfo);
        updatePs = connection.prepareStatement(UPDATE_SQL);
        insertPs = connection.prepareStatement(INSERT_SQL);
        selectPs = connection.prepareStatement(SELECT_SQL);

        Statement createStament = connection.createStatement();
        createStament.execute(CREATE_TABLE_SQL);
    }

    public void stop() throws SQLException {
        updatePs.close();
        insertPs.close();
        selectPs.close();
        connection.close();
    }

    @Override
    public void updateOrStore(String rowKey, int rt) {
        try {
            updatePs.setNString(1, rowKey);
            updatePs.setInt(2, rt);

            int rtn = updatePs.executeUpdate();
            if (rtn == 0) {
                insertPs.setNString(1, rowKey);
                insertPs.setInt(2, rt);

                insertPs.execute();
            }
        } catch (SQLException e) {
            throw new StoreException(e);
        }
    }

    @Override
    public void updateOrStore(ByteBuffer rowKey, int rt) {
        String key = new String(rowKey.array());
        updateOrStore(key, rt);
    }

    @Override
    public List<Serializable> byId(String traceId) {
        try {
            selectPs.setNString(1, extractDate(traceId));
            selectPs.setNString(2, extractDate(traceId));

            ResultSet rs =  selectPs.executeQuery();
            return collect(rs, null);
        } catch (SQLException e) {
            throw new StoreException(e);
        }
    }

    @Override
    public List<Serializable> range(String min, String max) {
        try {
            selectPs.setNString(1, extractDate(min));
            selectPs.setNString(2, extractDate(max));

            ResultSet rs =  selectPs.executeQuery();
            return collect(rs, null);
        } catch (SQLException e) {
            throw new StoreException(e);
        }
    }

    @Override
    public List<Serializable> visit(VisitFunction function) {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(VISIT_SQL);
            return collect(rs, function);
        } catch (SQLException e) {
            throw new StoreException(e);
        }
    }

    private String extractDate(String traceId) {
        assert traceId != null;
        assert traceId.length() == 25;
        return traceId.substring(8,21);
    }

    private List<Serializable> collect(ResultSet rs, VisitFunction function) throws SQLException {
        List<Serializable> rtn = new LinkedList<Serializable>();
        if (rs != null) {
            while (rs.next()) {
                MySQLRow row = new MySQLRow();
                row.setTraceId(rs.getNString(1));
                row.setRt(rs.getInt(2));
                row.setQps(rs.getLong(3));

                if (function == null) {
                    rtn.add(row);
                } else {
                    rtn.add(function.visit(row));
                }
            }
            return rtn.size() > 0 ? rtn : null;
        }
        return null;
    }

    public static class MySQLRow implements Serializable {
        String traceId;
        int rt;
        long qps;

        public String getTraceId() {
            return traceId;
        }

        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }

        public int getRt() {
            return rt;
        }

        public void setRt(int rt) {
            this.rt = rt;
        }

        public long getQps() {
            return qps;
        }

        public void setQps(long qps) {
            this.qps = qps;
        }
    }
}
