import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sahayak_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456"; // Change to your MySQL password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "MySQL JDBC Driver not found!", 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}

class ServiceProvider {
    private int id;
    private String name;
    private String serviceType;
    private float hourlyRate;
    private long contactNo;
    private String email;
    private String address;
    private String skills;

    public ServiceProvider(int id, String name, String serviceType, float hourlyRate, 
                           long contactNo, String email, String address, String skills) {
        this.id = id;
        this.name = name;
        this.serviceType = serviceType;
        this.hourlyRate = hourlyRate;
        this.contactNo = contactNo;
        this.email = email;
        this.address = address;
        this.skills = skills;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getServiceType() { return serviceType; }
    public float getHourlyRate() { return hourlyRate; }
    public long getContactNo() { return contactNo; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getSkills() { return skills; }

    @Override
    public String toString() {
        return String.format(
            "Adhaar ID: %d, Name: %s, Sahayata: %s, Rate: ₹%.2f/hr, Contact: %d, Email: %s, Address: %s, Skills: %s", 
            id, name, serviceType, hourlyRate, contactNo, email, address, skills
        );
    }
}

class ServiceProviderManager {
    public void addServiceProvider(ServiceProvider sp) throws SQLException {
        String sql = "INSERT INTO service_providers (id, name, service_type, hourly_rate, contact_no, email, address, skills) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, sp.getId());
            pstmt.setString(2, sp.getName());
            pstmt.setString(3, sp.getServiceType());
            pstmt.setFloat(4, sp.getHourlyRate());
            pstmt.setLong(5, sp.getContactNo());
            pstmt.setString(6, sp.getEmail());
            pstmt.setString(7, sp.getAddress());
            pstmt.setString(8, sp.getSkills());
            
            pstmt.executeUpdate();
        }
    }

    public ServiceProvider searchServiceProvider(int id) throws SQLException {
        String sql = "SELECT * FROM service_providers WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new ServiceProvider(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("service_type"),
                    rs.getFloat("hourly_rate"),
                    rs.getLong("contact_no"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getString("skills")
                );
            }
        }
        return null;
    }

    public List<ServiceProvider> searchByServiceType(String serviceType) throws SQLException {
        List<ServiceProvider> providers = new ArrayList<>();
        String sql = "SELECT * FROM service_providers WHERE service_type LIKE ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + serviceType + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                providers.add(new ServiceProvider(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("service_type"),
                    rs.getFloat("hourly_rate"),
                    rs.getLong("contact_no"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getString("skills")
                ));
            }
        }
        return providers;
    }

    public boolean deleteServiceProvider(int id) throws SQLException {
        String sql = "DELETE FROM service_providers WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public List<ServiceProvider> getAllServiceProviders() throws SQLException {
        List<ServiceProvider> providers = new ArrayList<>();
        String sql = "SELECT * FROM service_providers ORDER BY name";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                providers.add(new ServiceProvider(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("service_type"),
                    rs.getFloat("hourly_rate"),
                    rs.getLong("contact_no"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getString("skills")
                ));
            }
        }
        return providers;
    }
}

public class Sahayak extends JFrame {
    private ServiceProviderManager manager;
    private JPanel mainPanel;
    private JLabel titleLabel;
    private Color[] buttonColors = {
        new Color(52, 152, 219),   // Blue
        new Color(46, 204, 113),   // Green
        new Color(231, 76, 60),    // Red
        new Color(241, 196, 15),   // Yellow
        new Color(127, 140, 141),  // Gray
        new Color(52, 152, 219)    // Cyan
    };

    public Sahayak() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        manager = new ServiceProviderManager();

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Sahayata - Service Providers Management");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        // setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage()); // Commented out to avoid null resource error

        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(240, 240, 250), 
                    0, getHeight(), new Color(200, 220, 240)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Decorative elements
                g2d.setColor(new Color(255, 255, 255, 30));
                for (int i = 0; i < 20; i++) {
                    int x = (int) (Math.random() * getWidth());
                    int y = (int) (Math.random() * getHeight());
                    int size = (int) (Math.random() * 50 + 20);
                    g2d.fillOval(x, y, size, size);
                }
                
                g2d.dispose();
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // Title with icon
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        titlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // You would need to add an icon file to your project resources
        // ImageIcon icon = new ImageIcon(getClass().getResource("/sahayak-icon.png"));
        // JLabel iconLabel = new JLabel(icon);
        // titlePanel.add(iconLabel);
        
        titleLabel = new JLabel("Sahayata Service Providers");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(33, 47, 61));
        titlePanel.add(titleLabel);
        
        mainPanel.add(titlePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        addButtons();
        add(mainPanel);
        
        try {
            setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addButtons() {
        JButton addBtn = createStyledButton("Add New Sahayak", e -> addServiceProvider(), buttonColors[0]);
        JButton searchIdBtn = createStyledButton("Search by Adhaar ID", e -> searchServiceProvider(), buttonColors[1]);
        JButton searchTypeBtn = createStyledButton("Search by Service Type", e -> searchByServiceType(), buttonColors[5]);
        JButton deleteBtn = createStyledButton("Delete Sahayak", e -> deleteServiceProvider(), buttonColors[2]);
        JButton viewAllBtn = createStyledButton("View All Sahayaks", e -> viewServiceProviders(), buttonColors[3]);
        JButton exitBtn = createStyledButton("Exit Application", e -> System.exit(0), buttonColors[4]);

        JButton[] buttons = {addBtn, searchIdBtn, searchTypeBtn, deleteBtn, viewAllBtn, exitBtn};

        for (JButton button : buttons) {
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(button);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        }
    }

    private JButton createStyledButton(String text, ActionListener action, Color baseColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Button gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, baseColor, 
                    0, getHeight(), baseColor.darker()
                );
                
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Border
                g2.setColor(new Color(0, 0, 0, 50));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(350, 60));
        button.setMaximumSize(new Dimension(350, 60));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);

        // Hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(new Color(255, 255, 255, 220));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(Color.WHITE);
            }
        });

        return button;
    }

    private boolean validateServiceProvider(ServiceProvider sp) {
        if (sp.getId() <= 0) {
            showErrorDialog("Invalid ID", "ID must be a positive number.");
            return false;
        }

        if (sp.getName() == null || sp.getName().trim().isEmpty()) {
            showErrorDialog("Invalid Name", "Name cannot be empty.");
            return false;
        }

        if (sp.getServiceType() == null || sp.getServiceType().trim().isEmpty()) {
            showErrorDialog("Invalid Service Type", "Service type cannot be empty.");
            return false;
        }

        if (sp.getHourlyRate() < 0 || sp.getHourlyRate() > 10000) {
            showErrorDialog("Invalid Rate", "Hourly rate must be between ₹0 and ₹10,000.");
            return false;
        }

        String contactStr = String.valueOf(sp.getContactNo());
        if (contactStr.length() != 10 || !contactStr.matches("^[6-9]\\d{9}$")) {
            showErrorDialog("Invalid Contact", "Contact number must be 10 digits starting with 6-9.");
            return false;
        }

        if (!isValidEmail(sp.getEmail())) {
            showErrorDialog("Invalid Email", "Please enter a valid email address.");
            return false;
        }

        if (sp.getAddress() == null || sp.getAddress().trim().isEmpty()) {
            showErrorDialog("Invalid Address", "Address cannot be empty.");
            return false;
        }

        return true;
    }

    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(emailRegex, email);
    }

    private void addServiceProvider() {
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField idField = createFormattedTextField();
        JTextField nameField = createFormattedTextField();
        JTextField serviceTypeField = createFormattedTextField();
        JTextField hourlyRateField = createFormattedTextField();
        JTextField contactField = createFormattedTextField();
        JTextField emailField = createFormattedTextField();
        JTextField addressField = createFormattedTextField();
        JTextField skillsField = createFormattedTextField();

        formPanel.add(createFormLabel("Adhaar ID:"));
        formPanel.add(idField);
        formPanel.add(createFormLabel("Full Name:"));
        formPanel.add(nameField);
        formPanel.add(createFormLabel("Service Type:"));
        formPanel.add(serviceTypeField);
        formPanel.add(createFormLabel("Hourly Rate (₹):"));
        formPanel.add(hourlyRateField);
        formPanel.add(createFormLabel("Contact Number:"));
        formPanel.add(contactField);
        formPanel.add(createFormLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(createFormLabel("Address:"));
        formPanel.add(addressField);
        formPanel.add(createFormLabel("Skills:"));
        formPanel.add(skillsField);

        int result = JOptionPane.showConfirmDialog(
            this, 
            formPanel, 
            "Add New Service Provider", 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                ServiceProvider newSP = new ServiceProvider(
                    Integer.parseInt(idField.getText()),
                    nameField.getText(),
                    serviceTypeField.getText(),
                    Float.parseFloat(hourlyRateField.getText()),
                    Long.parseLong(contactField.getText()),
                    emailField.getText(),
                    addressField.getText(),
                    skillsField.getText()
                );

                if (validateServiceProvider(newSP)) {
                    try {
                        manager.addServiceProvider(newSP);
                        JOptionPane.showMessageDialog(this, 
                            "Service provider added successfully!", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } catch (SQLException ex) {
                        if (ex.getErrorCode() == 1062) { // Duplicate entry
                            showErrorDialog("Error", "This Adhaar ID already exists in the system.");
                        } else {
                            showErrorDialog("Database Error", "Failed to add service provider: " + ex.getMessage());
                        }
                    }
                }
            } catch (NumberFormatException ex) {
                showErrorDialog("Input Error", "Please check your numeric fields (ID, Rate, Contact).");
            }
        }
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return label;
    }

    private JTextField createFormattedTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private void searchServiceProvider() {
        String idStr = JOptionPane.showInputDialog(this, 
            "Enter Adhaar ID to search:", 
            "Search Provider", 
            JOptionPane.QUESTION_MESSAGE);
            
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                try {
                    ServiceProvider sp = manager.searchServiceProvider(id);
                    if (sp != null) {
                        showProviderDetails(sp);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "No service provider found with ID: " + id, 
                            "Not Found", 
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException ex) {
                    showErrorDialog("Database Error", "Failed to search: " + ex.getMessage());
                }
            } catch (NumberFormatException ex) {
                showErrorDialog("Invalid Input", "Please enter a valid numeric ID.");
            }
        }
    }

    private void showProviderDetails(ServiceProvider sp) {
        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header with icon and name
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(sp.getName(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setForeground(new Color(33, 47, 61));
        headerPanel.add(nameLabel, BorderLayout.CENTER);
        
        detailsPanel.add(headerPanel, BorderLayout.NORTH);

        // Details in a formatted panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 220)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        infoPanel.setBackground(new Color(240, 245, 250));

        addDetailRow(infoPanel, "Adhaar ID:", String.valueOf(sp.getId()));
        addDetailRow(infoPanel, "Service Type:", sp.getServiceType());
        addDetailRow(infoPanel, "Hourly Rate:", String.format("₹%.2f", sp.getHourlyRate()));
        addDetailRow(infoPanel, "Contact:", String.valueOf(sp.getContactNo()));
        addDetailRow(infoPanel, "Email:", sp.getEmail());
        addDetailRow(infoPanel, "Address:", sp.getAddress());
        addDetailRow(infoPanel, "Skills:", sp.getSkills());

        detailsPanel.add(new JScrollPane(infoPanel), BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, detailsPanel, 
            "Service Provider Details", JOptionPane.PLAIN_MESSAGE);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setOpaque(false);
        rowPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 14));
        labelComp.setForeground(new Color(70, 70, 70));
        labelComp.setPreferredSize(new Dimension(120, 20));

        JTextArea valueComp = new JTextArea(value);
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valueComp.setEditable(false);
        valueComp.setOpaque(false);
        valueComp.setLineWrap(true);
        valueComp.setWrapStyleWord(true);

        rowPanel.add(labelComp, BorderLayout.WEST);
        rowPanel.add(valueComp, BorderLayout.CENTER);
        panel.add(rowPanel);
    }

    private void searchByServiceType() {
        String serviceType = JOptionPane.showInputDialog(this, 
            "Enter service type to search (e.g., Plumber, Electrician):", 
            "Search by Service Type", 
            JOptionPane.QUESTION_MESSAGE);
            
        if (serviceType != null && !serviceType.trim().isEmpty()) {
            try {
                List<ServiceProvider> results = manager.searchByServiceType(serviceType);
                if (!results.isEmpty()) {
                    showSearchResults(results, serviceType);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "No service providers found for: " + serviceType, 
                        "Search Results", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException ex) {
                showErrorDialog("Database Error", "Failed to search: " + ex.getMessage());
            }
        }
    }

    private void showSearchResults(List<ServiceProvider> results, String searchTerm) {
        JDialog resultsDialog = new JDialog(this, "Search Results for: " + searchTerm, true);
        resultsDialog.setSize(1200, 600);
        resultsDialog.setLocationRelativeTo(this);

        // Create table model
        String[] columnNames = {"ID", "Name", "Service Type", "Hourly Rate", "Contact", "Email", "Location"};
        Object[][] data = new Object[results.size()][columnNames.length];

        for (int i = 0; i < results.size(); i++) {
            ServiceProvider sp = results.get(i);
            data[i] = new Object[]{
                sp.getId(),
                sp.getName(),
                sp.getServiceType(),
                String.format("₹%.2f", sp.getHourlyRate()),
                sp.getContactNo(),
                sp.getEmail(),
                sp.getAddress().length() > 30 ? sp.getAddress().substring(0, 30) + "..." : sp.getAddress()
            };
        }

        JTable resultsTable = new JTable(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Customize table appearance
        resultsTable.setRowHeight(30);
        resultsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        resultsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        resultsTable.setAutoCreateRowSorter(true);

        // Custom renderer for alternate row colors
        resultsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                                                         boolean isSelected, boolean hasFocus, 
                                                         int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(240, 245, 250) : Color.WHITE);
                }
                
                return c;
            }
        });

        // Add details button column
        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = resultsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int providerId = (int) resultsTable.getValueAt(selectedRow, 0);
                    try {
                        ServiceProvider sp = manager.searchServiceProvider(providerId);
                        if (sp != null) {
                            showProviderDetails(sp);
                        }
                    } catch (SQLException ex) {
                        showErrorDialog("Error", "Failed to load provider details.");
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JLabel resultCountLabel = new JLabel(
            String.format("Found %d service providers for '%s'", results.size(), searchTerm),
            SwingConstants.CENTER
        );
        resultCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resultCountLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(resultCountLabel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        resultsDialog.add(contentPanel);
        resultsDialog.setVisible(true);
    }

    private void deleteServiceProvider() {
        String idStr = JOptionPane.showInputDialog(this, 
            "Enter Adhaar ID to delete:", 
            "Delete Provider", 
            JOptionPane.QUESTION_MESSAGE);
            
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                try {
                    boolean deleted = manager.deleteServiceProvider(id);
                    if (deleted) {
                        JOptionPane.showMessageDialog(this, 
                            "Service provider deleted successfully!", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "No service provider found with ID: " + id, 
                            "Not Found", 
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException ex) {
                    showErrorDialog("Database Error", "Failed to delete: " + ex.getMessage());
                }
            } catch (NumberFormatException ex) {
                showErrorDialog("Invalid Input", "Please enter a valid numeric ID.");
            }
        }
    }

    private void viewServiceProviders() {
        try {
            List<ServiceProvider> providers = manager.getAllServiceProviders();
            if (!providers.isEmpty()) {
                showSearchResults(providers, "All Service Providers");
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No service providers found in the system.", 
                    "No Data", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            showErrorDialog("Database Error", "Failed to load providers: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new Sahayak().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, 
                    "Failed to initialize application: " + e.getMessage(), 
                    "Startup Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}