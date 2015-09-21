
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

public class TocadorMidVelho extends JPanel implements ActionListener, ChangeListener {

    private  int largura = 390;
    private  int altura  = 280;
    private long inicio = 0;
    static long progresso;
    static long tempoTotal;
    static long tempoAtual;
    BarraDeProgresso thProgresso;
    Thread threadDeProgresso;

    private int volumeATUAL = 75;
    private JSlider sliderVolume = new JSlider(JSlider.HORIZONTAL,0, 127, volumeATUAL);        

    JButton botaoAbrir, botaoTocar, botaoPausar, botaoParar;
    JTextField caminhoArq;
    JFileChooser pa;
    static JProgressBar barraProgresso;
            

    private boolean soando =false;

    static private Sequencer  sequenciador = null;
    private Sequence   sequencia;
    private Receiver   receptor;


    public TocadorMidVelho() {
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

        sliderVolume.setMajorTickSpacing(30);
        sliderVolume.setMinorTickSpacing(10);
        sliderVolume.setPaintTicks(true);
        sliderVolume.setPaintLabels(true);   

        botaoAbrir.addActionListener(this);
        botaoTocar.addActionListener(this);
        botaoPausar.addActionListener(this);
        botaoParar.addActionListener(this);
        sliderVolume.addChangeListener(this);        
        
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

        JPanel volumePainel = new JPanel();
        volumePainel.add(sliderVolume);     
 
        //adicionando o botao e log aos paineis painel
        add(painelLog, BorderLayout.BEFORE_FIRST_LINE);
        add(barraPainel,BorderLayout.LINE_START);
        add(volumePainel,BorderLayout.LINE_END);
        add(botaoPainel, BorderLayout.PAGE_END);

        thProgresso = new BarraDeProgresso();
        threadDeProgresso = new Thread(thProgresso);
        threadDeProgresso.start();
        
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

    public void AlterarVolume(){
        if(!sliderVolume.getValueIsAdjusting()){

            int valor = (int)sliderVolume.getValue();

            ShortMessage mensagemDeVolume = new ShortMessage();
            for(int i=0; i<16; i++){
                try { 
                    mensagemDeVolume.setMessage(ShortMessage.CONTROL_CHANGE, i, 7, valor);
                    receptor.send(mensagemDeVolume, -1);
                }
                catch (InvalidMidiDataException e1) {
                    System.out.println("Erro ao alterar o vomlume");
                }
            }
            volumeATUAL = valor;
        }
    }



    public void stateChanged(ChangeEvent e){
        if(e.getSource() == sliderVolume){
            if(soando)
                AlterarVolume();
        }
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
    
    public class BarraDeProgresso implements Runnable{
        public void run(){

            while(true){
                //System.out.println("");
                if(soando){
                    tempoTotal = sequenciador.getMicrosecondLength();
                    tempoAtual = sequenciador.getMicrosecondPosition();
                    progresso = 100*tempoAtual/tempoTotal;
                    barraProgresso.setValue((int)progresso);
                }
            }
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
        janela.add(new TocadorMidVelho());

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