import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.*;

public class TocadorMid extends JPanel implements ActionListener {
    JButton botaoAbrir;
    JTextArea log;
    JFileChooser pa;

    public TocadorMid() {
        super(new BorderLayout());
 
        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane painelLog = new JScrollPane(log);
 
        //Create a file chooser
        pa = new JFileChooser();
 
        botaoAbrir = new JButton("abrir");
                                
        botaoAbrir.addActionListener(this);
 
        
 
        
        JPanel botaoPainel = new JPanel(); 
        botaoPainel.add(botaoAbrir);
        
 
        //adicionando o botao e log a este painel
        add(botaoPainel, BorderLayout.PAGE_START);
        add(painelLog, BorderLayout.CENTER);
    }


    public void actionPerformed(ActionEvent e) {
 
        if (e.getSource() == botaoAbrir) {
            int valorRetorno = pa.showOpenDialog(TocadorMid.this);
 
            if (valorRetorno == JFileChooser.APPROVE_OPTION) {
                File arq = pa.getSelectedFile();
                //Operaçao de abrir o arquivo
                log.append("Abrindo: " + arq.getName() + ".\n" );
            } else {
                log.append("Operacao de abrir cancelada pelo o usuario.\n");
            }
            log.setCaretPosition(log.getDocument().getLength());
        }
    }


    private static void createAndShowGUI() {
        //Cria a janela
        JFrame janela = new JFrame("Tocador de mid");
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //adciona o busca asrquivo na janela
        janela.add(new TocadorMid());

        //Mostra a janela
        janela.pack();
        janela.setVisible(true);
    }
    public static void main(String[] args) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    UIManager.put("swing.boldMetal", Boolean.FALSE); 
                    createAndShowGUI();
                }
            });
        }
}