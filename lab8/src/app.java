import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class app{

    private static JLabel login_message;
    private static JLabel user_label;
    private static JLabel password_label;
    private static JTextField user_entry;
    private static JPasswordField password_entry;
    private static JButton login_button;
    private static JButton password_button;
    private static String pass = null;
    private static String usr = null;


    public static void main(String[] args) throws Exception{

        JPanel panel = new JPanel();
        JFrame outer = new JFrame();
        outer.setSize(290, 220);
        outer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        outer.add(panel);

        panel.setLayout(null);

        user_label = new JLabel("Email address:");
        user_label.setBounds(10, 20, 100, 25);
        panel.add(user_label);

        user_entry = new JTextField(20);
        user_entry.setBounds(110, 20, 155, 25);
        panel.add(user_entry);

        outer.setVisible(true);

        password_label = new JLabel("Password:");
        password_label.setBounds(10, 80, 80, 25);
        panel.add(password_label);

        password_entry = new JPasswordField();
        password_entry.setBounds(110, 80, 155, 25);
        panel.add(password_entry);
        outer.setVisible(true);

        password_button = new JButton("Get password");
        password_button.setBounds(150, 50, 115, 25);
        panel.add(password_button);
        outer.setVisible(true);

        login_button = new JButton("Login");
        login_button.setBounds(195, 110, 70, 25);
        panel.add(login_button);
        outer.setVisible(true);

        FileReader reader=new FileReader("D:\\University\\Sem5\\cs\\labs\\lab\\login.properties");

        Properties p=new Properties();
        p.load(reader);

        String p_user = p.getProperty("user");
        String p_password = p.getProperty("password");

        password_button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){

                String user = user_entry.getText();
                if (!user.equals("")){

                    PasswordGenerator passwordGenerator = new PasswordGenerator.PasswordGeneratorBuilder()
                            .useDigits(true)
                            .useLower(true)
                            .useUpper(true)
                            .usePunctuation(true)
                            .build();
                    pass = passwordGenerator.generate(16);
                    System.out.println("generated password: " + pass);

                    usr = user_entry.getText();
                    System.out.println("introduced user: " + usr);

                    SendPassword.send(p_user, p_password, user,"Lab9 App", "Your password is: "+ pass);
                    login_message.setText("Message sent successfully! Check your email.");
                }
                else{
                    login_message.setText("Email field can not be empty.");
                }
            }
        });


        login_button.addActionListener(e -> {
            String user = user_entry.getText();
            String password = password_entry.getText();
            System.out.println("introduced user: " + user + ", "+"introduced password: "+ password);

            if (!user.equals("") && !password.equals("") && password.equals(pass) && user.equals(usr)) {
                login_message.setText("Login successful!");
            }
            else{
                login_message.setText("Login failed!");
            }
            System.out.println("login status: " + login_message.getText());
        });

        login_message = new JLabel("");
        login_message.setBounds(10, 140, 300, 25);
        panel.add(login_message);

        outer.setVisible(true);
    }
}