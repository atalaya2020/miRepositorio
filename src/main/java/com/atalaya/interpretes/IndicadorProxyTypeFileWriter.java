package com.atalaya.interpretes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndicadorProxyTypeFileWriter implements IIndicadorProxyType {

	private static IndicadorProxyTypeFileWriter instance;
	
	protected static Logger log;
	/** 
	 * Constructor.
	 */
	protected IndicadorProxyTypeFileWriter(){}
	
	/** 
	 * Este metodo asegura que exista únicamente una instancia de esta clase. Devuelve el valor del atributo instance en caso de tener valor y si no es
	 * así genera una instancia de la clase y la devuelve como resultado.
	 */
	public static IndicadorProxyTypeFileWriter getInstance()
	{
		//Si el atributo instance no tiene valor significa que no existe ninguna instancia de la clase
		if (instance==null)
		{
			//Da valor al atributo instance, creando una instancia de la clase
			instance = new IndicadorProxyTypeFileWriter();
		}
		log = LoggerFactory.getLogger(IndicadorProxyTypeFileWriter.class);
		
		//Devuelve la instancia unica de la clase AliasTypeLoop
		return instance;
	}	

	
	public boolean ejecutar(IndicadorProxy indicador) {
		
		log.info("Ejecutando indicador...");
		
		boolean exec = false;
		String separador = "";
		
		BufferedWriter bw = null;
		Object[] resultado = new Object[3];
		
		try
		{	

			String[] comando= indicador.getIndicador().getComando().split(" ");
			String rutaFichero = comando[0];
			
			if (comando.length>1) { //tengo informado el separador
				separador = comando[1];
			}else { // uso el separador por defecto
				separador = instance.separadorFichero;
			}
			
			String indicadorDependiente = indicador.getIndicador().getParametros().get(0).getValor();
			
			
			IndicadorProxy indicador_a = indicador.getIndicadorNombre(indicador.getAnalisis(), indicadorDependiente);
//			IndicadorProxy indicador_a = indicador.getIndicadores().get(indicadorDependiente); 
			

			//Recupero el resultado del indicador A
			Vector<Object[]> resultado_indicador_a = (Vector<Object[]>)indicador_a.getResultadoEjecucion();
			
			File fichero = new File(rutaFichero);
			bw = new BufferedWriter(new FileWriter(fichero));
			
			String cabecera = "NOMBRE" + separador + "APELLIDOS" + separador + "IDENTIFICADOR" + separador + "PROVINCIA";
			bw.write(cabecera + "\n");
			
			for(int i = 0; i<resultado_indicador_a.size()  ; i++) {
				
				Object[] resultados = resultado_indicador_a.get(i);
				
				String nombre = (String)resultados[0];
				String apellidos = (String)resultados[1];
				String id = resultados[2].toString();
				String provincia = (String)resultados[3];
				
				String salida =  nombre + separador + apellidos + separador + id + separador + provincia +"\n";
				bw.write(salida);
				
			}
					
			bw.close();
			
			indicador.setEstado(IndicadorProxy.ESTADO_EJECUTADO);
			exec=true;
		}
		catch (Exception e)
		{
			try {
				bw.close();
			}catch (Exception f){
				System.out.println(f);
			}
			indicador.setEstado(IndicadorProxy.ESTADO_EJECUTADO);
		}
		
		return exec;
	}
	
}


