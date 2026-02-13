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

public class FullScreenCryptoApp extends JFrame {

    private JPasswordField passwordField;
    private JTextArea inputArea;
    private JTextArea outputArea;
    private static final byte[] SALT = "FixedSalt123".getBytes();

    public FullScreenCryptoApp() {
        // 設定全局字型 (全螢幕建議放大至 22 號字，視覺更舒適)
        setUIFont(new Font("Microsoft JhengHei", Font.PLAIN, 22));

        setTitle("AES 專業加解密工具 - 全螢幕旗艦版");

        // --- 設定啟動即最大化 ---
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 600)); // 設定最小尺寸防止縮太小
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 主容器：增加更寬大的內邊距
        JPanel mainPanel = new JPanel(new BorderLayout(30, 30));
        mainPanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        mainPanel.setBackground(new Color(236, 240, 241));
        setContentPane(mainPanel);

        // --- 1. 密碼區 ---
        JPanel topPanel = new JPanel(new BorderLayout(15, 15));
        topPanel.setOpaque(false);
        JLabel pwdLabel = new JLabel("請設定存取密碼 (Password)：");
        pwdLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 26));
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(0, 60));
        topPanel.add(pwdLabel, BorderLayout.NORTH);
        topPanel.add(passwordField, BorderLayout.CENTER);

        // --- 2. 文字處理區 (使用 GridBagLayout 讓拉伸更自然) ---
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 30, 30));
        centerPanel.setOpaque(false);

        inputArea = createStyledTextArea();
        outputArea = createStyledTextArea();
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(220, 225, 230));

        centerPanel.add(createScrollWrapper(inputArea, "輸入原始內容或密文"));
        centerPanel.add(createScrollWrapper(outputArea, "處理結果輸出"));

        // --- 3. 功能控制區 ---
        JPanel bottomPanel = new JPanel(new BorderLayout(20, 20));
        bottomPanel.setOpaque(false);

        // 主要加解密按鈕
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        actionPanel.setOpaque(false);
        actionPanel.setPreferredSize(new Dimension(0, 90));

        JButton encryptBtn = createStyledButton("執行加密 ENCRYPT", new Color(44, 62, 80));
        JButton decryptBtn = createStyledButton("執行解密 DECRYPT", new Color(30, 130, 76));
        actionPanel.add(encryptBtn);
        actionPanel.add(decryptBtn);

        // 輔助工具按鈕
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        toolPanel.setOpaque(false);
        JButton copyBtn = new JButton(" 複製輸出結果 ");
        JButton clearBtn = new JButton(" 清空所有欄位 ");
        toolPanel.add(copyBtn);
        toolPanel.add(clearBtn);

        bottomPanel.add(actionPanel, BorderLayout.CENTER);
        bottomPanel.add(toolPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // --- 邏輯功能 ---
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
            JOptionPane.showMessageDialog(this, "成功！內容已複製到剪貼簿。");
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
        area.setFont(new Font("Monospaced", Font.PLAIN, 22));
        area.setMargin(new Insets(20, 20, 20, 20));
        return area;
    }

    private JScrollPane createScrollWrapper(JTextArea area, String title) {
        JScrollPane scroll = new JScrollPane(area);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(149, 165, 166), 3), title);
        border.setTitleFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
        scroll.setBorder(border);
        return scroll;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));
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
            JOptionPane.showMessageDialog(this, "錯誤：解密失敗，請檢查密碼是否正確。", "提示", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new FullScreenCryptoApp().setVisible(true));
    }
}