package com.vastra.dao;

import com.vastra.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ProductDAO {

    public static void insertProduct(String prod_id, String prod_name, String variant,
                                     int mrp, int sellPrice, int gst, int stock) throws Exception {

        String sql = """
            INSERT INTO products(id, name, variant, mrp_cents, sell_price_cents, gst_percent, stock, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, datetime('now'))
        """;

        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, prod_id);
            ps.setString(2, prod_name);
            ps.setString(3, variant);
            ps.setInt(4, mrp);
            ps.setInt(5, sellPrice);
            ps.setInt(6, gst);
            ps.setInt(7, stock);
            ps.executeUpdate();
        }
    }
}
