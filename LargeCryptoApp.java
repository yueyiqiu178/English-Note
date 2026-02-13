import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.security.spec.KeySpec;
import java.util.Base64;

public class LargeCryptoApp extends JFrame {

    private JPasswordField passwordField;
    private JTextArea inputArea;
    private JTextArea outputArea;
    private static final byte[] SALT = "FixedSalt123".getBytes();

    public LargeCryptoApp() {
        // 設定全局字型 (放大至 20 號字)
        setUIFont(new Font("Microsoft JhengHei", Font.PLAIN, 20));

        setTitle("AES 專業加解密工具 - 大視窗版");

        // --- 設定視窗啟動大小 ---
        setSize(900, 950);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 主容器：增加內邊距 (Padding)
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(35, 35, 35, 35));
        mainPanel.setBackground(new Color(242, 244, 247));
        setContentPane(mainPanel);

        // --- 1. 密碼區 (加高) ---
        JPanel topPanel = new JPanel(new BorderLayout(15, 15));
        topPanel.setOpaque(false);
        JLabel pwdLabel = new JLabel("設定存取密碼 (Password):");
        pwdLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(0, 55)); // 加高密碼框
        topPanel.add(pwdLabel, BorderLayout.NORTH);
        topPanel.add(passwordField, BorderLayout.CENTER);

        // --- 2. 文字處理區 ---
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 25, 25));
        centerPanel.setOpaque(false);

        inputArea = createStyledTextArea();
        outputArea = createStyledTextArea();
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(228, 233, 237));

        centerPanel.add(createScrollWrapper(inputArea, "輸入內容 (Input Area)"));
        centerPanel.add(createScrollWrapper(outputArea, "結果輸出 (Output Area)"));

        // --- 3. 主要功能按鈕 ---
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        actionPanel.setOpaque(false);
        actionPanel.setPreferredSize(new Dimension(0, 80)); // 加高按鈕區

        JButton encryptBtn = createStyledButton("執行加密 ENCRYPT", new Color(44, 62, 80));
        JButton decryptBtn = createStyledButton("執行解密 DECRYPT", new Color(30, 130, 76));

        actionPanel.add(encryptBtn);
        actionPanel.add(decryptBtn);

        // --- 4. 底部工具按鈕 ---
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        toolPanel.setOpaque(false);
        JButton copyBtn = new JButton(" 複製結果 ");
        JButton clearBtn = new JButton(" 清空全部 ");
        toolPanel.add(copyBtn);
        toolPanel.add(clearBtn);

        // 組合底部
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(actionPanel, BorderLayout.NORTH);
        southPanel.add(toolPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        // --- 邏輯處理 ---
        encryptBtn.addActionListener(e -> process(Cipher.ENCRYPT_MODE));
        decryptBtn.addActionListener(e -> process(Cipher.DECRYPT_MODE));

        clearBtn.addActionListener(e -> {
            inputArea.setText("");
            outputArea.setText("");
            passwordField.setText("");
        });

        copyBtn.addActionListener(e -> {
            StringSelection selection = new StringSelection(outputArea.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            JOptionPane.showMessageDialog(this, "已複製到剪貼簿！", "成功", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void setUIFont(Font font) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (UIManager.get(key) instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, new javax.swing.plaf.FontUIResource(font));
            }
        }
    }

    private JTextArea createStyledTextArea() {
        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Monospaced", Font.PLAIN, 20)); // 文字內容也同步放大
        area.setMargin(new Insets(15, 15, 15, 15));
        return area;
    }

    private JScrollPane createScrollWrapper(JTextArea area, String title) {
        JScrollPane scroll = new JScrollPane(area);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 2), title);
        border.setTitleFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        scroll.setBorder(border);
        return scroll;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(bg.brighter()); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private void process(int mode) {
        try {
            String password = new String(passwordField.getPassword());
            String content = inputArea.getText().trim();
            if (password.isEmpty() || content.isEmpty()) return;

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, 65536, 256);
            SecretKeySpec secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(mode, secretKey);

            if (mode == Cipher.ENCRYPT_MODE) {
                outputArea.setText(Base64.getEncoder().encodeToString(cipher.doFinal(content.getBytes("UTF-8"))));
            } else {
                outputArea.setText(new String(cipher.doFinal(Base64.getDecoder().decode(content)), "UTF-8"));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "操作失敗：請檢查密碼或內容格式是否正確", "提示", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new LargeCryptoApp().setVisible(true));
    }
}