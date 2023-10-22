package lk.ijse.dep11.pos.db;

import lk.ijse.dep11.pos.tm.Item;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemDataAccess {

    private static final PreparedStatement STM_INSERT;
    private static final PreparedStatement STM_UPDATE;
    private static final PreparedStatement STM_DELETE;
    private static final PreparedStatement STM_GET_ALL;
    private static final PreparedStatement STM_GET_CODE;

    static {
        try {
            Connection connection = SingleConnectionDataSource.getInstance().getConnection();
            STM_INSERT = connection.prepareStatement
                    ("INSERT INTO item (code, description, qty, unit_price) VALUES (?, ?, ?, ?)");
            STM_UPDATE = connection.prepareStatement
                    ("UPDATE item SET description=?, qty=?, unit_price=? WHERE code=?");
            STM_DELETE = connection.prepareStatement("DELETE FROM item WHERE code=?");
            STM_GET_ALL = connection.prepareStatement("SELECT * FROM item ORDER BY code");
            STM_GET_CODE = connection.prepareStatement("SELECT code FROM item");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveItem(Item item) throws SQLException {
        STM_INSERT.setString(1, item.getCode());
        STM_INSERT.setString(2, item.getDescription());
        STM_INSERT.setInt(3, item.getQty());
        STM_INSERT.setBigDecimal(4, item.getUnitPrice());
        STM_INSERT.executeUpdate();
    }

    public static void updateItem(Item item) throws SQLException {
        STM_UPDATE.setString(1, item.getDescription());
        STM_UPDATE.setInt(2, item.getQty());
        STM_UPDATE.setBigDecimal(3, item.getUnitPrice());
        STM_UPDATE.setString(4, item.getCode());
        STM_UPDATE.executeUpdate();
    }

    public static void deleteItem(String code) throws SQLException {
        STM_DELETE.setString(1, code);
        STM_DELETE.executeUpdate();
    }

    public static List<Item> getAllItems() throws SQLException {
        ResultSet rst = STM_GET_ALL.executeQuery();
        List<Item> itemList = new ArrayList<>();
        while (rst.next()) {
            String code = rst.getString("code");
            String description = rst.getString("description");
            int qty = rst.getInt("qty");
            BigDecimal unitPrice = rst.getBigDecimal("unit_price");
            itemList.add(new Item(code, description, qty, unitPrice));
        }
        return itemList;
    }

    public static boolean isCodeExists(String code) throws SQLException {
        ResultSet rst = STM_GET_CODE.executeQuery();
        while (rst.next()) {
            if(rst.getString("code").equals(code)) return true;
        }
        return false;
    }
}
