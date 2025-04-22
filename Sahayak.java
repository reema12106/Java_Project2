import java.awt.*;
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
    private JPanel contentPanel; // Main content area
    private CardLayout cardLayout;
    private Color sidebarColor = new Color(44, 62, 80);
    private Color headerColor = new Color(52, 152, 219);
    private Color backgroundColor = new Color(245, 247, 250);

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
        setTitle("Sahayata - Service Providers Platform");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(headerColor);
        header.setPreferredSize(new Dimension(1100, 60));
        JLabel title = new JLabel("Sahayata Platform", JLabel.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setBackground(sidebarColor);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        String[] navItems = {"Dashboard", "Add Provider", "Search by ID", "Search by Type", "Delete Provider", "View All", "Exit"};
        for (String item : navItems) {
            JButton btn = new JButton(item);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(200, 45));
            btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
            btn.setForeground(Color.WHITE);
            btn.setBackground(sidebarColor);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(headerColor);
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(sidebarColor);
                }
            });
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
            // Navigation actions
            switch (item) {
                case "Dashboard":
                    btn.addActionListener(e -> cardLayout.show(contentPanel, "dashboard"));
                    break;
                case "Add Provider":
                    btn.addActionListener(e -> cardLayout.show(contentPanel, "add"));
                    break;
                case "Search by ID":
                    btn.addActionListener(e -> cardLayout.show(contentPanel, "searchId"));
                    break;
                case "Search by Type":
                    btn.addActionListener(e -> cardLayout.show(contentPanel, "searchType"));
                    break;
                case "Delete Provider":
                    btn.addActionListener(e -> cardLayout.show(contentPanel, "delete"));
                    break;
                case "View All":
                    btn.addActionListener(e -> cardLayout.show(contentPanel, "viewAll"));
                    break;
                case "Exit":
                    btn.addActionListener(e -> System.exit(0));
                    break;
            }
        }
        add(sidebar, BorderLayout.WEST);

        // Main content area with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(backgroundColor);

        // Add cards
        contentPanel.add(createDashboardPanel(), "dashboard");
        contentPanel.add(createAddProviderPanel(), "add");
        contentPanel.add(createSearchByIdPanel(), "searchId");
        contentPanel.add(createSearchByTypePanel(), "searchType");
        contentPanel.add(createDeleteProviderPanel(), "delete");
        contentPanel.add(createViewAllPanel(), "viewAll");

        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "dashboard");
    }

    // Dashboard card
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        panel.setLayout(new GridBagLayout());
        JLabel welcome = new JLabel("Welcome to Sahayata Service Providers Platform");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcome.setForeground(new Color(44, 62, 80));
        panel.add(welcome);
        return panel;
    }

    // Add Provider card
    private JPanel createAddProviderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel title = new JLabel("Add New Service Provider");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(title, gbc);
        gbc.gridy++;
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField serviceTypeField = new JTextField();
        JTextField hourlyRateField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField skillsField = new JTextField();
        formPanel.add(new JLabel("Adhaar ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Service Type:"));
        formPanel.add(serviceTypeField);
        formPanel.add(new JLabel("Hourly Rate (₹):"));
        formPanel.add(hourlyRateField);
        formPanel.add(new JLabel("Contact Number:"));
        formPanel.add(contactField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Address:"));
        formPanel.add(addressField);
        formPanel.add(new JLabel("Skills:"));
        formPanel.add(skillsField);
        panel.add(formPanel, gbc);
        gbc.gridy++;
        JButton submitBtn = new JButton("Add Provider");
        submitBtn.setBackground(headerColor);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        submitBtn.setFocusPainted(false);
        submitBtn.addActionListener(e -> {
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
                    manager.addServiceProvider(newSP);
                    JOptionPane.showMessageDialog(this, "Service provider added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    idField.setText(""); nameField.setText(""); serviceTypeField.setText(""); hourlyRateField.setText(""); contactField.setText(""); emailField.setText(""); addressField.setText(""); skillsField.setText("");
                }
            } catch (Exception ex) {
                showErrorDialog("Input Error", "Please check your fields.");
            }
        });
        panel.add(submitBtn, gbc);
        return panel;
    }

    // Search by ID card
    private JPanel createSearchByIdPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel title = new JLabel("Search Service Provider by Adhaar ID");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(title, gbc);
        gbc.gridy++;
        JTextField idField = new JTextField(15);
        panel.add(idField, gbc);
        gbc.gridy++;
        JButton searchBtn = new JButton("Search");
        searchBtn.setBackground(headerColor);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        searchBtn.setFocusPainted(false);
        panel.add(searchBtn, gbc);
        searchBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                ServiceProvider sp = manager.searchServiceProvider(id);
                if (sp != null) {
                    showProviderDetails(sp);
                } else {
                    JOptionPane.showMessageDialog(this, "No service provider found with ID: " + id, "Not Found", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                showErrorDialog("Input Error", "Please enter a valid numeric ID.");
            }
        });
        return panel;
    }

    // Search by Type card
    private JPanel createSearchByTypePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel title = new JLabel("Search Service Providers by Type");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(title, gbc);
        gbc.gridy++;
        JTextField typeField = new JTextField(15);
        panel.add(typeField, gbc);
        gbc.gridy++;
        JButton searchBtn = new JButton("Search");
        searchBtn.setBackground(headerColor);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        searchBtn.setFocusPainted(false);
        panel.add(searchBtn, gbc);
        searchBtn.addActionListener(e -> {
            try {
                List<ServiceProvider> results = manager.searchByServiceType(typeField.getText());
                if (!results.isEmpty()) {
                    showSearchResults(results, typeField.getText());
                } else {
                    JOptionPane.showMessageDialog(this, "No service providers found for: " + typeField.getText(), "Search Results", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                showErrorDialog("Database Error", "Failed to search: " + ex.getMessage());
            }
        });
        return panel;
    }

    // Delete Provider card
    private JPanel createDeleteProviderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel title = new JLabel("Delete Service Provider by Adhaar ID");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(title, gbc);
        gbc.gridy++;
        JTextField idField = new JTextField(15);
        panel.add(idField, gbc);
        gbc.gridy++;
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setBackground(new Color(231, 76, 60));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        deleteBtn.setFocusPainted(false);
        panel.add(deleteBtn, gbc);
        deleteBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                boolean deleted = manager.deleteServiceProvider(id);
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Service provider deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No service provider found with ID: " + id, "Not Found", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                showErrorDialog("Input Error", "Please enter a valid numeric ID.");
            }
        });
        return panel;
    }

    // View All Providers card
    private JPanel createViewAllPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(backgroundColor);
        JLabel title = new JLabel("All Service Providers", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(headerColor);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshBtn.setFocusPainted(false);
        panel.add(refreshBtn, BorderLayout.SOUTH);
        JTable table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        refreshBtn.addActionListener(e -> loadAllProviders(table));
        loadAllProviders(table);
        return panel;
    }

    private void loadAllProviders(JTable table) {
        try {
            List<ServiceProvider> providers = manager.getAllServiceProviders();
            String[] columnNames = {"ID", "Name", "Service Type", "Hourly Rate", "Contact", "Email", "Location"};
            Object[][] data = new Object[providers.size()][columnNames.length];
            for (int i = 0; i < providers.size(); i++) {
                ServiceProvider sp = providers.get(i);
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
            table.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
                public boolean isCellEditable(int row, int column) { return false; }
            });
            table.setRowHeight(28);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        } catch (Exception ex) {
            showErrorDialog("Database Error", "Failed to load providers: " + ex.getMessage());
        }
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