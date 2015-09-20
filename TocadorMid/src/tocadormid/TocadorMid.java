package tocadormid;

import java.text.DecimalFormat;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JSlider;
import javax.swing.JProgressBar;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;

import java.awt.Dimension;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Font;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.InvalidMidiDataException;

public class TocadorMid extends JPanel implements ActionListener {

    private  int largura = 390;
    private  int altura  = 280;
    private long inicio = 0;

    JButton botaoAbrir, botaoTocar, botaoPausar, botaoParar;
    JTextField caminhoArq;
    JFileChooser pa;
    JProgressBar barraProgresso;
            

    private boolean soando;

    private Sequencer  sequenciador = null;
    private Sequence   sequencia;
    private Receiver   receptor;

    public TocadorMid() {
        super(new BorderLayout());
 
        /*log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane painelLog = new JScrollPane(log);*/
        JPanel painelLog = new JPanel();
        caminhoArq = new JTextField();
        caminhoArq.setText(System.getProperty("user.dir"));
        caminhoArq.setEditable(false);
        painelLog.add(caminhoArq);


        pa = new JFileChooser();
        botaoAbrir = new JButton("Abrir");
        botaoTocar = new JButton("Play");
        botaoPausar = new JButton("Pause");
        botaoParar = new JButton("Stop");
        barraProgresso = new JProgressBar();
        //barraProgresso.setStringPainted(true);

        

        botaoAbrir.addActionListener(this);
        botaoTocar.addActionListener(this);
        botaoPausar.addActionListener(this);
        botaoParar.addActionListener(this);
        
        botaoTocar.setEnabled(false);
        botaoPausar.setEnabled(false);
        botaoParar.setEnabled(false);
        barraProgresso.setValue(0);
 
        
        JPanel botaoPainel = new JPanel(); 
        botaoPainel.add(botaoAbrir);
        botaoPainel.add(botaoTocar);
        botaoPainel.add(botaoPausar);
        botaoPainel.add(botaoParar);
        
        JPanel barraPainel = new JPanel();
        barraPainel.add(barraProgresso);
        
        
 
        //adicionando o botao e log aos paineis painel
        add(painelLog, BorderLayout.BEFORE_FIRST_LINE);
        add(barraPainel,BorderLayout.CENTER);
        add(botaoPainel, BorderLayout.PAGE_END);
        
    }
    public void Abrir(){
        JFileChooser selecao = new JFileChooser(".");  
        selecao.setFileSelectionMode(JFileChooser.FILES_ONLY);              
        selecao.setFileFilter(new FileFilter() {
            public boolean accept(File f){
                if (!f.isFile()) 
                    return false;
                   
                String name = f.getName().toLowerCase();
                    
                return name.endsWith(".mid") || name.endsWith(".midi");
                    
            }

            public String getDescription(){

                return "Arquivo Midi (*.mid,*.midi)";
            }
        });

        selecao.showOpenDialog(this);  

        if(selecao.getSelectedFile() != null){
            caminhoArq.setText(selecao.getSelectedFile().toString());  
            File arqseqnovo = selecao.getSelectedFile();
            try { 
                if(sequenciador!=null && sequenciador.isRunning()) { 
                    sequenciador.stop();
                    sequenciador.close();
                    sequenciador = null;
                }
                Sequence sequencianova = MidiSystem.getSequence(arqseqnovo);           
                double duracao = sequencianova.getMicrosecondLength()/1000000.0d;
                 
                //botaoMOSTRADORarquivo.setText("Arquivo: \"" + arqseqnovo.getName() + "\"");                
                //botaoMOSTRADORduracao.setText("\nDura\u00e7\u00e3o:"+ formataInstante(duracao));                   
                  
                botaoTocar.setEnabled(true);
                botaoPausar.setEnabled(false);
                botaoParar.setEnabled(false);                                    
            }
            catch (Throwable e1) { 
                System.out.println("Erro em carregaArquivoMidi: "+ e1.toString());
            }
        }
    }

    public void Tocar(String caminho){
        try{
            File arqmidi = new File(caminho);
            sequencia    = MidiSystem.getSequence(arqmidi);  
            sequenciador = MidiSystem.getSequencer();  

            sequenciador.setSequence(sequencia); 
            sequenciador.open();  
            Thread.sleep(500);
            sequenciador.start();  
            
            receptor = sequenciador.getTransmitters().iterator().next().getReceiver();
            sequenciador.getTransmitter().setReceiver(receptor);
         
            //botaoMOSTRADORarquivo.setText("Arquivo: \"" + arqmidi.getName() + "\"");
                                                   
            long duracao  = sequencia.getMicrosecondLength()/1000000;
            //botaoMOSTRADORduracao.setText("\nDura\u00e7\u00e3o:"+ formataInstante(duracao)); 
            //botaoMOSTRADORinstante.setText(formataInstante(0));                
                                            
            sequenciador.setMicrosecondPosition(inicio);

            if (sequenciador.isRunning()){ 
                duracao = sequenciador.getMicrosecondLength();
                soando = true;
                progressaoBarra();
            } 
            else { 
                soando = false; 
                sequenciador.stop();  
                sequenciador.close();
                inicio = 0L;
                duracao = 0;
            }  
            
             botaoAbrir.setEnabled(false);
             botaoTocar.setEnabled(false);
             botaoPausar.setEnabled(true);
             botaoParar.setEnabled(true);                
                
        }
        catch(MidiUnavailableException e1) { 
            System.out.println(e1+" : Dispositivo midi nao disponivel.");
        }
        catch(InvalidMidiDataException e2) { 
            System.out.println(e2+" : Erro nos dados midi."); 
        }
        catch(IOException e3) { 
            System.out.println(e3+" : O arquivo midi nao foi encontrado.");   
        }
        catch(Exception e){  
            System.out.println(e.toString());  
        }  
    }

    public void Pausar(){

            inicio = sequenciador.getMicrosecondPosition();
            soando = false;
            sequenciador.stop();  
            
            botaoAbrir.setEnabled(false);            
            botaoTocar.setEnabled(true);
            botaoPausar.setEnabled(false);
            //botaoPARAR.setEnabled(false);            
    }



    public void actionPerformed(ActionEvent e) {
 
        if (e.getSource() == botaoAbrir) {
            Abrir();
        }
        else if(e.getSource() == botaoTocar){
            Tocar(caminhoArq.getText());

        }
        else if(e.getSource() == botaoPausar){
            Pausar();
        }
        else if(e.getSource() == botaoParar){
            Parar();
        }
        
    }

    public void Parar() {
            soando = false;
            sequenciador.stop();  
            sequenciador.close();
            sequenciador = null;
            inicio = 0L;
            
             botaoAbrir.setEnabled(true);            
             botaoTocar.setEnabled(true);
             botaoPausar.setEnabled(false);
             botaoParar.setEnabled(false);
             barraProgresso.setValue(0);
             //sliderPROGRESSOinstante.setValue(0);             
             //botaoMOSTRADORinstante.setText(formataInstante(0));      
    }
    
    public void progressaoBarra(){
        long progresso;
        long tempoTotal;
        long tempoAtual;
        while(soando == true){
            tempoTotal = sequenciador.getMicrosecondLength();
            tempoAtual = sequenciador.getMicrosecondPosition();
            progresso = 100*tempoAtual/tempoTotal;
            System.out.println(progresso);
            barraProgresso.setValue((int)progresso);
        }
    }
    
    
/*        public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
            taskOutput.append(String.format(
                    "Completed %d%% of task.\n", task.getProgress()));
        } 
    }*/
    


    private static void createAndShowGUI() {
        //Cria a janela
        JFrame janela = new JFrame("Tocador de mid");
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //adciona o busca arquivo na janela
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