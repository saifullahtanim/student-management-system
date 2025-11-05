package Student_Managment;

import javax.swing.*;
import java.awt.image.BufferedImage;

import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

// ====== NEW: persist imports ======
import java.nio.file.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/* ---------- Rounded Button ---------- */
class RoundedButton extends JButton {
    private final Color base, border, fg;
    private final Color hover, press;
    private int radius = 12;

    RoundedButton(String text, Color base, Color border, Color fg) {
        super(text);
        this.base = base; this.border = border; this.fg = fg;
        this.hover = base.darker(); this.press = border.darker();
        setContentAreaFilled(false);
        setFocusPainted(false);
        setForeground(fg);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(10,16,10,16));
        setFont(new Font("Segoe UI", Font.BOLD, 14));
    }
    public void setRadius(int r){ this.radius=r; }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2=(Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color use = getModel().isPressed()? press : getModel().isRollover()? hover : base;
        g2.setColor(use);
        g2.fillRoundRect(0,0,getWidth(),getHeight(),radius,radius);
        g2.setColor(border);
        g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1, radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }
}

/* ---------- Login Window ---------- */
class LoginWindow extends JFrame {
    private static final String VALID_USER = "Tanim";
    private static final String VALID_PASS = "321";

    public LoginWindow() {
        super("Login · Student Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(620, 360);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,0,Color.WHITE,0,getHeight(),new Color(247,249,248)));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        root.setBorder(new EmptyBorder(16,16,16,16));
        setContentPane(root);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        top.setOpaque(false);
        JLabel logo = new JLabel(loadIcon("/Student_Managment/gub.png", 24, 24));
        JLabel a = new JLabel("Green "); a.setFont(new Font("Segoe UI", Font.BOLD, 26)); a.setForeground(new Color(36,163,99));
        JLabel b = new JLabel("University of Bangladesh"); b.setFont(new Font("Segoe UI", Font.PLAIN, 26)); b.setForeground(new Color(0,124,137));
        top.add(logo); top.add(a); top.add(b);
        root.add(top, BorderLayout.NORTH);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.white);
        card.setBorder(new LineBorder(new Color(223,229,227),1,true));
        root.add(card, BorderLayout.CENTER);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets=new Insets(10,12,10,12);
        gc.fill=GridBagConstraints.HORIZONTAL;
        gc.weightx=1;

        JLabel heading=new JLabel("Login");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        heading.setForeground(new Color(36,163,99));
        gc.gridx=0;gc.gridy=0;gc.gridwidth=2; card.add(heading,gc);

        JTextField user = new JTextField(); user.setFont(new Font("Segoe UI",Font.PLAIN,16));
        JPasswordField pass = new JPasswordField(); pass.setFont(new Font("Segoe UI",Font.PLAIN,16));
        gc.gridwidth=1; gc.gridy=1; card.add(new JLabel("Username"),gc);
        gc.gridx=1; card.add(user,gc);
        gc.gridx=0; gc.gridy=2; card.add(new JLabel("Password"),gc);
        gc.gridx=1; card.add(pass,gc);

        RoundedButton login = new RoundedButton("LOG IN", new Color(36,163,99), new Color(11,132,73), Color.white);
        gc.gridx=0; gc.gridy=3; gc.gridwidth=2; gc.anchor=GridBagConstraints.CENTER;
        card.add(login,gc);

        login.addActionListener(e -> {
            if (VALID_USER.equals(user.getText().trim()) && VALID_PASS.equals(new String(pass.getPassword()))) {
                dispose();
                SwingUtilities.invokeLater(() -> new Student(user.getText().trim()).setVisible(true));
            } else {
                JOptionPane.showMessageDialog(this,"Invalid credentials","Login",JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    static ImageIcon loadIcon(String path, int w, int h){
        try {
            URL u = LoginWindow.class.getResource(path);
            if (u == null) return new ImageIcon(new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB));
            Image img = new ImageIcon(u).getImage().getScaledInstance(w,h,Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch(Exception ex){
            return new ImageIcon(new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB));
        }
    }
}

/* ==================== MAIN SCREEN ==================== */
public class Student extends JFrame {

    // ====== NEW: persistent file path ======
    private static final Path DATA_DIR  = Paths.get(System.getProperty("user.home"), "Student_Managment");
    private static final Path DATA_FILE = DATA_DIR.resolve("students.csv");

    // Theme
    private static final Color BRAND = new Color(36,163,99);
    private static final Color BRAND_DARK = new Color(11,132,73);
    private static final Font  H1 = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font  H2 = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font  TXT = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Color INPUT_BG = new Color(248,249,250);

    // UI
    private JTextField fnTf, lnTf, phoneTf, searchTf;
    private JFormattedTextField gpaTf;
    private JRadioButton maleRb, femaleRb, otherRb;
    private RoundedButton addBtn, updateBtn, deleteBtn, clearBtn, refreshBtn, logoutBtn, devBtn, pdfBtn;
    private JLabel clockLabel, accountLabel;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<TableModel> sorter;
    private JComboBox<String> genderFilter, sortCombo;

    private int hoverRow = -1;

    public Student(){ this("Admin"); }

    public Student(String username) {
        super("Student · Student Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 680);
        setLocationRelativeTo(null);
        setResizable(false);

        // ====== save on window close ======
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { saveDataToDisk(); }
        });

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,0,new Color(253,254,254),
                        0,getHeight(), new Color(240,245,244)));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        root.setBorder(new EmptyBorder(14,18,16,18));
        setContentPane(root);

        /* ---------- Top bar ---------- */
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel titleWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleWrap.setOpaque(false);
        JLabel logo = new JLabel(LoginWindow.loadIcon("/Student_Managment/gub.png", 28, 28));
        JLabel t1 = new JLabel("Green "); t1.setFont(H1); t1.setForeground(BRAND);
        JLabel t2 = new JLabel("University of Bangladesh  ·  Student Management");
        t2.setFont(H1.deriveFont(Font.PLAIN)); t2.setForeground(new Color(0,124,137));
        titleWrap.add(logo); titleWrap.add(t1); titleWrap.add(t2);
        top.add(titleWrap, BorderLayout.WEST);

        JPanel logoutBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        logoutBox.setOpaque(true);
        logoutBox.setBackground(Color.white);
        logoutBox.setBorder(new LineBorder(new Color(222,228,226),1,true));
        accountLabel = new JLabel("Account: " + username);
        accountLabel.setFont(TXT);
        logoutBtn = new RoundedButton("Logout", new Color(248,250,249),
                new Color(210,216,214), new Color(60,60,60));
        logoutBtn.setRadius(10);
        logoutBtn.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,"Logout and return to Login?","Logout",
                    JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
            if (ok==JOptionPane.YES_OPTION) {
                saveDataToDisk();           // save before leaving
                dispose();
                new LoginWindow().setVisible(true);  // back to login window
            }
        });
        logoutBox.add(accountLabel);
        logoutBox.add(logoutBtn);
        top.add(logoutBox, BorderLayout.EAST);

        root.add(top, BorderLayout.NORTH);

        /* ---------- Middle: left form + right table ---------- */
        JPanel middle = new JPanel(new GridBagLayout());
        middle.setOpaque(false);
        root.add(middle, BorderLayout.CENTER);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10,12,10,12);
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;

        // Left Form
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.white);
        formCard.setBorder(new LineBorder(new Color(230,235,233),1,true));
        formCard.setPreferredSize(new Dimension(420, 470));
        GridBagConstraints gcf = new GridBagConstraints();
        gcf.insets = new Insets(8,12,2,12);
        gcf.fill = GridBagConstraints.HORIZONTAL;
        gcf.weightx = 1;

        JLabel formTitle = new JLabel("Student Registration");
        formTitle.setFont(H2);
        formTitle.setForeground(BRAND_DARK);
        gcf.gridx=0; gcf.gridy=0; gcf.gridwidth=2; formCard.add(formTitle,gcf);

        fnTf = tf(); lnTf = tf(); phoneTf = tf();

        // Phone: only digits & max 11
        phoneTf.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume();
                }
                if (phoneTf.getText().length() >= 11 && Character.isDigit(c)) {
                    e.consume();
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });

        gpaTf = new JFormattedTextField(new javax.swing.text.NumberFormatter(new DecimalFormat("#0.00")));
        gpaTf.setFont(TXT); gpaTf.setBorder(new LineBorder(new Color(210,216,214),1,true)); gpaTf.setBackground(INPUT_BG);

        addFieldStacked(formCard, gcf, "First name", fnTf, 1);
        addFieldStacked(formCard, gcf, "Last name",  lnTf, 3);
        addFieldStacked(formCard, gcf, "Phone Number", phoneTf, 5);
        addFieldStacked(formCard, gcf, "GPA (0.00–4.00)", gpaTf, 7);

        JLabel gl = new JLabel("Gender"); gl.setFont(TXT);
        maleRb = rb("Male"); femaleRb = rb("Female"); otherRb = rb("Other");
        ButtonGroup gGroup = new ButtonGroup(); gGroup.add(maleRb); gGroup.add(femaleRb); gGroup.add(otherRb);
        JPanel gp = new JPanel(); gp.setLayout(new BoxLayout(gp, BoxLayout.Y_AXIS));
        gp.setOpaque(false); gp.add(maleRb); gp.add(femaleRb); gp.add(otherRb);
        gcf.gridx=0; gcf.gridy=9; gcf.gridwidth=2; formCard.add(gl,gcf);
        gcf.gridy=10; formCard.add(gp,gcf);

        addBtn    = new RoundedButton("Add",    new Color(36,163,99),  new Color(11,132,73), Color.white);
        updateBtn = new RoundedButton("Update", new Color(71,142,254), new Color(58,120,218), Color.white);
        deleteBtn = new RoundedButton("Delete", new Color(255,92,92),  new Color(220,70,70), Color.white);
        clearBtn  = new RoundedButton("Clear",  new Color(230,234,233),new Color(210,216,214), new Color(45,52,54));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT,10,6)); buttons.setOpaque(false);
        buttons.add(addBtn); buttons.add(updateBtn); buttons.add(deleteBtn); buttons.add(clearBtn);
        gcf.gridx=0; gcf.gridy=11; gcf.gridwidth=2; formCard.add(buttons,gcf);

        gc.gridx=0; gc.gridy=0; gc.weightx=0.40; gc.weighty=1;
        middle.add(formCard, gc);

        // Right Table
        JPanel tableArea = new JPanel(new BorderLayout(10,10));
        tableArea.setOpaque(false);

        JPanel searchRow = new JPanel(new GridBagLayout());
        searchRow.setOpaque(false);
        GridBagConstraints sgc = new GridBagConstraints();
        sgc.insets = new Insets(0,0,0,8);
        sgc.fill = GridBagConstraints.HORIZONTAL;
        sgc.weightx=1;

        JLabel sl = new JLabel("Search"); sl.setFont(TXT);
        searchTf = tf();
        sgc.gridx=0; sgc.gridy=0; sgc.weightx=0; searchRow.add(sl, sgc);
        sgc.gridx=1; sgc.weightx=1; searchRow.add(searchTf, sgc);

        genderFilter = new JComboBox<>(new String[]{"All genders","Male","Female","Other"});
        genderFilter.setFont(TXT);
        sortCombo = new JComboBox<>(new String[]{"Sort: None", "Name A–Z", "Name Z–A", "GPA ↑", "GPA ↓"});
        sortCombo.setFont(TXT);
        refreshBtn = new RoundedButton("Refresh", new Color(245,249,248), new Color(210,216,214), new Color(60,60,60));

        sgc.gridx=2; sgc.weightx=0; searchRow.add(genderFilter, sgc);
        sgc.gridx=3; searchRow.add(sortCombo, sgc);
        sgc.gridx=4; searchRow.add(refreshBtn, sgc);

        model = new DefaultTableModel(new String[]{"First name","Last name","Phone num.","GPA","Gender"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model) {
            @Override public TableCellRenderer getCellRenderer(int row, int column) {
                return new HoverRenderer(super.getCellRenderer(row,column));
            }
        };
        table.setFont(TXT);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(36,163,99)); // green
        table.setSelectionForeground(Color.WHITE);          // white text

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(3).setCellRenderer(center);

        sorter = new TableRowSorter<>(table.getModel());
        sorter.setComparator(3, (a,b) -> {
            double da = Double.parseDouble(a.toString());
            double db = Double.parseDouble(b.toString());
            return Double.compare(da, db);
        });
        table.setRowSorter(sorter);

        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                if (r != hoverRow) { hoverRow = r; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoverRow = -1; table.repaint(); }
            @Override public void mouseClicked(MouseEvent e) { loadSelected(); }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(new Color(230,235,233),1,true));
        tableArea.add(searchRow, BorderLayout.NORTH);
        tableArea.add(sp, BorderLayout.CENTER);

        gc.gridx=1; gc.gridy=0; gc.weightx=0.60; gc.fill=GridBagConstraints.BOTH;
        middle.add(tableArea, gc);

        /* ---------- Bottom ---------- */
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        clockLabel = new JLabel();
        clockLabel.setFont(TXT);
        clockLabel.setBorder(new EmptyBorder(8,10,8,10));
        bottom.add(clockLabel, BorderLayout.WEST);

        JPanel rightWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        rightWrap.setOpaque(false);

        pdfBtn = new RoundedButton("Download PDF", new Color(245,249,248), new Color(210,216,214), new Color(60,60,60));
        pdfBtn.addActionListener(e -> exportPdf());

        devBtn = new RoundedButton("Developer", new Color(245,249,248), new Color(210,216,214), new Color(60,60,60));
        devBtn.addActionListener(e -> showDeveloperDialog());

        rightWrap.add(pdfBtn);
        rightWrap.add(devBtn);
        bottom.add(rightWrap, BorderLayout.EAST);

        root.add(bottom, BorderLayout.SOUTH);
        startClock();

        /* ---------- Actions ---------- */
        addBtn.addActionListener(e -> { onAdd(); saveDataToDisk(); });
        updateBtn.addActionListener(e -> { onUpdate(); saveDataToDisk(); });
        deleteBtn.addActionListener(e -> { onDelete(); saveDataToDisk(); });
        clearBtn.addActionListener(e -> clearForm());
        refreshBtn.addActionListener(e -> {
            searchTf.setText(""); genderFilter.setSelectedIndex(0); sortCombo.setSelectedIndex(0); refreshFilter();
        });
        searchTf.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e){ refreshFilter(); }
            public void removeUpdate(DocumentEvent e){ refreshFilter(); }
            public void changedUpdate(DocumentEvent e){ refreshFilter(); }
        });
        genderFilter.addActionListener(e -> refreshFilter());
        sortCombo.addActionListener(e -> applySort());

        getRootPane().setDefaultButton(addBtn);
        getRootPane().registerKeyboardAction(e -> clearForm(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> onDelete(),
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // ====== load saved data at startup ======
        loadDataFromDisk();
    }

    /* -------- Developer Dialog (compact) -------- */
    private void showDeveloperDialog() {
        JDialog d = new JDialog(this, "Developer", true);
        d.setSize(740, 430);
        d.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(10,14,10,14));
        root.setBackground(new Color(243,246,248));
        d.setContentPane(root);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel photo = new JLabel(LoginWindow.loadIcon("/Student_Managment/Tanim.png", 240, 240)){
            @Override public void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                Shape clip = new Ellipse2D.Double(0,0,getWidth(),getHeight());
                g2.setClip(clip);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        photo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel name = new JLabel("Safulla Tanim");
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        name.setFont(new Font("Segoe UI", Font.BOLD, 40));
        name.setBorder(new EmptyBorder(8,0,6,0));

        JPanel socials = new JPanel(new FlowLayout(FlowLayout.CENTER,12,0));
        socials.setOpaque(false);
        JButton fb  = new JButton("Facebook");
        JButton git = new JButton("GitHub");
        JButton ln  = new JButton("LinkedIn");
        fb.addActionListener(e -> openLink("https://www.facebook.com/iam.saifullatanim02/"));
        git.addActionListener(e -> openLink("https://github.com/saifullahtanim"));
        ln.addActionListener(e -> openLink("https://www.linkedin.com/in/saifullatanim/"));
        socials.add(fb); socials.add(git); socials.add(ln);

        left.add(photo);
        left.add(name);
        left.add(socials);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Full Stack Developer (MERN)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(10,14,10,14));
        JPanel titleP = new JPanel(new BorderLayout());
        titleP.setBackground(new Color(220,239,224));
        titleP.add(title, BorderLayout.CENTER);
        titleP.setBorder(new EmptyBorder(2,2,8,2));

        JTextArea about = new JTextArea(
                "Creating seamless digital experiences with MERN. I build clean, reliable and user-focused web apps, " +
                        "solve real problems, and love learning & sharing. Comfortable with ReactJS, JavaScript, PHP & Python.");
        about.setEditable(false);
        about.setLineWrap(true); about.setWrapStyleWord(true);
        about.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        about.setBorder(new EmptyBorder(12,14,12,14));
        JPanel aboutWrap = new JPanel(new BorderLayout());
        aboutWrap.setBackground(new Color(245,248,246));
        aboutWrap.add(about, BorderLayout.CENTER);

        right.add(titleP);
        right.add(aboutWrap);

        JPanel center = new JPanel(new GridLayout(1,2,16,0));
        center.setOpaque(false);
        center.add(left);
        center.add(right);
        root.add(center, BorderLayout.CENTER);

        JLabel footerL = new JLabel("Generated: " + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  •  hh:mm a")));
        footerL.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JLabel footerR = new JLabel("Developer: Safulla Tanim");
        footerR.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.add(footerL, BorderLayout.WEST);
        foot.add(footerR, BorderLayout.EAST);
        foot.setBorder(new EmptyBorder(6,2,2,2));
        root.add(foot, BorderLayout.SOUTH);

        d.setResizable(false);
        d.setVisible(true);
    }
    private void openLink(String url) {
        try { Desktop.getDesktop().browse(new URI(url)); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "Could not open link."); }
    }

    /* ---------- Clock ---------- */
    private void startClock() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  •  hh:mm:ss a");
        new javax.swing.Timer(1000, e -> {
            clockLabel.setText(" " + LocalDateTime.now().format(fmt));
        }).start();
    }

    /* ---------- Helpers ---------- */
    private JTextField tf(){
        JTextField t = new JTextField(18);
        t.setFont(TXT);
        t.setBorder(new LineBorder(new Color(210,216,214),1,true));
        t.setBackground(INPUT_BG);
        return t;
    }
    private JRadioButton rb(String s){
        JRadioButton r=new JRadioButton(s); r.setOpaque(false); r.setFont(TXT); return r;
    }
    private void addFieldStacked(JPanel p, GridBagConstraints g, String label, JComponent field, int startRow){
        JLabel l=new JLabel(label); l.setFont(TXT);
        g.gridx=0; g.gridy=startRow; g.gridwidth=2; p.add(l,g);
        g.gridy=startRow+1; p.add(field,g);
    }

    private String gender(){
        if(maleRb.isSelected()) return "Male";
        if(femaleRb.isSelected()) return "Female";
        if(otherRb.isSelected()) return "Other";
        return "";
    }
    private boolean validateForm(){
        String fn=fnTf.getText().trim(), ln=lnTf.getText().trim(), ph=phoneTf.getText().trim(), gp=gpaTf.getText().trim();
        if(fn.isEmpty()||ln.isEmpty()){ msg("First & Last name required."); return false; }
        if(!ph.matches("^01\\d{9}$")){ msg("Phone must be exactly 11 digits and start with 01."); return false; }

        try{
            double g=Double.parseDouble(gp);
            if(g<0||g>4.0) throw new NumberFormatException();
            gpaTf.setText(new DecimalFormat("#0.00").format(g));
        }catch(Exception ex){ msg("GPA must be between 0.00 and 4.00"); return false; }
        if(gender().isEmpty()){ msg("Please select Gender."); return false; }
        return true;
    }
    private void msg(String s){ JOptionPane.showMessageDialog(this,s,"Validation",JOptionPane.WARNING_MESSAGE); }

    private void onAdd(){
        if(!validateForm()) return;
        Vector<String> row=new Vector<>();
        row.add(fnTf.getText().trim());
        row.add(lnTf.getText().trim());
        row.add(phoneTf.getText().trim());
        row.add(gpaTf.getText().trim());
        row.add(gender());
        model.addRow(row);
        clockLabel.setText(clockLabel.getText() + "   •   ✔ Student added");
        clearForm();
    }
    private void onUpdate(){
        int r=table.getSelectedRow(); if(r<0){ JOptionPane.showMessageDialog(this,"Select a row first."); return; }
        if(!validateForm()) return;
        int m=table.convertRowIndexToModel(r);
        model.setValueAt(fnTf.getText().trim(),m,0);
        model.setValueAt(lnTf.getText().trim(),m,1);
        model.setValueAt(phoneTf.getText().trim(),m,2);
        model.setValueAt(gpaTf.getText().trim(),m,3);
        model.setValueAt(gender(),m,4);
        clockLabel.setText(clockLabel.getText() + "   •   ✎ Updated");
    }
    private void onDelete(){
        int r=table.getSelectedRow(); if(r<0){ JOptionPane.showMessageDialog(this,"Select a row to delete."); return; }
        int ok=JOptionPane.showConfirmDialog(this,"Delete selected record?","Confirm Delete",
                JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(ok==JOptionPane.YES_OPTION){
            int m=table.convertRowIndexToModel(r);
            model.removeRow(m);
            clockLabel.setText(clockLabel.getText() + "   •   🗑️ Deleted");
            clearForm();
        }
    }
    private void clearForm(){
        fnTf.setText(""); lnTf.setText(""); phoneTf.setText(""); gpaTf.setText("");
        maleRb.setSelected(false); femaleRb.setSelected(false); otherRb.setSelected(false);
        table.clearSelection(); fnTf.requestFocus();
    }
    private void loadSelected(){
        int r=table.getSelectedRow(); if(r<0) return;
        int m=table.convertRowIndexToModel(r);
        fnTf.setText((String)model.getValueAt(m,0));
        lnTf.setText((String)model.getValueAt(m,1));
        phoneTf.setText((String)model.getValueAt(m,2));
        gpaTf.setText((String)model.getValueAt(m,3));
        String g=(String)model.getValueAt(m,4);
        maleRb.setSelected("Male".equalsIgnoreCase(g));
        femaleRb.setSelected("Female".equalsIgnoreCase(g));
        otherRb.setSelected("Other".equalsIgnoreCase(g));
    }

    private void refreshFilter(){
        String q=searchTf.getText().trim();
        RowFilter<TableModel,Object> rf = new RowFilter<>() {
            @Override public boolean include(Entry<? extends TableModel, ? extends Object> e) {
                boolean textOk = q.isEmpty();
                if(!textOk){
                    for(int i=0;i<e.getValueCount();i++){
                        if(String.valueOf(e.getValue(i)).toLowerCase().contains(q.toLowerCase())) { textOk=true; break; }
                    }
                }
                String gSel = (String) genderFilter.getSelectedItem();
                boolean genderOk = "All genders".equals(gSel) || String.valueOf(e.getValue(4)).equalsIgnoreCase(gSel);
                return textOk && genderOk;
            }
        };
        sorter.setRowFilter(rf);
    }
    private void applySort(){
        String sel = (String) sortCombo.getSelectedItem();
        java.util.List<RowSorter.SortKey> keys = new java.util.ArrayList<>();
        if ("Name A–Z".equals(sel)) keys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        else if ("Name Z–A".equals(sel)) keys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
        else if ("GPA ↑".equals(sel))   keys.add(new RowSorter.SortKey(3, SortOrder.ASCENDING));
        else if ("GPA ↓".equals(sel))   keys.add(new RowSorter.SortKey(3, SortOrder.DESCENDING));
        sorter.setSortKeys(keys);
    }

    private class HoverRenderer extends DefaultTableCellRenderer {
        private final TableCellRenderer delegate;
        HoverRenderer(TableCellRenderer delegate){ this.delegate=delegate; }
        @Override public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean foc, int r, int c) {
            Component comp = ((DefaultTableCellRenderer)delegate)
                    .getTableCellRendererComponent(tbl, val, sel, foc, r, c);
            if (!sel) {
                comp.setForeground(Color.BLACK);
                comp.setBackground((r%2==0)? new Color(251,253,252): Color.white);
                if (r==hoverRow) comp.setBackground(new Color(243,246,245));
            } else {
                comp.setForeground(Color.WHITE);
                comp.setBackground(tbl.getSelectionBackground()); // green
            }
            return comp;
        }
    }

    /* ---------- PDF Export (no external lib) ---------- */
    private void exportPdf(){
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("Student List");
            job.setPrintable(new Printable() {
                @Override
                public int print(Graphics graphics, PageFormat pf, int pageIndex) throws PrinterException {
                    if (pageIndex > 0) return NO_SUCH_PAGE;
                    Graphics2D g2 = (Graphics2D) graphics;
                    g2.translate(pf.getImageableX(), pf.getImageableY());

                    int x = 40, yTop = 40;
                    ImageIcon ic = LoginWindow.loadIcon("/Student_Managment/gub.png", 28, 28);
                    g2.drawImage(ic.getImage(), x, yTop-24, null);

                    g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                    g2.drawString("Green University of Bangladesh", x+36, yTop-6);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    g2.drawString("Student List", x+36, yTop+14);

                    int y = yTop + 30;
                    int rowH = 20;
                    int[] w = {120,120,160,60,80};
                    String[] head = {"First name","Last name","Phone num.","GPA","Gender"};

                    g2.setColor(new Color(230,235,233));
                    g2.fillRect(x, y, sum(w), rowH);
                    g2.setColor(Color.BLACK);
                    g2.drawRect(x, y, sum(w), rowH);

                    int cx = x + 6;
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    for (int i=0;i<head.length;i++){
                        g2.drawString(head[i], cx, y+14);
                        cx += w[i];
                    }

                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    y += rowH;
                    for (int r=0; r<model.getRowCount(); r++){
                        if (r%2==0) g2.setColor(new Color(251,253,252));
                        else g2.setColor(Color.WHITE);
                        g2.fillRect(x, y, sum(w), rowH);
                        g2.setColor(Color.BLACK);
                        g2.drawRect(x, y, sum(w), rowH);

                        int tx = x + 6;
                        g2.drawString(String.valueOf(model.getValueAt(r,0)), tx, y+14); tx+=w[0];
                        g2.drawString(String.valueOf(model.getValueAt(r,1)), tx, y+14); tx+=w[1];
                        g2.drawString(String.valueOf(model.getValueAt(r,2)), tx, y+14); tx+=w[2];
                        g2.drawString(String.valueOf(model.getValueAt(r,3)), tx, y+14); tx+=w[3];
                        g2.drawString(String.valueOf(model.getValueAt(r,4)), tx, y+14);

                        y += rowH;
                        if (y > pf.getImageableHeight()-80) break; // single page
                    }

                    String left  = "Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  •  hh:mm a"));
                    String right = "Developer: Safulla Tanim";
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    g2.drawString(left, x, (int)(pf.getImageableHeight()-20));
                    int textW = g2.getFontMetrics().stringWidth(right);
                    g2.drawString(right, (int)(pf.getImageableWidth()-x - textW), (int)(pf.getImageableHeight()-20));

                    return PAGE_EXISTS;
                }
            });

            // User picks "Microsoft Print to PDF" (or any PDF printer) and location
            if (job.printDialog()) {
                job.print();
            }
        } catch (Exception ex){
            try {
                table.print(JTable.PrintMode.FIT_WIDTH,
                        new MessageFormat("Student List"),
                        null, true, null, true, null);
            } catch (Exception ignore) { }
        }
    }
    private int sum(int[] a){ int s=0; for(int v:a) s+=v; return s; }

    // ====== NEW: persistence ======
    private void loadDataFromDisk() {
        try {
            if (!Files.exists(DATA_FILE)) {
                Files.createDirectories(DATA_DIR);
                Files.createFile(DATA_FILE);
                return;
            }
            java.util.List<String> lines = Files.readAllLines(DATA_FILE, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length < 5) continue;
                Vector<String> row = new Vector<>();
                for (int i=0;i<5;i++) row.add(parts[i]);
                model.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Load failed: "+ex.getMessage());
        }
    }

    private void saveDataToDisk() {
        try {
            Files.createDirectories(DATA_DIR);
            try (BufferedWriter bw = Files.newBufferedWriter(DATA_FILE, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                int rows = model.getRowCount();
                for (int r=0;r<rows;r++){
                    // simple csv (কমা থাকলে স্পেসে পাল্টে নিচ্ছি)
                    String fn = String.valueOf(model.getValueAt(r,0)).replace(',', ' ');
                    String ln = String.valueOf(model.getValueAt(r,1)).replace(',', ' ');
                    String ph = String.valueOf(model.getValueAt(r,2)).replace(',', ' ');
                    String gp = String.valueOf(model.getValueAt(r,3)).replace(',', ' ');
                    String ge = String.valueOf(model.getValueAt(r,4)).replace(',', ' ');
                    bw.write(fn + "," + ln + "," + ph + "," + gp + "," + ge);
                    bw.newLine();
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: "+ex.getMessage());
        }
    }

    /* Entry */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginWindow().setVisible(true));
    }
}
