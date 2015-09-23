import java.text.DecimalFormat;
import java.io.*;
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

import javax.sound.midi.*;
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
    static int numComp;
    static int denComp;
    BarraDeProgresso thProgresso;
    Thread threadDeProgresso;

    Track[] trilhas;

    private int volumeATUAL = 75;
    private JSlider sliderVolume = new JSlider(JSlider.HORIZONTAL,0, 127, volumeATUAL);

    JButton botaoAbrir, botaoTocar, botaoPausar, botaoParar;
    JTextField caminhoArq, duracaoBarra, duracaoMaxima, andamento, formulaDeCompasso;
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

        duracaoBarra = new JTextField();
        duracaoBarra.setEditable(false);
        duracaoBarra.setText(constroiStringTempo(0));

        duracaoMaxima = new JTextField();
        duracaoMaxima.setEditable(false);
        duracaoMaxima.setText("Duracao:"+ constroiStringTempo(0));

        andamento = new JTextField();
        andamento.setEditable(false);
        andamento.setText("Andamento:"+" BPM");

        formulaDeCompasso = new JTextField();
        formulaDeCompasso.setEditable(false);
        formulaDeCompasso.setText("Fórmula de Compasso: ");



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


        painelLog.add(caminhoArq);
        painelLog.add(duracaoMaxima);

        JPanel botaoPainel = new JPanel();
        botaoPainel.add(botaoAbrir);
        botaoPainel.add(botaoTocar);
        botaoPainel.add(botaoPausar);
        botaoPainel.add(botaoParar);
        botaoPainel.add(formulaDeCompasso);

        JPanel barraPainel = new JPanel();
        barraPainel.add(barraProgresso);
        barraPainel.add(duracaoBarra);

        JPanel volumePainel = new JPanel();
        volumePainel.add(sliderVolume);

        JPanel parametropartPainel = new JPanel();
        parametropartPainel.add(andamento);


        //adicionando o botao e log aos paineis painel
        add(painelLog, BorderLayout.PAGE_START);
        add(barraPainel, BorderLayout.CENTER);
        add(parametropartPainel, BorderLayout.BEFORE_LINE_BEGINS);
        add(volumePainel, BorderLayout.AFTER_LINE_ENDS);
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
                duracaoMaxima.setText("Duracao:"+constroiStringTempo(duracao));


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
            //duracaoBarra.setText(constroiStringTempo(tempoAtual));

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
            duracaoBarra.setText(constroiStringTempo(0));
    }

    public class BarraDeProgresso implements Runnable{
        public void run(){

            while(true){
                if(soando){
                    tempoTotal = sequenciador.getMicrosecondLength();
                    tempoAtual = sequenciador.getMicrosecondPosition();
                    progresso = 100*tempoAtual/tempoTotal;
                    barraProgresso.setValue((int)progresso);
                    duracaoBarra.setText(constroiStringTempo(tempoAtual/1000000));

                    andamento.setText("Andamento: "+getAndamento()+" BPM");

                    trilhas = sequencia.getTracks();
                    Par fc = getFormulaDeCompasso(trilhas[0]);
	            formulaDeCompasso.setText("Fórmula de Compasso: " + fc.getX() +":"+ (int)(Math.pow(2, fc.getY())) );

                    String st;
                    try{
                        st = getTonalidade(trilhas[0]);
                        System.out.println("Tonalidade         : " + st);
                    }
                    catch(Exception e){}


                }
                try
                {
                    Thread.sleep(100);
                }
                catch(InterruptedException e)
                {
                }
            }
        }
    }

    public String reformata(double x, int casas)
      {
          DecimalFormat df = new DecimalFormat() ;
          df.setGroupingUsed(false);
          df.setMaximumFractionDigits(casas);
          return df.format(x);
      }

    public String constroiStringTempo(double t1)
        {
            String inicio    = "";
            //--------início
            double h1  = (int)(t1/3600.0);
            double m1  = (int)((t1 - 3600*h1)/60);
            double s1  = (t1 - (3600*h1 +60*m1));


            double h1r  = t1/3600.0;
            double m1r  = (t1 - 3600*h1)/60.0f;
            double s1r  = (t1 - (3600*h1 +60*m1));

            String sh1="";
            String sm1="";
            String ss1="";

            if     (h1 ==0) sh1 = "00";
            else if(h1 <10) sh1 = "0"+reformata(h1, 0);
            else if(h1<100) sh1 = "" +reformata(h1, 0);
            else            sh1 = "" +reformata(h1, 0);

            if     (m1 ==0) sm1 = "00";
            else if(m1 <10) sm1= "0"+reformata(m1, 0);
            else if(m1 <60) sm1 = ""+reformata(m1, 0);

            if     (s1 ==0) ss1 = "00";
            else if(s1 <10) ss1 = "0"+reformata(s1r, 2);
            else if(s1 <60) ss1 = reformata(s1r, 2);

            return inicio = "\n" + "   "+sh1+"h "+       sm1+"m "+    ss1+"s";
        }

    public String getAndamento(){
        float andamento = sequenciador.getTempoInBPM();
        return reformata(andamento,2);
    }

    static final int FORMULA_DE_COMPASSO = 0x58;
    static Par getFormulaDeCompasso(Track trilha)
    {   int p=1;
        int q=1;

        for(int i=0; i<trilha.size(); i++)
        {
          MidiMessage m = trilha.get(i).getMessage();
          if(m instanceof MetaMessage)
          {
            if(((MetaMessage)m).getType()==FORMULA_DE_COMPASSO)
            {
                MetaMessage mm = (MetaMessage)m;
                byte[] data = mm.getData();
                p = data[0];
                q = data[1];
                return new Par(p,q);
            }
          }
        }
        return new Par(p,q);
    }



    static private class Par
    { int x, y;

      Par (int x_, int y_)
      { this.x = x_;
        this.y = y_;
      }

      int getX()
      { return x;
      }

      int getY()
      { return y;
      }

    }

    static final int MENSAGEM_TONALIDADE = 0x59;

    static String getTonalidade(Track trilha) throws InvalidMidiDataException
    {
       String stonalidade = "";
       for(int i=0; i<trilha.size(); i++)
       { MidiMessage m = trilha.get(i).getMessage();


       if(((MetaMessage)m).getType() == MENSAGEM_TONALIDADE)
       {
            MetaMessage mm        = (MetaMessage)m;
            byte[]     data       = mm.getData();
            byte       tonalidade = data[0];
            byte       maior      = data[1];

            String       smaior = "Maior";
            if(maior==1) smaior = "Menor";

            if(smaior.equalsIgnoreCase("Maior"))
            {
                switch (tonalidade)
                {
                    case -7: stonalidade = "Dób Maior"; break;
                    case -6: stonalidade = "Solb Maior"; break;
                    case -5: stonalidade = "Réb Maior"; break;
                    case -4: stonalidade = "Láb Maior"; break;
                    case -3: stonalidade = "Mib Maior"; break;
                    case -2: stonalidade = "Sib Maior"; break;
                    case -1: stonalidade = "Fá Maior"; break;
                    case  0: stonalidade = "Dó Maior"; break;
                    case  1: stonalidade = "Sol Maior"; break;
                    case  2: stonalidade = "Ré Maior"; break;
                    case  3: stonalidade = "Lá Maior"; break;
                    case  4: stonalidade = "Mi Maior"; break;
                    case  5: stonalidade = "Si Maior"; break;
                    case  6: stonalidade = "Fá# Maior"; break;
                    case  7: stonalidade = "Dó# Maior"; break;
                }
            }

            else if(smaior.equalsIgnoreCase("Menor"))
            {
                switch (tonalidade)
                {
                    case -7: stonalidade = "Láb Menor"; break;
                    case -6: stonalidade = "Mib Menor"; break;
                    case -5: stonalidade = "Sib Menor"; break;
                    case -4: stonalidade = "Fá Menor"; break;
                    case -3: stonalidade = "Dó Menor"; break;
                    case -2: stonalidade = "Sol Menor"; break;
                    case -1: stonalidade = "Ré Menor"; break;
                    case  0: stonalidade = "Lá Menor"; break;
                    case  1: stonalidade = "Mi Menor"; break;
                    case  2: stonalidade = "Si Menor"; break;
                    case  3: stonalidade = "Fá# Menor"; break;
                    case  4: stonalidade = "Dó# Menor"; break;
                    case  5: stonalidade = "Sol# Menor"; break;
                    case  6: stonalidade = "Ré# Menor"; break;
                    case  7: stonalidade = "Lá# Menor"; break;
                }
            }
         }
      }
      return stonalidade;
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
