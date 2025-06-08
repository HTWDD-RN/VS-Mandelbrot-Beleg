import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Swing-View: Eingabefelder statt Slider
 */
public class ApfelView extends JFrame {
    private final MandelPanel panel = new MandelPanel();

    // Eingabe-Felder für Bildgröße
    private final JTextField inputX  = new JTextField("1024", 5);
    private final JTextField inputY  = new JTextField("768", 5);

    // Eingabe-Felder für Cr/Ci
    private final JTextField inputCr = new JTextField("-0.34837308755059104", 12);
    private final JTextField inputCi = new JTextField("-0.6065038451823017", 12);

    // Neu: Eingabe-Felder statt Slider
    private final JTextField inputRounds   = new JTextField("100", 5);
    private final JTextField inputIter     = new JTextField("1000", 5);
    private final JTextField inputThreads  = new JTextField("4", 5);
    private final JTextField inputLayers   = new JTextField("2", 5);
    private final JTextField inputZoom     = new JTextField("0.8", 5); // Zoom*10

    private final JButton btnStart = new JButton("Start");
    private final JButton btnPause = new JButton("Pause");
    private final JButton btnReset = new JButton("Reset");

    private final JLabel lblStatus = new JLabel("Ready");
    private final JLabel lblTime   = new JLabel("0.0s");

    // Lokale Berechnung
    private final JCheckBox checkboxLocal = new JCheckBox("Nur lokal ausführen");

    public ApfelView(ApfelPresenter p) {
        super("Verteiltes Mandelbrot (schwarz/rot)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        panel.setBackground(Color.BLACK);
        add(panel, BorderLayout.CENTER);

        add(createControlPanel(p), BorderLayout.EAST);

        pack();
        setSize(1024, 768);
        setLocationRelativeTo(null);
    }

    private JPanel createControlPanel(ApfelPresenter p) {
        JPanel cp = new JPanel();
        cp.setBackground(Color.BLACK);
        cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));
        cp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkboxLocal.setForeground(Color.RED);
        checkboxLocal.setBackground(Color.BLACK);
        checkboxLocal.setAlignmentX(Component.CENTER_ALIGNMENT);
        cp.add(checkboxLocal);
        cp.add(Box.createVerticalStrut(10));

        cp.add(makeLabeledField("X-Pixel", inputX));
        cp.add(Box.createVerticalStrut(5));
        cp.add(makeLabeledField("Y-Pixel", inputY));
        cp.add(Box.createVerticalStrut(10));

        cp.add(makeLabeledField("Cr-Wert", inputCr));
        cp.add(Box.createVerticalStrut(5));
        cp.add(makeLabeledField("Ci-Wert", inputCi));
        cp.add(Box.createVerticalStrut(10));

        cp.add(makeLabeledField("Runden", inputRounds));
        cp.add(Box.createVerticalStrut(10));
        cp.add(makeLabeledField("Iterationen", inputIter));
        cp.add(Box.createVerticalStrut(10));
        cp.add(makeLabeledField("Threads", inputThreads));
        cp.add(Box.createVerticalStrut(10));
        cp.add(makeLabeledField("Layers", inputLayers));
        cp.add(Box.createVerticalStrut(10));
        cp.add(makeLabeledField("Zoomfaktor", inputZoom));
        cp.add(Box.createVerticalStrut(20));

        btnStart.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPause.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnReset.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnStart.addActionListener(e -> p.onStart());
        //btnPause.addActionListener(e -> p.onPause());
        btnReset.addActionListener(e -> p.onReset());
        cp.add(btnStart);
        cp.add(Box.createVerticalStrut(5));
        //cp.add(btnPause);
        //cp.add(Box.createVerticalStrut(5));
        cp.add(btnReset);
        cp.add(Box.createVerticalStrut(15));

        lblStatus.setForeground(Color.RED);
        lblTime.setForeground(Color.RED);
        cp.add(lblStatus);
        cp.add(Box.createVerticalStrut(5));
        cp.add(lblTime);

        return cp;
    }

    private JPanel makeLabeledField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setBackground(Color.BLACK);
        JLabel lbl = new JLabel(label + ": ");
        lbl.setForeground(Color.RED);
        p.add(lbl, BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        field.setBackground(Color.DARK_GRAY);
        field.setForeground(Color.RED);
        field.setBorder(BorderFactory.createLineBorder(Color.RED));
        return p;
    }

    public int getImageWidth() {
        try {
            return Integer.parseInt(inputX.getText());
        } catch (NumberFormatException e) {
            return 800;
        }
    }

    public int getImageHeight() {
        try {
            return Integer.parseInt(inputY.getText());
        } catch (NumberFormatException e) {
            return 600;
        }
    }

    public double getCr() {
        try {
            return Double.parseDouble(inputCr.getText());
        } catch (NumberFormatException e) {
            return -0.743643887035151;
        }
    }

    public double getCi() {
        try {
            return Double.parseDouble(inputCi.getText());
        } catch (NumberFormatException e) {
            return 0.131825904205330;
        }
    }

    public int getRounds() {
        try {
            return Integer.parseInt(inputRounds.getText());
        } catch (NumberFormatException e) {
            return 100;
        }
    }

    public int getMaxIter() {
        try {
            return Integer.parseInt(inputIter.getText());
        } catch (NumberFormatException e) {
            return 1000;
        }
    }

    public int getThreads() {
        try {
            return Integer.parseInt(inputThreads.getText());
        } catch (NumberFormatException e) {
            return 4;
        }
    }

    public int getLayers() {
        try {
            return Integer.parseInt(inputLayers.getText());
        } catch (NumberFormatException e) {
            return 2;
        }
    }

    public double getZoomRate() {
        try {
            return Double.parseDouble(inputZoom.getText());
        } catch (NumberFormatException e) {
            return 0.8;
        }
    }

    public double getInitialXmin() { return -0.743748; }
    public double getInitialXmax() { return -0.743540; }
    public double getInitialYmin() { return 0.131748; }
    public double getInitialYmax() { return 0.131904; }

    public void resetStatus() {
        updateStatus("Ready");
        updateTime(0.0);
    }

    public void updateStatus(String txt) {
        lblStatus.setText(txt);
    }

    public void updateTime(double s) {
        lblTime.setText(String.format("%.1fs", s));
    }

    public void updateImage(BufferedImage img) {
        panel.setImage(img);
        panel.repaint();
    }

    public boolean isLocalOnly() {
        return checkboxLocal.isSelected();
    }


    private static class MandelPanel extends JPanel {
        private BufferedImage image;

        MandelPanel() {
            setBackground(Color.BLACK);
        }

        void setImage(BufferedImage img) {
            this.image = img;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                int w = getWidth();
                int h = getHeight();
                g.drawImage(image, 0, 0, w, h, null);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(800, 600);
        }
    }
}
