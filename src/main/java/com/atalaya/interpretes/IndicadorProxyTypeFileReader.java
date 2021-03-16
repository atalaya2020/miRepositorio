package com.atalaya.interpretes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndicadorProxyTypeFileReader implements IIndicadorProxyType {

	private static IndicadorProxyTypeFileReader instance;
	private static Logger log = null;
	
	
	/** 
	 * Constructor.
	 */
	protected IndicadorProxyTypeFileReader(){}
	
	/** 
	 * Este metodo asegura que exista únicamente una instancia de esta clase. Devuelve el valor del atributo instance en caso de tener valor y si no es
	 * así genera una instancia de la clase y la devuelve como resultado.
	 */
	public static IndicadorProxyTypeFileReader getInstance()
	{
		//Si el atributo instance no tiene valor significa que no existe ninguna instancia de la clase
		if (instance==null)
		{
			//Da valor al atributo instance, creando una instancia de la clase
			instance = new IndicadorProxyTypeFileReader();
		}

		log = LoggerFactory.getLogger(IndicadorProxyTypeFileReader.class);
		
		//Devuelve la instancia unica de la clase AliasTypeLoop
		return instance;
	}	

	
	public boolean ejecutar(IndicadorProxy indicador) {
		
		log.info("Ejecutando indicador...");
		
		boolean exec = false;
		
		FileReader fReader = null; 
		BufferedReader bReader = null;
		
		try
		{			
			int numeroParametrosSalida = indicador.getIndicador().getResultado().length;
			
			String fuenteDatos = indicador.getIndicador().getFuente();
			log.info("La fuente de datos va a ser" + fuenteDatos);
			
			
			String separador = "";
			List<String> posiblesSeparadores = Arrays.asList( "^" , "|" , "?" , "*" , "+" , "(" , ")" , "{" , "}" , "[" );
			
			Vector<Object[]> vResultado = new Vector<Object[]>();
			
			//Se cargan los campos de la cabecera
			Object[] row = new Object[numeroParametrosSalida]; 
			row[0] = indicador.getIndicador().getResultado()[0];
			row[1] = indicador.getIndicador().getResultado()[1];
			row[2] = indicador.getIndicador().getResultado()[2];
			row[3] = indicador.getIndicador().getResultado()[3];
			
			vResultado.add(row);
						
			// En comando se encuentra la ruta y el separador a usar separados por un espacio en blando. 
			// En el caso de que no viaje separador, se pondra el | por defecto
			// El nombre del fichero debe ser el primer parámetro que vaya en el comando. El separador podrá viajar o no
			String[] rutaSeparador= indicador.getIndicador().getComando().split(" ");
			
			//String rutaFichero = rutaSeparador[0] + indicador.getIndicador().getParametros().get(0).getValor();
			String rutaFichero = rutaSeparador[0];
			
			if (rutaSeparador.length>0) { 
				separador = rutaSeparador[1];
				
				if (posiblesSeparadores.contains(separador)) {
					separador = "\\" + separador;
				}
				
				
			}else {
				separador = instance.separadorFichero;
			}
			
			File fIn = new File (rutaFichero); 
			fReader = new FileReader (fIn); 
			bReader = new BufferedReader(fReader); 
				
			String sLinea = bReader.readLine();
			
			while (sLinea!=null) {
				
				//El separador del fichero lo voy a tener que recoger del comando en el indicador dentro del analisis
				if (sLinea.split(separador).length!=numeroParametrosSalida) { 
					log.info("Linea del fichero descartada por no cumplir validacion de numero de parametros");
				}else {
					row = new Object[numeroParametrosSalida];
					String[] partes = sLinea.split(separador);
					
					row[0] = partes[0];					row[1] = partes[1];
					row[2] = partes[2];					row[3] = partes[3]; 
						
					vResultado.add(row);
						
				}
				sLinea = bReader.readLine();
					
			}
			indicador.setResultadoEjecucion(vResultado);

			
//			Vector<Object[]> vResultado = indicador.getResultadoEjecucion();
			
			Object[] salida = null;
			String nombre;
			String apellido;
			String id;
			String ciudad;
			
			for (int i=1 ; i<vResultado.size(); i++) {
				salida 	= vResultado.get(i);
				
				id 			= (String)salida[2];
				nombre   	= (String)salida[0];
				apellido 	= (String)salida[1];
				ciudad 		= (String)salida[3];
			
				log.info(nombre + "|" + apellido + "|" + id + "|" + ciudad);
//				log.info(nombre + "|" + apellido + "|" + id );
			}
			
			


			
			
			indicador.setEstado(IndicadorProxy.ESTADO_EJECUTADO);
			exec=true;
		}
		catch (Exception e)
		{
			indicador.setEstado(IndicadorProxy.ESTADO_EJECUTADO);
		}
		
		return exec;
		
	}
	
}



