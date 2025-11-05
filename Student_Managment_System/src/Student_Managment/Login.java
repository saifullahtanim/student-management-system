package Student_Managment;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;

public class Login extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginBtn, clearBtn, exitBtn;

    private static final String VALID_USER = "Tanim";
    private static final String VALID_PASS = "321";

    public Login() {
        super("Login · Student Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // -------- Window size (কমপ্যাক্ট) --------
        setSize(600, 420);                // ← ফ্রেমের প্রস্থ/উচ্চতা
        setLocationRelativeTo(null);
        setResizable(false);

        // Background
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,0, Color.WHITE, 0, getHeight(), new Color(247,249,248)));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        root.setBorder(new EmptyBorder(8, 10, 10, 10));
        setContentPane(root);

        // Fixed-width column (heading + card aligned)
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBorder(new EmptyBorder(4, 0, 0, 0));
        root.add(column, BorderLayout.NORTH);

        final int COL_W  = 460;   // ← heading + card প্রস্থ (কমালে prosto কমবে)
        final int CARD_H = 240;   // ← card height

        // ---- Heading (same width as card) ----
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setMaximumSize(new Dimension(COL_W, Integer.MAX_VALUE));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel titleLine = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        titleLine.setOpaque(false);
        JLabel green = new JLabel("Green");
        green.setFont(new Font("Segoe UI", Font.BOLD, 40));          // ← বড় করা হলো
        green.setForeground(new Color(34,153,84));
        JLabel tail  = new JLabel(" University of Bangladesh");
        tail.setFont(new Font("Segoe UI", Font.ITALIC, 30));          // ← বড় করা হলো
        tail.setForeground(new Color(0,156,170));
        titleLine.add(green); titleLine.add(tail);

        JLabel sub = new JLabel("Admin Panel", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 20));            // ← একটু বড়
        sub.setForeground(new Color(0,140,150));

        heading.add(titleLine);
        heading.add(Box.createVerticalStrut(6));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        heading.add(sub);
        heading.setBorder(new EmptyBorder(0,0,6,0));                  // heading↔card gap ছোট
        column.add(heading);

        // ---- RECTANGLE CARD (equal padding) ----
        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(46,204,113), 2, true),
                new EmptyBorder(14,14,14,14)                           // ← padding (চারদিক সমান)
        ));
        card.setPreferredSize(new Dimension(COL_W, CARD_H));           // ← card size
        card.setMaximumSize(new Dimension(COL_W, 9999));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        column.add(card);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);                             // ← row gap (টাইট)
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        JLabel loginTitle = new JLabel("Login", SwingConstants.CENTER);
        loginTitle.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        c.gridx=0; c.gridy=0; card.add(loginTitle, c);

        JPanel sepWrap = new JPanel(new BorderLayout());
        sepWrap.setOpaque(false);
        sepWrap.setBorder(new EmptyBorder(0, 60, 0, 60));              // ← separator inset
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(225,228,226));
        sepWrap.add(sep);
        c.gridy=1; card.add(sepWrap, c);

        // Username
        usernameField = inputText();
        setTextPlaceholder(usernameField, "Enter Username");
        c.gridy=2; card.add(usernameField, c);

        // Password
        passwordField = inputPassword();
        setPasswordPlaceholder(passwordField, "Enter Password");
        c.gridy=3; card.add(passwordField, c);

        // Login
loginBtn = new GreenButton("LOG IN");
loginBtn.setPreferredSize(new Dimension(0, 36));
loginBtn.addActionListener(e -> onLogin());
c.gridy = 4;
card.add(loginBtn, c);

// ⬇ Enter চাপলেই LOG IN ট্রিগার হবে
getRootPane().setDefaultButton(loginBtn);

// Clear | Exit
JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
actions.setOpaque(false);
clearBtn = outlineButton("Clear");
exitBtn  = outlineButton("Exit");
clearBtn.setPreferredSize(new Dimension(0, 34));
exitBtn.setPreferredSize(new Dimension(0, 34));
actions.add(clearBtn);
actions.add(exitBtn);
c.gridy = 5;
card.add(actions, c);

clearBtn.addActionListener(e -> {
    usernameField.setText("");
    passwordField.setText("");
    setTextPlaceholder(usernameField, "Enter Username");
    setPasswordPlaceholder(passwordField, "Enter Password");
    usernameField.requestFocusInWindow();
});
exitBtn.addActionListener(e -> System.exit(0));

// ⬇ Enter কীকে "login" অ্যাকশনে বেঁধে দিলাম
root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "login");
root.getActionMap().put("login", new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) { onLogin(); }
});


        // --- prevent auto-focus on any field ---
        SwingUtilities.invokeLater(() -> {
            java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
            getContentPane().requestFocusInWindow();
        });
    }

    // ===== Auth =====
    private void onLogin() {
        String u = usernameField.getText().trim();
        String p = new String(passwordField.getPassword()).trim();
        if (u.equals(VALID_USER) && p.equals(VALID_PASS)) {
            JOptionPane.showMessageDialog(this, "Welcome, " + u + "!");
            dispose();
            new Student(u).setVisible(true); // Student.java একই package-এ থাকতে হবে
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Username or Password!", "Failed",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // ===== UI helpers =====
    private static JTextField inputText() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setMargin(new Insets(8,12,8,12));
        tf.setBackground(new Color(250,250,250));
        tf.setBorder(new CompoundBorder(
                new LineBorder(new Color(220,227,223), 1, true),
                new EmptyBorder(0,6,0,6)
        ));
        addFocusHighlight(tf);
        return tf;
    }
    private static JPasswordField inputPassword() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setMargin(new Insets(8,12,8,12));
        pf.setBackground(new Color(250,250,250));
        pf.setBorder(new CompoundBorder(
                new LineBorder(new Color(220,227,223), 1, true),
                new EmptyBorder(0,6,0,6)
        ));
        addFocusHighlight(pf);
        return pf;
    }
    private static void addFocusHighlight(JTextComponent c) {
        c.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                c.setBorder(new CompoundBorder(
                        new LineBorder(new Color(46,204,113), 2, true),
                        new EmptyBorder(0,5,0,5)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                c.setBorder(new CompoundBorder(
                        new LineBorder(new Color(220,227,223), 1, true),
                        new EmptyBorder(0,6,0,6)
                ));
            }
        });
    }

    private static void setTextPlaceholder(JTextComponent comp, String hint) {
        if (comp.getText()==null || comp.getText().isEmpty()) {
            comp.setText(hint); comp.setForeground(new Color(140,140,140));
        }
        comp.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (hint.equals(comp.getText())) { comp.setText(""); comp.setForeground(new Color(33,33,33)); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (comp.getText().isEmpty()) { comp.setText(hint); comp.setForeground(new Color(140,140,140)); }
            }
        });
    }
    private static void setPasswordPlaceholder(JPasswordField pf, String hint) {
        final char DOT = '•';
        pf.putClientProperty("realEcho", DOT);
        Runnable showHint = () -> { pf.setEchoChar((char)0); pf.setText(hint); pf.setForeground(new Color(140,140,140)); };
        Runnable clearHint = () -> { pf.setForeground(new Color(33,33,33)); pf.setEchoChar((char) pf.getClientProperty("realEcho")); pf.setText(""); };
        showHint.run();
        pf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { if (new String(pf.getPassword()).equals(hint)) clearHint.run(); }
            @Override public void focusLost (FocusEvent e) { if (new String(pf.getPassword()).isEmpty()) showHint.run(); }
        });
    }

    private static JButton outlineButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(Color.WHITE);
        b.setBorder(new CompoundBorder(
                new LineBorder(new Color(205,205,205), 2, true),
                new EmptyBorder(8,12,8,12)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static class GreenButton extends JButton {
        GreenButton(String text) {
            super(text);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ButtonModel m=getModel();
            Color base=new Color(39,174,96);
            Color hov =new Color(30,156,86);
            Color prs =new Color(24,135,74);
            Color dis =new Color(39,174,96,120);
            g2.setColor(isEnabled() ? (m.isPressed()?prs:(m.isRollover()?hov:base)) : dis);
            g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
            g2.setColor(new Color(255,255,255, isEnabled()?255:180));
            FontMetrics fm=g2.getFontMetrics(getFont());
            int x=(getWidth()-fm.stringWidth(getText()))/2;
            int y=(getHeight()+fm.getAscent()-fm.getDescent())/2;
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info: UIManager.getInstalledLookAndFeels())
                if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; }
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
