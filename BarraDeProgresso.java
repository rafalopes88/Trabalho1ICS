public class BarraDeProgresso implements Runnable{

		public void run(){
			tempoTotal = sequenciador.getMicrosecondLength();
	        tempoAtual = sequenciador.getMicrosecondPosition();
	        progresso = 100*tempoAtual/tempoTotal;
	        barraProgresso.setValue((int)progresso);
		}
}