package lk.ijse.dep11.pos.db;

import lk.ijse.dep11.pos.tm.Order;
import lk.ijse.dep11.pos.tm.OrderItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDataAccess {

    private static final PreparedStatement STM_EXISTS_BY_CUSTOMER_ID;
    private static final PreparedStatement STM_EXISTS_BY_ITEM_CODE;
    private static final PreparedStatement STM_GET_LAST_ID;
    private static final PreparedStatement STM_INSERT_ORDER;
    private static final PreparedStatement STM_INSERT_ORDER_ITEM;
    private static final PreparedStatement STM_UPDATE_STOCK;
    private static final PreparedStatement STM_FIND;

    static {
        try {
            Connection connection = SingleConnectionDataSource.getInstance().getConnection();
            STM_EXISTS_BY_CUSTOMER_ID = connection.prepareStatement
                    ("SELECT * FROM \"order\" WHERE customer_id = ?");
            STM_EXISTS_BY_ITEM_CODE = connection.prepareStatement
                    ("SELECT * FROM order_item WHERE item_code = ?");
            STM_GET_LAST_ID = connection.prepareStatement
                    ("SELECT id FROM \"order\" ORDER BY id DESC FETCH FIRST ROWS ONLY");
            STM_INSERT_ORDER = connection.prepareStatement
                    ("INSERT INTO \"order\" (id, date, customer_id) VALUES (?,?,?)");
            STM_INSERT_ORDER_ITEM = connection.prepareStatement
                    ("INSERT INTO order_item (order_id, item_code, qty, unit_price) VALUES (?,?,?,?)");
            STM_UPDATE_STOCK = connection.prepareStatement
                    ("UPDATE item SET qty = qty - ? WHERE code = ?");
            STM_FIND = connection.prepareStatement
                    ("SELECT o.*,c.name, CAST(order_total.total AS DECIMAL(8,2)) FROM \"order\" AS o\n" +
                            "    INNER JOIN customer AS c ON o.customer_id = c.id\n" +
                            "    INNER JOIN\n" +
                            "(SELECT o.id, SUM(qty * unit_price) AS total\n" +
                            "FROM \"order\" AS o\n" +
                            "    INNER JOIN order_item AS oi ON oi.order_id = o.id GROUP BY o.id) AS order_total\n" +
                            "ON o.id = order_total.id\n" +
                            "WHERE o.id LIKE ? OR CAST(o.date AS VARCHAR(20)) LIKE ? OR o.customer_id LIKE ? OR c.name LIKE ? "+
                            "ORDER BY o.id");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static List<Order> findOrders(String query) throws SQLException {
        for (int i = 1; i <= 4; i++) {
            STM_FIND.setString(i, "%".concat(query).concat("%"));
        }
        ResultSet rst = STM_FIND.executeQuery();
        ArrayList<Order> orderList = new ArrayList<>();
        while (rst.next()) {
            String orderId = rst.getString("id");
            Date orderDate = rst.getDate("date");
            String customerId = rst.getString("customer_id");
            String customerName = rst.getString("name");
            BigDecimal orderTotal = rst.getBigDecimal("total");
            orderList.add(new Order(orderId, orderDate.toString(), customerId, customerName, orderTotal));
        }
        return orderList;
    }

    public static void saveOrder(String orderId, Date orderDate, String customerId,
                                 List<OrderItem> orderItemList) throws SQLException {
        SingleConnectionDataSource.getInstance().getConnection().setAutoCommit(false);

        try{
            STM_INSERT_ORDER.setString(1, orderId);
            STM_INSERT_ORDER.setDate(2, orderDate);
            STM_INSERT_ORDER.setString(3, customerId);
            STM_INSERT_ORDER.executeUpdate();

            for (OrderItem orderItem : orderItemList) {
                STM_INSERT_ORDER_ITEM.setString(1, orderId);
                STM_INSERT_ORDER_ITEM.setString(2, orderItem.getCode());
                STM_INSERT_ORDER_ITEM.setInt(3, orderItem.getQty());
                STM_INSERT_ORDER_ITEM.setBigDecimal(4, orderItem.getUnitPrice());
                STM_INSERT_ORDER_ITEM.executeUpdate();

                STM_UPDATE_STOCK.setInt(1, orderItem.getQty());
                STM_UPDATE_STOCK.setString(2, orderItem.getCode());
                STM_UPDATE_STOCK.executeUpdate();
            }
            SingleConnectionDataSource.getInstance().getConnection().commit();

        } catch (Throwable t) {
            SingleConnectionDataSource.getInstance().getConnection().rollback();
            throw new SQLException(t);
        } finally {
            SingleConnectionDataSource.getInstance().getConnection().setAutoCommit(true);
        }
    }

    public static String getLastOrderId() throws SQLException {
        ResultSet rst = STM_GET_LAST_ID.executeQuery();
        return (rst.next()) ? rst.getString(1) : null;
    }


    public static boolean existsOrderByCustomerId(String customerId) throws SQLException {
        STM_EXISTS_BY_CUSTOMER_ID.setString(1, customerId);
        return STM_EXISTS_BY_CUSTOMER_ID.executeQuery().next();
    }

    public static boolean existsOrderByItemCode(String code) throws SQLException {
        STM_EXISTS_BY_ITEM_CODE.setString(1, code);
        return STM_EXISTS_BY_ITEM_CODE.executeQuery().next();
    }

}
