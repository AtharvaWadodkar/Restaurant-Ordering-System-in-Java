import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;


public class Main {
    private Connection connection;
    private JFrame frame;
    private JComboBox<String> itemDropdown;
    private JComboBox<Integer> quantityDropdown;
    private JButton orderButton;
    private JButton placeOrderButton;
    private JLabel totalPriceLabel;
    private JLabel cumulativeTotalLabel;
    private double totalAmount = 0.0;
    private double cumulativeTotalAmount = 0.0; // Initialize cumulative total amount
    private DefaultListModel<String> orderedItemsModel;
    private JList<String> orderedItemsList;
    private ArrayList<OrderItem> orderedItems;

    public Main() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/restaurantdb", "root", "atharva#03");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to the database.");
            System.exit(1);
        }

        frame = new JFrame("Restaurant Ordering System");
        frame.setLayout(new BorderLayout());
        frame.setPreferredSize(new Dimension(600, 400)); // Increase the window size

        JPanel orderPanel = new JPanel();
        orderPanel.setLayout(new FlowLayout());

        String[] menuItems = {"Burger", "Pizza", "Pasta", "Ice Tea", "Chocolate Cake"}; // Replace with your menu items

        JLabel itemLabel = new JLabel("Select Item:");
        itemDropdown = new JComboBox<>(menuItems);

        JLabel quantityLabel = new JLabel("Select Quantity:");
        Integer[] quantities = {1, 2, 3, 4, 5};
        quantityDropdown = new JComboBox<>(quantities);

        orderButton = new JButton("Add to Order");
        orderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedItem = (String) itemDropdown.getSelectedItem();
                int quantity = (int) quantityDropdown.getSelectedItem();
                double itemPrice = getItemPrice(selectedItem);

                double orderTotal = itemPrice * quantity;
                totalAmount += orderTotal;
                cumulativeTotalAmount += orderTotal; // Update cumulative total amount

                orderedItemsModel.addElement(selectedItem + " (Qty: " + quantity + ") - ₹" + String.format("%.2f", orderTotal));
                orderedItems.add(new OrderItem(selectedItem, quantity, orderTotal));

                updateTotalPriceLabel();
                updateCumulativeTotalLabel();
            }
        });

        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new FlowLayout());

        totalPriceLabel = new JLabel("Total Price: ₹0.00");
        updateTotalPriceLabel();

        cumulativeTotalLabel = new JLabel("Cumulative Total: ₹0.00");
        updateCumulativeTotalLabel();

        placeOrderButton = new JButton("Place Order");
        placeOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeOrder();
            }
        });

        controlsPanel.add(itemLabel);
        controlsPanel.add(itemDropdown);
        controlsPanel.add(quantityLabel);
        controlsPanel.add(quantityDropdown);
        controlsPanel.add(orderButton);
        controlsPanel.add(placeOrderButton);
        controlsPanel.add(totalPriceLabel);
        controlsPanel.add(cumulativeTotalLabel);

        frame.add(controlsPanel, BorderLayout.SOUTH);

        orderedItemsModel = new DefaultListModel<>();
        orderedItemsList = new JList<>(orderedItemsModel);

        frame.add(new JScrollPane(orderedItemsList), BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        orderedItems = new ArrayList<>();
    }

    private double getItemPrice(String item) {
        // Replace with a method to get the price of the selected item from your menu
        switch (item) {
            case "Burger":
                return 200;
            case "Pizza":
                return 350;
            case "Pasta":
                return 300;
            case "Ice Tea":
                return 150;
            case "Chocolate Cake":
                return 200;
            default:
                return 0.0;
        }
    }

    private void updateTotalPriceLabel() {
        double totalPriceInRupees = totalAmount;
        totalPriceLabel.setText("Total Price: ₹" + String.format("%.2f", totalPriceInRupees));
    }

    private void updateCumulativeTotalLabel() {
        double cumulativeTotalInRupees = cumulativeTotalAmount;
        cumulativeTotalLabel.setText("Cumulative Total: ₹" + String.format("%.2f", cumulativeTotalInRupees));
    }

    private void placeOrder() {
        // Implement the code to place the order in the database here
        // You can use the 'orderedItems' ArrayList to access the selected items and quantities
        // Calculate the total price again before inserting into the database
        double orderTotal = 0.0;

        for (OrderItem item : orderedItems) {
            orderTotal += item.getPrice();
        }

        try {
            String sql = "INSERT INTO orders (item_name, quantity, item_price, total_price) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            for (OrderItem item : orderedItems) {
                statement.setString(1, item.getName());
                statement.setInt(2, item.getQuantity());
                statement.setDouble(3, item.getPrice());
                statement.setDouble(4, orderTotal);
                statement.executeUpdate();
            }

            JOptionPane.showMessageDialog(null, "Order placed successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to place the order.");
        }

        // Clear the ordered items list and update the GUI
        orderedItems.clear();
        orderedItemsModel.clear();
        totalAmount = 0.0;
        cumulativeTotalAmount = 0.0; // Reset cumulative total amount
        updateTotalPriceLabel();
        updateCumulativeTotalLabel();
    }

    class OrderItem {
        private String name;
        private int quantity;
        private double price;

        public OrderItem(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main());
    }
}
