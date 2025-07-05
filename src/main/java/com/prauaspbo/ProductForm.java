package com.prauaspbo;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.google.gson.Gson;

public class ProductForm extends JFrame {
    private JTable table;
    private JTextField tfName = new JTextField();
    private JTextField tfPrice = new JTextField();
    private JTextField tfCategory = new JTextField();
    private JTextArea outputArea = new JTextArea(10, 30);
    private DefaultTableModel tableModel;

    public ProductForm() {
        setTitle("GraphQL Product Form");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // tabel model
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Category"}, 0);
        table = new JTable(tableModel);

        //input panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(tfName);
        inputPanel.add(new JLabel("Price:"));
        inputPanel.add(tfPrice);
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(tfCategory);

        JPanel btnPanel = new JPanel(new GridLayout(1, 4));
        JButton btnAdd = new JButton("Add Product");
        JButton btnEdit = new JButton("Edit Product");
        JButton btnDelete = new JButton("Delete Product");
        JButton btnFetch = new JButton("Show All");
        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        btnPanel.add(btnFetch);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(inputPanel);
        topPanel.add(btnPanel);
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(new JScrollPane(outputArea), BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> tambahProduk());
        btnEdit.addActionListener(e -> ubahProduk());
        btnDelete.addActionListener(e -> hapusProduk());
        btnFetch.addActionListener(e -> ambilSemuaProduk());

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        table.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                tfName.setText(String.valueOf(tableModel.getValueAt(selectedRow, 1)));
                tfPrice.setText(String.valueOf(tableModel.getValueAt(selectedRow, 2)));
                tfCategory.setText(String.valueOf(tableModel.getValueAt(selectedRow, 3)));
            }
        });
    }

    private void tambahProduk() {
        try {
            String query = String.format(
                "mutation { addProduct(name: \"%s\", price: %s, category: \"%s\") { id name } }",
                tfName.getText(),
                tfPrice.getText(),
                tfCategory.getText()
            );

            String jsonRequest = new Gson().toJson(new GraphQLQuery(query));
            String response = sendGraphQLRequest(jsonRequest);
            outputArea.setText("Product added!\n\n" + response);
            tfName.setText("");
            tfPrice.setText("");
            tfCategory.setText("");
            loadData();
        } catch (Exception e) {
            outputArea.setText("Error: " + e.getMessage());
        }
    }

    private void ubahProduk() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) { // jika menekan tombol edit tanpa menyeleksi barisnya
            JOptionPane.showMessageDialog(this, "Pilih produk yang ingin diubah!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String id = tableModel.getValueAt(selectedRow, 0).toString();
        
        try {
            String query = String.format(
                "mutation { updateProduct(id: \"%s\", name: \"%s\", price: %s, category: \"%s\") { id name } }",
                id,
                tfName.getText(),
                tfPrice.getText(),
                tfCategory.getText()
            );
            
            String jsonRequest = new Gson().toJson(new GraphQLQuery(query));
            String response = sendGraphQLRequest(jsonRequest);
            outputArea.setText("Product updated!\n\n" + response);
            tfName.setText("");
            tfPrice.setText("");
            tfCategory.setText("");
            loadData();
        } catch (Exception e) {
            outputArea.setText("Error: " + e.getMessage());
        }
    }

    private void hapusProduk() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih produk yang ingin dihapus!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String id = tableModel.getValueAt(selectedRow, 0).toString();
        
        try {
            String query = String.format(
                "mutation { deleteProduct(id: \"%s\") { id name } }",
                id
            );

            String jsonRequest = new Gson().toJson(new GraphQLQuery(query));
            String response = sendGraphQLRequest(jsonRequest);
            outputArea.setText("Product deleted!\n\n" + response);
            tfName.setText("");
            tfPrice.setText("");
            tfCategory.setText("");
            loadData();
        } catch (Exception e) {
            outputArea.setText("Error: " + e.getMessage());
        }
    }

    private void ambilSemuaProduk() {
        try {
            String query = "query { allProducts { id name price category } }";
            String jsonRequest = new Gson().toJson(new GraphQLQuery(query));
            String response = sendGraphQLRequest(jsonRequest);
            outputArea.setText(response);
            loadData();
        } catch (Exception e) {
            outputArea.setText("Error: " + e.getMessage());
        }
    }

    private String sendGraphQLRequest(String json) throws Exception {
        URL url = new URL("http://localhost:4567/graphql");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        }
    }

    private void loadData() {
    try {
        String query = "query { allProducts { id name price category } }";
        String jsonRequest = new Gson().toJson(new GraphQLQuery(query));
        String response = sendGraphQLRequest(jsonRequest);

        var gson = new Gson();
        var map = gson.fromJson(response, java.util.Map.class);
        var data = (java.util.Map) map.get("data");
        var products = (java.util.List) data.get("allProducts");

        tableModel.setRowCount(0); // kosongkan tabel
        for (Object obj : products) {
            var product = (java.util.Map) obj;
            tableModel.addRow(new Object[]{
                product.get("id"),
                product.get("name"),
                product.get("price"),
                product.get("category")
            });
        }
    } catch (Exception e) {
        outputArea.setText("Error: " + e.getMessage());
    }
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ProductForm::new);
    }

    class GraphQLQuery {
        String query;
        GraphQLQuery(String query) {
            this.query = query;
        }
    }
}