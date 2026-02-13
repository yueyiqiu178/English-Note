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

public class HighCompatCryptoApp extends JFrame {

    private JPasswordField passwordField;
    private JTextArea inputArea;
    private JTextArea outputArea;
    private static final byte[] SALT = "FixedSalt123".getBytes();

    public HighCompatCryptoApp() {
        // 設定全局字型，確保中文顯示正常，移除可能導致亂碼的圖示
        setUIFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));

        setTitle("AES 專業加解密工具 - 高相容版");
        setSize(750, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(new Color(240, 242, 245));
        setContentPane(mainPanel);

        // --- 1. 密碼區 ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        JLabel pwdLabel = new JLabel("設定存取密碼 (Password):");
        pwdLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(0, 45));
        topPanel.add(pwdLabel, BorderLayout.NORTH);
        topPanel.add(passwordField, BorderLayout.CENTER);

        // --- 2. 文字處理區 ---
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 15, 15));
        centerPanel.setOpaque(false);

        inputArea = createStyledTextArea();
        outputArea = createStyledTextArea();
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(225, 230, 235));

        centerPanel.add(createScrollWrapper(inputArea, "輸入內容 (Input)"));
        centerPanel.add(createScrollWrapper(outputArea, "結果輸出 (Output)"));

        // --- 3. 按鈕功能區 ---
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        actionPanel.setOpaque(false);
        actionPanel.setPreferredSize(new Dimension(0, 65));

        // 加密按鈕 - 深石板灰
        JButton encryptBtn = createStyledButton("執行加密 ENCRYPT", new Color(44, 62, 80));
        // 解密按鈕 - 翠綠色
        JButton decryptBtn = createStyledButton("執行解密 DECRYPT", new Color(30, 130, 76));

        actionPanel.add(encryptBtn);
        actionPanel.add(decryptBtn);

        // --- 4. 輔助功能區 (清除與複製) ---
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        toolPanel.setOpaque(false);
        JButton copyBtn = new JButton("複製結果");
        JButton clearBtn = new JButton("清空全部");
        toolPanel.add(copyBtn);
        toolPanel.add(clearBtn);

        // 組合面板
        JPanel southPanel = new JPanel(new BorderLayout(10, 10));
        southPanel.setOpaque(false);
        southPanel.add(actionPanel, BorderLayout.NORTH);
        southPanel.add(toolPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        // --- 事件處理 ---
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
            JOptionPane.showMessageDialog(this, "已複製到剪貼簿");
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
        area.setFont(new Font("Monospaced", Font.PLAIN, 18)); // 內容使用等寬字型更好看
        area.setMargin(new Insets(12, 12, 12, 12));
        return area;
    }

    private JScrollPane createScrollWrapper(JTextArea area, String title) {
        JScrollPane scroll = new JScrollPane(area);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 2), title);
        border.setTitleFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
        scroll.setBorder(border);
        return scroll;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
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
            if (password.isEmpty() || content.isEmpty()) {
                JOptionPane.showMessageDialog(this, "請輸入密碼與內容");
                return;
            }

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, 65536, 256);
            SecretKeySpec secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(mode, secretKey);

            if (mode == Cipher.ENCRYPT_MODE) {
                byte[] encrypted = cipher.doFinal(content.getBytes("UTF-8"));
                outputArea.setText(Base64.getEncoder().encodeToString(encrypted));
            } else {
                byte[] decoded = Base64.getDecoder().decode(content);
                outputArea.setText(new String(cipher.doFinal(decoded), "UTF-8"));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "錯誤：密碼錯誤或格式不正確");
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new HighCompatCryptoApp().setVisible(true));
    }
}