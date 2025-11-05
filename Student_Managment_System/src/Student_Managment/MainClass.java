package Student_Managment;

import javax.swing.JFrame;

public class MainClass extends JFrame {

    public static void main(String[] args) {
        Login window = new Login();
        window.setVisible(true);
        window.setBounds(100, 50, 420, 350);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setTitle("Login");
    }
}
