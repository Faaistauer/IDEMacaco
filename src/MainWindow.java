/**
 *
 * @author eas
 */
import javax.swing.*;
import gals.Lexico;
import gals.Sintatico;
import gals.Semantico;
import gals.LexicalError;
import gals.SyntacticError;
import gals.SemanticError;

import java.awt.*;


public class MainWindow extends javax.swing.JFrame {

    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        initComponents();
        setSize(1320, 700);
        ImageIcon icon = new ImageIcon(getClass().getResource("/recursos/BenjaminPortrait32.png"));
        setIconImage(icon.getImage());

        // Customização do estilo da barra de rolagem
        jScrollPane1.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = Color.decode("#a0522d");
                this.trackColor = Color.decode("#f5deb3");
            }
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(thumbColor);
                g2.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
                g2.dispose();
            }
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor);
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g2.dispose();
            }
        });
        jScrollPane2.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = Color.decode("#a0522d");
                this.trackColor = Color.decode("#f5deb3");
            }
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(thumbColor);
                g2.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
                g2.dispose();
            }
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor);
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g2.dispose();
            }
        });

        jScrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane2.setPreferredSize(new Dimension(400, 300));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // Generated using JFormDesigner Evaluation license - unknown
    private void initComponents() {
        jScrollPane1 = new JScrollPane();
        sourceInput = new JTextArea();
        jScrollPane2 = new JScrollPane();
        console = new JTextArea();
        buttonCompile = new JButton();
        jScrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        console.setEditable(false);
        console.setColumns(20);
        console.setLineWrap(true);
        console.setRows(5);
        console.setTabSize(4);
        console.setBackground(Color.decode("#dd9830"));
        jScrollPane2.setViewportView(console);

        JLabel gifPlaceholder = new JLabel();
        gifPlaceholder.setBackground(Color.decode("#8B4513"));
        gifPlaceholder.setOpaque(true);

        java.net.URL gifUrl = getClass().getResource("/recursos/bloons-td-6.gif");
        System.out.println("gifUrl: " + gifUrl);
        if (gifUrl != null) {
            ImageIcon icon = new ImageIcon(gifUrl);
            System.out.println("Icon width: " + icon.getIconWidth());
            gifPlaceholder.setIcon(icon);
        } else {
            gifPlaceholder.setText("GIF não encontrado");
        }

        // Painel lateral para gif e botão
        JPanel sidePanel = new JPanel();
        sidePanel.setOpaque(false);
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));

        // Painel para alinhar o gif à esquerda
        JPanel gifPanel = new JPanel();
        gifPanel.setOpaque(false);
        gifPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        gifPanel.add(gifPlaceholder);
        sidePanel.add(gifPanel);

        sidePanel.add(Box.createVerticalStrut(10)); // Espaço entre gif e botão

        // Painel para alinhar o botão à esquerda (já está correto)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.add(buttonCompile);
        sidePanel.add(buttonPanel);

        //======== this ========
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("IDE de macaco");
        var contentPane = getContentPane();
        contentPane.setBackground(Color.decode("#8B4513")); // ou qualquer cor desejada
        //======== jScrollPane1 ========
        {

            //---- sourceInput ----
            sourceInput.setColumns(20);
            sourceInput.setRows(5);
            jScrollPane1.setViewportView(sourceInput);
            sourceInput.setBackground(Color.decode("#dd9830"));
        }

        //======== jScrollPane2 ========
        {

            //---- console ----
            console.setEditable(false);
            console.setColumns(20);
            console.setLineWrap(true);
            console.setRows(5);
            console.setTabSize(4);
            jScrollPane2.setViewportView(console);
            console.setBackground(Color.decode("#dd9830"));
        }

        //---- buttonCompile ----
        buttonCompile.setIcon(new ImageIcon(getClass().getResource("/recursos/banana.png")));
        buttonCompile.setText("<html><div style='margin-top:20px;'>Macacar</div></html>");
        buttonCompile.setHorizontalTextPosition(SwingConstants.CENTER);
        buttonCompile.setVerticalTextPosition(SwingConstants.CENTER);
        buttonCompile.setIconTextGap(900); // Aumente o valor para descer mais, diminua para subir
        buttonCompile.setContentAreaFilled(false);
        buttonCompile.setBorderPainted(false);
        buttonCompile.setFocusPainted(false);
        buttonCompile.setOpaque(false);
        buttonCompile.setForeground(Color.BLACK); // Cor do texto
        buttonCompile.setFont(new Font("Arial", Font.BOLD, 18)); // Fonte e tamanho
        buttonCompile.addActionListener(e -> buttonCompileActionPerformed(e));

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(contentPaneLayout.createParallelGroup()
                        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addComponent(jScrollPane2)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(sidePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(contentPaneLayout.createParallelGroup()
                        .addComponent(jScrollPane2)
                        .addComponent(sidePanel)))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }// </editor-fold>//GEN-END:initComponents

    private void buttonCompileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCompileActionPerformed

        Lexico lex = new Lexico();
        Sintatico sint = new Sintatico();
        Semantico sem = new Semantico();

        lex.setInput(sourceInput.getText());


try {
        sint.parse(lex, sem);
        console.setText("Compilado com sucesso!");
    } catch (LexicalError ex) {
        console.setText("Erro Léxico: " + ex.getLocalizedMessage());
    } catch (SyntacticError ex) {
        console.setText("Erro Sintático: " + ex.getLocalizedMessage());
    } catch (SemanticError ex) {
        console.setText("Erro Semântico: " + ex.getLocalizedMessage());
    }
}
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - unknown
    private JScrollPane jScrollPane1;
    private JTextArea sourceInput;
    private JScrollPane jScrollPane2;
    private JTextArea console;
    private JButton buttonCompile;
    // End of variables declaration//GEN-END:variables
}
