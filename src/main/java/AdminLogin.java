import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Separate class for the login frame
class AdminLogin extends JFrame {
    public AdminLogin() {
        setTitle("Admin Login Page");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create a main panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            // Override paintComponent to draw background image
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Load background image
                ImageIcon imageIcon = new ImageIcon("C:\\Users\\sarvi\\OneDrive\\Desktop\\java downloads\\Proj1admin\\Proj1\\src\\icon\\bgimg3.png"); // Change the file path to your image
                Image image = imageIcon.getImage();
                // Draw image at full size of the panel
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        };

        // Create panel for welcome label
        JPanel welcomePanel = new JPanel();
        JLabel welcomeLabel = new JLabel("Welcome Admin!!");
        // Set font for welcome label
        welcomeLabel.setFont(new Font("Times new roman", Font.BOLD, 36)); // Adjust font as needed
        // Set text color for welcome label
        welcomeLabel.setForeground(Color.WHITE);
        welcomePanel.setBackground(new Color(255, 255, 255, 0)); // Transparent background
        welcomePanel.add(welcomeLabel);

        // Create panel for username
        JPanel usernamePanel = new JPanel();
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(30); // Adjust field size as needed
        // Set preferred size for username field
        usernameField.setPreferredSize(new Dimension(300, 40)); // Adjust width as needed
        // Set font for username field
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16)); // Change font as needed
        // Set background color for username field
        usernameField.setBackground(Color.WHITE);
        // Set text color for username label
        usernameLabel.setForeground(Color.WHITE);
        usernamePanel.setBackground(new Color(255, 255, 255, 0)); // Transparent background
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);

        // Create panel for password
        JPanel passwordPanel = new JPanel();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(30); // Adjust field size as needed
        // Set preferred size for password field
        passwordField.setPreferredSize(new Dimension(300, 40)); // Adjust width as needed
        // Set font for password field
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16)); // Change font as needed
        // Set background color for password field
        passwordField.setBackground(Color.WHITE);
        // Set text color for password label
        passwordLabel.setForeground(Color.WHITE);
        passwordPanel.setBackground(new Color(255, 255, 255, 0)); // Transparent background
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);

        // Create panel for login button
        JPanel buttonPanel = new JPanel();
        JButton loginButton = new JButton("Login");
        // Set font for login button
        loginButton.setFont(new Font("Arial", Font.PLAIN, 18)); // Change font as needed
        // Set background color for login button
        loginButton.setBackground(Color.WHITE);
        // Set text color for login button
        loginButton.setForeground(Color.BLACK);
        buttonPanel.setBackground(new Color(255, 255, 255, 0)); // Transparent background
        buttonPanel.add(loginButton);

        // Add ActionListener to login button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if(username.equals("admin")&& password.equals("1234"))
                {

                    AdminFrame adminFrame = new AdminFrame();
                    adminFrame.setVisible(true);
                    dispose();

                }
                else{
                    JOptionPane.showMessageDialog(null,"Invalid Username or Password","Error",JOptionPane.ERROR_MESSAGE);
                    usernameField.setText("");
                    passwordField.setText("");

                }
            }
        });


        // Add panels to the main panel with constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 5, 5, 5); // Adjust insets, increased top padding
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(welcomePanel, gbc); // Add welcome panel

        gbc.gridy = 1;
        mainPanel.add(usernamePanel, gbc);

        gbc.gridy = 2;
        mainPanel.add(passwordPanel, gbc);

        gbc.gridy = 3;
        mainPanel.add(buttonPanel, gbc);

        // Add the main panel to the frame
        add(mainPanel);
    }
}
