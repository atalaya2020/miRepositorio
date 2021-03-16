package com.atalaya.interpretes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import com.atalaya.evaluador.Comunes;
import com.atalaya.evaluador.Condicion;
import com.atalaya.evaluador.CondicionMultiple;
import com.atalaya.evaluador.Operando;
import com.modelodatos.Criterio;

public class CriterioProxy extends Ejecutable implements Runnable {

	private Criterio criterio;
	
	private ArrayList<CondicionMultiple> evaluacion = new ArrayList<CondicionMultiple>();
	private int indCondicion = 0;
	private long analisis;


	String cabeceralog;
	
	private static final String operadores [] = new String [] {"<=", ">=", "<>", "=", "<", ">"};	
	private static final String opLogicos [] = new String [] {" AND ", " OR "};
	public final char abre = '(';
	public final char cierra = ')';
	
	public CriterioProxy(Criterio criterio) {
		
		this.criterio = criterio;
		
		setEstado(ESTADO_NOEJEUCTADO);
		
		setHashCode(hashCode());
		cabeceralog = "Criterio " + criterio.getNombre() + "|" + this.getHashCode() + ":";
	}

	public void setCriterio(Criterio criterio)  {
		this.criterio = criterio;
	}
	
	public Criterio getCriterio()  {
		return this.criterio;
	}
	
	public long getAnalisis() {
		return analisis;
	}

	public void setAnalisis(long analisis) {
		this.analisis = analisis;
	}
	
	public void run()
	{
		ejecutar();
	}
	
	public boolean ejecutar() {
		
		String cabeceralog = this.getCriterio().getNombre() + " - " + this.getHashCode() + ":";
		
		boolean result = false;
		
		if (this.noejecutado())
		{
			this.setEstado(ESTADO_EJECUTANDO);
			setCrono(Calendar.getInstance().getTimeInMillis());
			log.info(cabeceralog + "Ejecutando criterio...");
			log.info(cabeceralog + this.getCriterio().getDescripcion());
			
			//Bucle para el control del ciclo de vida del hilo
			while(!this.ejecutado())
			{			
				CondicionMultiple multiple;
		
				ArrayList<String> errores = new ArrayList<String>();
				
				evaluacion.clear();
				errores.clear();
				
				validarCondicion();
				
				//informa la lista evaluacion
				extraerCondiciones();
				
				int maxNivel = 0;
				int minNivel = 0;
				
				for (int i = 0; i < evaluacion.size(); i++) { 
					multiple = evaluacion.get(i);
					// Se obtienen los niveles de profundidad minimo y maximo de las condiciones  
					if (multiple.getNivel() > maxNivel) { maxNivel = multiple.getNivel(); }
					if (multiple.getNivel() < minNivel) { minNivel = multiple.getNivel(); }	
				}
				
				// Para obtener el resultado de la condicion, se evalúan las condiciones simples de mayor a menor nivel de profundidad, teniendo en cuenta las relaciones definidas entre ellas 
				for (int nivel = maxNivel; nivel >= minNivel; nivel--) 
				{
					for (int i = 0; i < evaluacion.size(); i++) 
					{ 
						multiple = evaluacion.get(i);
						if (multiple.getNivel() == nivel) 
						{
							evaluaCondicionMultiple(multiple);
							// El resultado de la condicion completa es el resultado de la condicion del nivel minimo
							if (nivel == minNivel) 
							{
								if (multiple.getResultado()) 
								{
									result = true;
									this.setEstado(ESTADO_EJECUTADO);
									this.setDescripcionEstado(FIN_OK);
									log.info(cabeceralog+"Criterio cumplido");
									//eventosCriterio(criterio); //TAREA parece logico transformar el objeto Evento como Ejecutable
								} 
								else 
								{
									result = false;
									this.setEstado(ESTADO_EJECUTADO);
									this.setDescripcionEstado(FIN_OK_SIN_RESULTADO);
									log.info(cabeceralog+"Criterio NO cumplido");
								}							
							}
						}				
					}			
				} 
			}

			return result;
		}
		
		return result;
	}
	
	public void detener()
	{
		super.detener();
		
		if (indicadores!=null && indicadores.size()>0)
		{
			Hashtable<String,IndicadorProxy> listaIndicadores = this.getIndicadores().get(this.getAnalisis());
			Enumeration<String> enumIndicadores = listaIndicadores.keys();
			//Recorremos todos los indicadores lanzados para este Analisis
			while(enumIndicadores.hasMoreElements())
			{
				String nombreIndicador = (String)enumIndicadores.nextElement();
				//Paramos los indicadores
				if (listaIndicadores.get(nombreIndicador).ejecutando())
				{
					//Forzamos su parada por haber superado el tiempo maximo de ejeucion
					listaIndicadores.get(nombreIndicador).detener();
					log.info(cabeceralog+"Parado el indicador:" +nombreIndicador+ " por sobrepasar el tiempo maximo de ejecucion el criterio: " + this.getCriterio().getNombre() + tiempo_max);
				}
			}
		}
	}
	
	public boolean validar() {
		boolean valido = true;
		return valido;
	}
	
	
	
	private void extraerCondiciones () {
		// Recorre el texto de la condicion indicada, identificando por niveles el texto comprendido entre parentesis. Cada trozo de texto comprendido entre parentesis del mismo nivel, será una condicion
			int nivel = 0;
			String txCondicion;		
			Integer indCond;
			
			ArrayList<Integer> abiertos = new ArrayList<Integer>(); // Almacena las posiciones en el texto en la que se encuntra el '(' que abre la condicion actual. El índice se corresponderá con el nivel
			
			CondicionMultiple multiple;		
			
			char c;
			for (int i = 0;i<this.getCriterio().getEvaluacion().length();i++) {
				c = this.getCriterio().getEvaluacion().charAt(i);
				if (c == abre) {
					// Por cada '(' se guarda en el array la posicion en la que se encuentra en el nivel actual
					abiertos.add(nivel, new Integer(i));
					nivel++;
				} else {
					if (c == cierra) {					
						// Cuando se encuentra un ')' se extre la condicion desde el inicial guardado en el array hasta la posicion en la que se cierra el nivel
						txCondicion = this.getCriterio().getEvaluacion().substring(abiertos.get(nivel-1).intValue(), i + 1);
						// El texto obtenido puede ser una valor, operación ente parentesis, o una condicion. Si es valor, debe ignorarse, si es condicion debe tratarse.
						if (esCondicion(txCondicion, operadorAnterior(this.getCriterio().getEvaluacion(), abiertos.get(nivel-1).intValue() ))) {
							multiple = new CondicionMultiple();
							multiple.setIdCondicion(indCondicion);
							multiple.setNivel(nivel-1);
							multiple.setTexto(txCondicion);	
							if ((abiertos.get(nivel-1).intValue() - 4) >= 0) {				
								if (this.getCriterio().getEvaluacion().substring(abiertos.get(nivel-1).intValue() - 4, i + 1).startsWith("NOT")) {
									multiple.setNegacion(true);	
								}
							}						
							indCond = indCondicion;
							evaluacion.add(indCond, multiple);		
							indCondicion++;
							infoCondicionMultiple(indCond);																
						}
						abiertos.remove(nivel - 1);	
						nivel--;
					}					
				}				
			}		
		}
	
	private void infoCondicionMultiple(Integer indCond) {
		// Se completa la informacion de la condicion recien creada, creando sus condiciones hijas y relacionandolas con ellas
			CondicionMultiple madre;
			CondicionMultiple hija;
			ArrayList<String> simples = new ArrayList<String>();
			String simple;
			String texto;
			String litOperador = "";
			int iAnd;
			int iOr;		
			madre = evaluacion.get(indCond);
			int nivel = madre.getNivel();
			// Las condiciones del nivel superior que no tengan madre seran hijas de esta condicion 
			for (int i = 0; i<evaluacion.size();i++) {
				hija = evaluacion.get(i);			
				if (hija.getMadre() == null && hija.getNivel() == (madre.getNivel() + 1) && hija.getIdCondicion() != madre.getIdCondicion()) {
					hija.setMadre(indCond);
					evaluacion.set(i,  hija);
				}			
			}
			// Las partes del texto de la condicion que estan entre parentesis se eliminan del texto de la condicion pues, por ser de un nivel superior, ya se habran tratado.
			texto = eliminarCondicionesInteriores(madre.getTexto());
			// Se obtienen el numero de operadores AND y OR que tiene la condicion.
			iAnd = contarCaracter(texto, opLogicos[0]);
			iOr = contarCaracter(texto, opLogicos[1]);
			// Si la condicion contiene operadores de un sólo tipo, se le asigna el tipo. Si contiene los dos operadores, se tratará aparte		
			if (iAnd > 0 && iOr > 0) {
				analizarCondicion(indCond);			
			} else{
				if ((iAnd > 0 && iOr == 0) || (iAnd == 0 && iOr > 0)) {
					if (iAnd > 0) {
						madre.setTipo("AND");	
						litOperador = opLogicos[0];
					} else {
						if (iOr > 0 ) {
							madre.setTipo("OR");
							litOperador = opLogicos[1];
						} 
					}	
				}
			}
		// Para obtener las condiciones simples de la condicion. Si no tiene ninguno de los dos operadores, seria una condicion simple. Si tiene varios operadores, se utilizara el 
			// operador logico para separar cada una de las condiciones simples.
			if ((iAnd > 0 && iOr == 0) || (iAnd == 0 && iOr > 0)) {
				simples = condicionesSimples(texto, litOperador);
			}
			if ((iAnd > 0 && iOr == 0) || (iAnd == 0 && iOr > 0)) {	
				for (int i = 0; i< simples.size(); i++) {		
					simple = simples.get(i);
					hija = new CondicionMultiple();				
					hija.setIdCondicion(indCondicion);
					hija.setNivel(nivel+1);
					hija.setTexto(formatoCondicion(simple));	
					hija.setMadre(madre.getIdCondicion());
					hija.setCondicion(operadoresCondicion(simple));
					evaluacion.add(indCondicion, hija);				
					indCondicion++;			
				}	
			} else {
				if  (iAnd == 0 && iOr == 0) {
					madre.setCondicion(operadoresCondicion(texto));
					evaluacion.set(madre.getIdCondicion(), madre);
				}
			}
		}
	
	private Condicion operadoresCondicion(String txCondicion) {
	// A partir de un texto que contiene una condicion simple, obtiene los operandos y operador y crea el objeto condicion que devuelve como resultado
		Condicion nuevaCond = null;
		
		int i = 0;
		String operador = "";
		// Obtiene el operador utilizado en la condicion
		i = 0;
		while (i < operadores.length) {
			if (txCondicion.indexOf(operadores[i]) >= 0) {
				operador = operadores[i];
				i = operadores.length + 4;
			}
			i++;
		}		
		// Si se ha encontrado el operador, divide la condicion en tres partes: operando1, operador y operando2.
		if (operador != "") {
			String[] minimas = txCondicion.split(operador);			
				nuevaCond = new Condicion();	
				Operando oper1 =nuevoOperando(aislarOperando(minimas[0].trim()));
				nuevaCond.setOperando1(oper1);
				if (minimas.length == 2) {
					Operando oper2 = nuevoOperando(aislarOperando(minimas[1].trim()));				
					nuevaCond.setOperando2(oper2);			
				}			
				nuevaCond.setOperador(operador);
			//	nuevoError ("La condicion es errónea. Falta un operador: " + txCondicion); 
								
		} else {
			nuevaCond = new Condicion();				
			Operando oper1 = nuevoOperando(aislarOperando(txCondicion));	
			nuevaCond.setOperando1(oper1);
			Operando oper2 = nuevoOperando("true");
			nuevaCond.setOperador("=");
			nuevaCond.setOperando2(oper2);
		//	nuevoError ("La condicion es errónea. No se ha definido ningún operador: " + txCondicion); 
		}
		return nuevaCond;
	}
	
	private Operando nuevoOperando(String nombre) {
	// Crea el operando de una condicion simple. 
		int tipo;		
		Operando oper = new Operando();			
		// El operando puede ser de tres tipos: Indicador, valor o fórmula. Éste último aún no se ha codificado, por lo que se identifica como erróneo.	
		oper.setNombre(nombre);		
		tipo = esIndicador(oper.getNombre());
		if (tipo == Comunes.tpIndicador) 
		{
			oper.setTipo(Comunes.tpIndicador);
			oper.setTipoValor(Comunes.tpVlIndicador);
		} 
		else 
		{
			if (tipo == Comunes.tpIndErroneo) {
				oper.setTipo(Comunes.tpIndErroneo);
				oper.setTipoValor(Comunes.tpVlNoTipo);				
			} else {			
				tipo = esValor(nombre);
				// Si es de tipo valor, guarda el tipo de valor que es.
				if (tipo != Comunes.tpVlNoTipo) {
					oper.setTipo(Comunes.tpValor);
					oper.setTipoValor(tipo);				
				} 
			}
		}
		return oper;
	}
	
	private int esIndicador (String operando) {
	// Comprueba si la cadena recibida como parámetro corresponde a alguno de los indicadores definidios en el análisis. Devuelve  -1, si es erroneo, 0 si no lo es, y 1 si es indicador.
		int esIndica = Comunes.tpNoIndicador;
		
		if (operando.startsWith(Comunes.tpMarcaIndicador))
		{	
			esIndica = Comunes.tpIndErroneo;
			
			String[] tramos = new String [] {};
			tramos = operando.split("\\.");
			String nombreIndicador = tramos[0].substring(1);
			
			if (getIndicadorNombre(this.getAnalisis(),nombreIndicador)!=null)
			{
				esIndica = Comunes.tpIndicador;
			}	
		}
		else
			esIndica = Comunes.tpNoIndicador;
		
		return esIndica;		
	}
	
	private int esValor(String operando) {
	// Comprueba si la cadena recibida como parametro corresponde a un valor: Los valores pueden ser logicos, de cadena, numericos o de fecha.
		int valor = Comunes.tpVlNoTipo;
		String oper = operando; 
		
		if (operando.toUpperCase().equals(Comunes.verdadero) || operando.toUpperCase().equals(Comunes.falso)) {
			valor = Comunes.tpVlBoolean;
		} else {
			if (operando.startsWith(Comunes.comillas) && operando.endsWith(Comunes.comillas) && contarCaracter(operando, Comunes.comillas) == 2) {
				if (esFecha(operando)) {
					valor = Comunes.tpVlDate;
				} else {
					valor = Comunes.tpVlString;
				}
			} else {
				if (contarCaracter(operando, Comunes.comillas) == 0) {
					oper = oper.replaceAll("\\,", "\\.");
					try {
						Integer.parseInt(oper);
						valor = Comunes.tpVlInt;
					} catch (NumberFormatException e3) {
						valor = Comunes.tpVlNoTipo;
						//nuevoError("El operando contiene un valor numérico erróneo: " + operando);
					}					
				}
			}
		}		
		return valor;
	}
	
	private boolean esFecha (String fecha) {
	// Comprueba si la cadena recibida corresponde a una fecha		
		boolean vale = false;
		String formatosFecha [] = new String [] {"dd/MM/yyyy","dd-MM-yyyy", "MM/dd/yyyy", "MM-dd-yyyy", "yyyy/MM/dd", "yyyy-MM-dd", "dd/MM/yy","dd-MM-yy", "MM/dd/yy", "MM-dd-yy", "yy/MM/dd", "yy-MM-dd"};
		String formatosHora [] = new String [] {"hh:mm:ss","hh:mm", "HH:mm:ss", "HH:mm"};
		String txFecha = fecha.replaceAll(Comunes.comillas, "");
		Date fecParse;
		SimpleDateFormat formato;
		String txFormato;
		
		int f = 0;
		int h;
		while (f < formatosFecha.length && vale == false) {
			h = 0;
			while (h < formatosHora.length && vale == false) {
				if (txFecha.indexOf(" ") >= 0) {
					txFormato = formatosFecha[f] + " " + formatosHora[h];					
				} else {
					txFormato = formatosHora[h];	
				}
				formato= new SimpleDateFormat(txFormato);
		        try {		        	
		            fecParse = formato.parse(txFecha);           		            
		            f = formatosFecha.length + 5;
		            h = formatosHora.length + 5;
		            vale = true;	
		        } catch (ParseException ef) {
		        	h++;
		        }					
			}
			if (!vale) {
				txFormato= formatosFecha[f];		
				formato= new SimpleDateFormat(txFormato);
        		try {
        			fecParse = formato.parse(txFecha);        			        			
        			f = formatosFecha.length + 5;
        			vale = true;
        		} catch (ParseException eh) {
        			f++;
        		}	        		
	        }  
		}
		return vale;		
	}
	
	private void analizarCondicion(Integer indCond) {
		// La condicion compuesta contiene condiciones simples entre las que hay relaciones AND y OR. Al estar al mismo nivel de agrupacion, en este caso, se da prioridad a la relacion OR,
		// creando una relacion OR entre las condiciones que estan asi relacionadas entre ellas, que se relacionaran como AND con el resto de condiciones.
			
			CondicionMultiple madre;
			CondicionMultiple hija;
			CondicionMultiple nieta;
			String texto;
//			String[] partes = new String [] {};
//			String[] simples = new String [] {};		
			Integer condHija; 
			ArrayList<String> partes = new ArrayList<String>();
			ArrayList<String> simples = new ArrayList<String>();
			String simple;
			String parte;
			
			madre = evaluacion.get(indCond);
			madre.setTipo(opLogicos[0].trim());
			evaluacion.set(indCond, madre);		
			// Las partes del texto de la condicion que estan entre parentesis se eliminan del texto de la condicion pues, por ser de un nivel superior, ya se habran tratado.
			texto = eliminarCondicionesInteriores(madre.getTexto());
			// La condicion se decompone en condiciones o grupos de condiciones que tendran una relacion AND.
//			partes = texto.split(opLogicos[0]);
			partes = condicionesSimples(texto, opLogicos[0]);	
			for (int i = 0; i< partes.size(); i++) {
				parte = partes.get(i);
				hija = new CondicionMultiple();
				hija.setIdCondicion(indCondicion);
				condHija = indCondicion;
				indCondicion++;					
				hija.setNivel(madre.getNivel()+1);
				hija.setTexto(formatoCondicion(parte));		
				hija.setMadre(madre.getIdCondicion());			
			
				// Si la condicion contiene un OR, se descompone en las condiciones que la forman para relacionarlas con OR
				if (parte.indexOf(opLogicos[1]) >= 0) {
					hija.setTipo("OR");
//					simples = parte.split(opLogicos[1]);
					simples = condicionesSimples(parte, opLogicos[1]);
					for (int j= 0; j< simples.size();j++) {
						simple = simples.get(j);
						nieta = new CondicionMultiple();
						nieta.setIdCondicion(indCondicion);
						nieta.setNivel(madre.getNivel()+2);
						nieta.setTexto(formatoCondicion(simple));	
						nieta.setMadre(condHija);	
						nieta.setCondicion(operadoresCondicion(simple));					
						evaluacion.add(indCondicion, nieta);
						indCondicion++;							
					}
				} else {
					hija.setCondicion(operadoresCondicion(partes.get(0)));				
				}			
				evaluacion.add(condHija, hija);			
			}	
		}
	
	private ArrayList<String>  condicionesSimples(String compuesta, String operador ) {
	// Obtiene las condiciones simples que forman una compuesta eliminando los parentesis sin abrir o cerrar que pueda contener la compuesta .
		ArrayList<String> simples = new ArrayList<String>();
		String condicion;
		int indice = 0;
		int desde = 0;
		int hasta = compuesta.indexOf(operador);
		int abiertos;
		int cerrados;
		while (hasta >= 0 && hasta < compuesta.length()) {			
			condicion = compuesta.substring(desde, hasta);
			abiertos = contarCaracter(condicion, "(");
			cerrados = contarCaracter(condicion, ")");	
			if (abiertos != cerrados) {
				if ((abiertos - cerrados) == 1 && condicion.startsWith("(")) {
					condicion = condicion.substring(1);
					abiertos = contarCaracter(condicion, "(");
				}
				if ((cerrados - abiertos) == 1 && condicion.endsWith(")")) {
					condicion = condicion.substring(0, condicion.length()-1);
					cerrados = contarCaracter(condicion, ")");
				}				
			}			
			if (abiertos == cerrados) {
				simples.add(indice, condicion);		
				desde = hasta + operador.trim().length() + 1;
				indice++;
				hasta = compuesta.indexOf(operador, desde);			
			}	else {
				hasta = compuesta.indexOf(operador, hasta + operador.trim().length() + 1);
			}			
		}
		condicion = compuesta.substring(desde);
		abiertos = contarCaracter(condicion, "(");
		cerrados = contarCaracter(condicion, ")");	
		if (abiertos != cerrados) {
			if ((abiertos - cerrados) == 1 && condicion.startsWith("(")) {
				condicion = condicion.substring(1);				
			}
			if ((cerrados - abiertos) == 1 && condicion.endsWith(")")) {
				condicion = condicion.substring(0, condicion.length()-1);				
			}				
		}
		simples.add(indice, condicion);
		return simples;
	}
	
	private String eliminarCondicionesInteriores(String texto) {
	// Elimina del texto de entrada las partes de texto contenidas entre parentesis, manteniendo los parentesis de inicio y fin del texto. Esas condiciones interiores se habran definido en niveles superiores
		String textoFinal;
		textoFinal = texto;
		int iAbre = 0;
		int i;
		boolean cambio;
		char c;
		cambio = true;
		while (cambio == true) {
			cambio = false;
			i = 1;
			while (i<(textoFinal.length() - 1) && cambio == false) {				
				c = textoFinal.charAt(i);
				if (c == abre) {
					iAbre = i;
				} else {
					if (c == cierra) {						
						if (!esCondicion(textoFinal.substring(iAbre, i + 1), operadorAnterior(textoFinal, iAbre))) {
							textoFinal = cambiarCaracter (textoFinal, iAbre, '[');
							textoFinal = cambiarCaracter (textoFinal, i, ']');
						} else {
							if ((iAbre - 4) >= 0) {							
								if (textoFinal.substring(iAbre - 4, i + 1).startsWith("NOT")) {							
									iAbre = iAbre - 4;
								}
							}
							textoFinal = textoFinal.replace(textoFinal.substring(iAbre, i + 1), " ");
						}												
						cambio = true;
					}					
				}
				i++;				
			}	
		}
		textoFinal = limpiaCondicion(textoFinal);
		return textoFinal;
	}
	
	private String limpiaCondicion (String texto) {
		String textoLimpio = texto;
		// Se corrige la repetición de operadores lógicos resultante de le eliminación del texto entre parentesis que había entre ellos. 
		
		textoLimpio = remplazarCadena(textoLimpio, "  ", " ");	
		textoLimpio = remplazarCadena(textoLimpio, " AND AND ", " AND ");
		textoLimpio = remplazarCadena(textoLimpio, " OR OR ", " OR ");
		textoLimpio = textoLimpio.replaceAll(" AND \\)", "\\)");
		textoLimpio = textoLimpio.replaceAll("\\( AND ", "\\(");		
		textoLimpio = textoLimpio.replaceAll(" OR \\)", "\\)");	
		textoLimpio = textoLimpio.replaceAll("\\( OR ", "\\(");	
		
		textoLimpio = textoLimpio.replaceAll("\\[", "\\(");	
		textoLimpio = textoLimpio.replaceAll("\\]", "\\)");
		textoLimpio = textoLimpio.replaceAll("\\( ", "\\(");		
		textoLimpio = textoLimpio.replaceAll(" \\)", "\\)");		
		textoLimpio = remplazarCadena(textoLimpio, "  ", " ");	
		
		if (textoLimpio.indexOf(" AND OR ") > 0 || textoLimpio.indexOf(" OR AND ") > 0) {
			//nuevoError("La relacion lógica en la condicion no es correcta: " + textoLimpio);
		}		
		return textoLimpio;
	}
	
	private String formatoCondicion(String cadena) {
		String cadenaFormato;		
		cadenaFormato = cadena.trim();
		// La condicion completa debe estar entre parentesis, asi se asegura que el nivel minimo tenga una sola condicion, que es la completa.. 
		if (!entreParentesis(cadena) ) {
			cadenaFormato = "(" + cadenaFormato + ")";
		}
		// Se añade un espacio delante y detrás de cada uno de los operadores.
		for (int i = 0; i< operadores.length;i++) {
			cadenaFormato = cadenaFormato.replaceAll(operadores[i].toLowerCase(), " " + operadores[i] + " ");			
		}		
		// Se elimina el espacio que se habría añadido en el bucle anterioren los operadores que se definen con dos caracteres
		cadenaFormato = cadenaFormato.replaceAll("< =", "<=");
		cadenaFormato = cadenaFormato.replaceAll("<  =", "<=");	
		cadenaFormato = cadenaFormato.replaceAll("> =", ">=");		
		cadenaFormato = cadenaFormato.replaceAll(">  =", ">=");		
		cadenaFormato = cadenaFormato.replaceAll("< >", "<>");
		cadenaFormato = cadenaFormato.replaceAll("<  >", "<>");
		cadenaFormato = cadenaFormato.replaceAll("! =", "<>");
		cadenaFormato = cadenaFormato.replaceAll("!  =", "<>");
		cadenaFormato = cadenaFormato.replaceAll("!", " NOT ");		
		// Convierte a mayúsculas todos los operadores AND y OR
		for (int i = 0; i< opLogicos.length;i++) {
			cadenaFormato = cadenaFormato.replaceAll(opLogicos[i].toLowerCase(), opLogicos[i]);
		}		
		cadenaFormato = cadenaFormato.replaceAll(" not ", " NOT ");
		cadenaFormato = cadenaFormato.replaceAll("\\(not\\(", "\\( NOT \\(");
		cadenaFormato = cadenaFormato.replaceAll("\\(NOT\\(", "\\( NOT \\(");
		cadenaFormato = cadenaFormato.replaceAll("\\( not\\( ", "( NOT \\(");
		cadenaFormato = cadenaFormato.replaceAll("\\( NOT\\( ", "\\( NOT \\(");
		cadenaFormato = cadenaFormato.replaceAll("\\(not \\( ", "( NOT \\(");
		cadenaFormato = cadenaFormato.replaceAll("\\ NOT \\( ", "\\( NOT \\(");
		// Se eliminan los espacios de más	
		cadenaFormato = cadenaFormato.replaceAll("\\(", " \\(");
		cadenaFormato = cadenaFormato.replaceAll("\\) ", "\\) ");
		cadenaFormato = remplazarCadena(cadenaFormato, "  ", " ");	
		cadenaFormato = cadenaFormato.replaceAll("\\( ", "\\(");
		cadenaFormato = cadenaFormato.replaceAll(" \\)", "\\)");
		cadenaFormato = cadenaFormato.replaceAll("\\'", Comunes.comillas);
		return cadenaFormato;
	}
	
	private boolean entreParentesis(String cadena) {
		// Comprueba si la condicion completa contenida en el parametro cadena esta indicada entre parentesis. 
		// Estara entre parentesis si la primera y ultima posicion de la cadena contienen, respectivamente '(' y ')' y el parentesis de la ultima posicion cierra el de la primera
			boolean entre = false;
			int nivel = 0;
		
			ArrayList<Integer> abiertos = new ArrayList<Integer>(); // Almacena las posiciones en el texto en la que se encuntra el '(' que abre la condicion actual. El indice se correspondera con el nivel
			cadena = cadena.trim();
			char c;
			for (int i = 0;i<this.getCriterio().getEvaluacion().length();i++) {
				c = this.getCriterio().getEvaluacion().charAt(i);
				if (c == abre) {
					// Por cada '(' se guarda en el array la posicion en la que se encuentra en el nivel actual
					abiertos.add(nivel, new Integer(i));
					nivel++;
				} else {
					if (c == cierra) {		
						// Comprueba si ese ')' cerrara el nivel 0, si la condicion empieza por '(' y el nivel que se va a cerrar se abrio con un '(' en la primera posicion 
						// y si la posicion del ')' es la ultima de la cadena
						if ((nivel - 1) == 0 && cadena.startsWith("(")  && (abiertos.get(nivel - 1) == 0) && (i == (cadena.length() - 1))) {
							entre = true;
						}
						abiertos.remove(nivel - 1);	
						nivel--;
					}					
				}				
			}			
			return entre;
		}
		
	private static int contarCaracter(String cadena, String car) {
	// Cuenta el numero de veces que el caracter aparece en la cadena
        int pos = 0;
        int cont = 0;
        pos = cadena.indexOf(car);
        while (pos != -1) {
            cont++;                       
            pos = cadena.indexOf(car, pos + 1);
        }
        return cont;
	}

	private String aislarOperando(String operando) {
	// Elimina los parentesis al inicio y al final de la cadena
		String oper = operando;
		
		int abiertos = contarCaracter(oper, "(");
		int cerrados = contarCaracter(oper, ")");		
		if (abiertos != cerrados) {
			if (oper.startsWith("(") && !(oper.endsWith(")"))) {
				oper = oper.replaceAll("\\(", "");
			} else {
				if (oper.endsWith(")") && !(oper.startsWith("("))) {
					oper = oper.replaceAll("\\)", "");
				} 
			}
		} else {
			if (abiertos == 1 && cerrados == 1 && oper.startsWith("(") && oper.endsWith(")") ) {
				oper = oper.replaceAll("\\(", "");
				oper = oper.replaceAll("\\)", "");					
			}
		}		
		return oper;
	}
	
	private String remplazarCadena(String cadena, String busca, String cambia) {
		// Sustituye en una cadena la cadena de busqueda por la que que se cambia.
		// Utiliza un bucle con replaceAll para, por ejemplo, los casos en los que se quiere cambiar una serie de espacios seguidos por un solo.
			String remplazo = cadena;
			while (remplazo.indexOf(busca) > 0) {		
				remplazo = remplazo.replaceAll(busca, cambia);
			}	
//				remplazo = remplazarCadena(remplazo, "  ", " ");
			return remplazo;
	}
	
	private String operadorAnterior(String texto, int pos) {
		// Devuelve el texto anterior entre espacios en un texto situado delante de la posicion indicada.
			String opAnt = " ";
			char c;
			int i = pos - 1;
			c = texto.charAt(i);
			boolean espacio = false;
			
			while (i >=0) {			
				c = texto.charAt(i);
				if (c == ' ') 	{ 
					if (!espacio) {
						espacio = true;
						opAnt = c + opAnt;
					} else {
						i = -1;							
					}				
				} else {
					opAnt = c + opAnt;
				}
				i--;
			}
					
			return opAnt.trim();
		}
	
	private boolean esCondicion(String texto, String previo) {
	// Identifica si es una condicion o un valor, para saber como debe tratarse. Si el texto contiene algun operador, sera una condicion.
		boolean esCond = true;
		
		for (int i = 0; i < operadores.length; i++) {
			if (previo.equals(operadores[i])) {
				esCond = false;
			}
		}				
		if (esCond) {
			esCond = false;
			for (int i = 0; i < operadores.length; i++) {
				if (texto.indexOf(operadores[i]) >= 0) {
					esCond = true;
				}
			}			
		}
		return esCond;
	}
	
	private String cambiarCaracter (String cadena, int pos, char car) {
	// Sustituye en una cadena el caracter situado en una posicion por el indicado.
		String cambiada;
		cambiada = cadena.substring(0, pos) + car + cadena.substring(pos+1);
		return cambiada;		
	}
	
	//Deberian ser opoeraciones realizadas en la validacion de un analisis NO en su ejecucion
	private boolean validarCondicion() {
		// Comprueba la sintaxis de la condicion
		boolean result = new Boolean(true);
		
		if (this.getCriterio().getEvaluacion().length() == 0 || this.getCriterio().getEvaluacion() == null) {
			//this.setResultado("No se ha definido la condicion");
			result = false;
		} else {
			this.getCriterio().setEvaluacion(formatoCondicion(this.getCriterio().getEvaluacion()));	
			int iAbre = contarCaracter(this.getCriterio().getEvaluacion(), "(");
			int iCierra = contarCaracter(this.getCriterio().getEvaluacion(), ")");			
		
			// En la condicion, el numero de "(" debe ser igual al de ")".
			if (iAbre != iCierra) 	{
				result = false;	
				if (iAbre > iCierra) {
					//this.setResultado("Falta parentesis )");
				} else {
					//this.setResultado("Falta parentesis (");
				}					
			}		
		}
		return result;
	}
	
/*	private boolean comprobarCondiciones() {
		// Recorre la lista de condiciones encontradas en la consulta completa para determinar si la información obtenida es correcta y se puede evaluar la condicion compleja
			boolean result = true;		
			CondicionMultiple multiple;		
			
			for (int m = 0; m < evaluacion.size(); m++) {
				multiple = evaluacion.get(m);
				if (multiple.getCondicion() != null) {
					Condicion simple = multiple.getCondicion();
					boolean op1 = comprobarOperando(simple.getOperando1());
					boolean op2 = comprobarOperando(simple.getOperando2());		
					if (op1 == false || op2 == false) {
						result = false;
					}								
				}
			}		
			return result;
		}*/
	
/*	private boolean comprobarOperando(Operando oper) {
		// Comprueba si la información obtenida para el valor del opernado es completa y coherente.
			boolean comprobado = true;
			if (oper.getTipo() == Comunes.tpIndErroneo) {
				comprobado = false;
				nuevoError("No se ha encontrado el indicador " + oper.getNombre());
			} 
			if (oper.getTipo() == Comunes.tpNoIndicador && oper.getTipoValor() == Comunes.tpVlNoTipo) {
				comprobado = false;
				nuevoError("No se ha podido identificar el indicador " + oper.getNombre());
			}
			if (oper.getTipo() == Comunes.tpIndicador && oper.getTipoValor() != Comunes.tpVlIndicador) {
				comprobado = false;
				nuevoError("El tipo de indicador y el tipo del resultado del operando no coinciden: " + oper.getNombre());
			}
			if (oper.getTipo() == Comunes.tpValor && !(oper.getTipoValor() != Comunes.tpVlBoolean || oper.getTipoValor() != Comunes.tpVlString || oper.getTipoValor() != Comunes.tpVlInt || oper.getTipoValor() != Comunes.tpVlDate)) {
				comprobado = false;
				nuevoError("No se ha podido identificado el tipo de resultado del operando: " + oper.getNombre());
			}		
			return comprobado;
		}*/
	
/*	private void nuevoError(String txError) {
		// Anhade el nuevo error detectado a la lista de errores de la evaluacion		
			int i = errores.size();
			errores.add(i, txError);  ;
		}*/
	
	public boolean evaluaCondicionMultiple (CondicionMultiple condicion) {
		boolean result = true;
		CondicionMultiple hija;
		
		if (condicion.getEvaluada()) {	return condicion.getResultado();		}	
		if (condicion.getTipo() != null ) {
			if (condicion.getTipo().equalsIgnoreCase("AND"))			{	result = true;		}
			else if (condicion.getTipo().equalsIgnoreCase("OR")) 		{	result = false;		}
		}
		
		if (condicion.getCondicion() == null) {
			if (condicion.getTipo() != null ) {
				for (int i = 0; i < evaluacion.size(); i++) { 
					hija = evaluacion.get(i);
					if (hija.getMadre() == condicion.getIdCondicion())	{					
						if (condicion.getTipo().equalsIgnoreCase("AND") && !hija.getResultado() ) {
							result = false;
						}
						if (condicion.getTipo().equalsIgnoreCase("OR") && hija.getResultado()) {
							result = true;
						}					
					}			
				}		
			} else {
				result = evaluaCondicionSimple(condicion.getCondicion());
			}
		} else {
			result = evaluaCondicionSimple(condicion.getCondicion());
		}		
		if (condicion.getNegacion()) {
			result = !result;
		}
		condicion.setResultado(result);
		condicion.setEvaluada(true);		
		return result;	
	}
	
	public boolean evaluaCondicionSimple(Condicion simple) {
	boolean result = false;	
	String oper1;
	String oper2;
	int iOper1 = 0;
	int iOper2 = 0;
	boolean numerica;
	int compara;
	
		if (simple.getEvaluada()) {return simple.getResultado();  } 
		else {
			
			// Recupera los resultados de cada uno de los operandos de la condicion.
			//oper1 = simple.getOperando1().getResultado().toString().trim();
			boolean interpretarOp1 = interpretarOperando(simple.getOperando1());
			boolean interpretarOp2 = interpretarOperando(simple.getOperando2());
			
			if (interpretarOp1 && interpretarOp2)
			{			
				oper1 = simple.getOperando1().getResultado().toString().trim();
				oper2 = simple.getOperando2().getResultado().toString().trim();
				// Convierte ambos resultados a int para decidir si la comparacion sera numerica o alfabetica.
				numerica = true;
				try {
					iOper1 = Integer.parseInt(oper1);				
				} catch (NumberFormatException e3) {				
					numerica = false;
				}	
				try {
					iOper2 = Integer.parseInt(oper2);				
				} catch (NumberFormatException e3) {				
					numerica = false;
				}
				// Si ha podido convertir a int ambos resultados, los comparará numéricamente. Si no, hará comparación alfabéticamente.
				String operadores [] = new String[] {"=", "<", ">", ">=", "<=", "<>"};
				int op = 0;
				for (op = 0; op < operadores.length; op++) {
					if (simple.getOperador().equals(operadores[op])) {
						break;
					}
				}			
				
				if (numerica) {			
					switch (op) {
					case 0:
						if (iOper1 == iOper2) 	{	result = true;break;		}
					case 1:
						if (iOper1 < iOper2) 	{	result = true;break;		}
					case 2:
						if (iOper1 > iOper2)	{	result = true;break;		}
					case 3:
						if (iOper1 >= iOper2) 	{	result = true;break;		}
					case 4:
						if (iOper1 <= iOper2) 	{	result = true;break;		}
					case 5:	
						if (iOper1 != iOper2) 	{	result = true;break;		}
					}		
				} else {
					
					compara = oper1.compareToIgnoreCase(oper2);
					if (compara < 0 && (simple.getOperador().equals("<") || simple.getOperador().equals("<=") || simple.getOperador().equals("<>"))) {
						result = true; 
					} else {
						if (compara == 0 && (simple.getOperador().equals("=") || simple.getOperador().equals("<=") || simple.getOperador().equals(">="))) {
							result = true;
						} else {
							if (compara > 0 && (simple.getOperador().equals(">") || simple.getOperador().equals(">=") || simple.getOperador().equals("<>"))) {
								result = true;
							}
						}
					}					
				}			
				simple.setEvaluada(true);
			}
			else
				simple.setEvaluada(false);
		}
		if (simple.getNegacion()) 	{result = !result;	}
		simple.setResultado(result);
		return result;
	}
	
	private String camposIndicador(String texto, String clave)
	{
		String valor = null;
		
		String[] tramos = texto.split(Comunes.tpSeparador);
		if (tramos!=null)
		{
			if (clave.equals("Nombre"))
				valor = tramos[0].substring(1);
			else if (clave.equals("Campo"))
				valor = tramos[1];
		}
		else
			valor = texto;
		
		return valor;
	}
	
	
	private boolean interpretarOperando(Operando op)
	{
		boolean interpretado = false;
		
		//Ejecuta el operando. Si es un indicador, ejecutarÃ¡ el indicador. Si no, convertirÃ¡ el valor al tipo correspondiente.	
		if (!op.getEjecutado())
		{
			//Si es de tipo indicador
			if (op.getTipo() == Comunes.tpIndicador) 
			{
				String nombreIndicador = camposIndicador(op.getNombre(),"Nombre");
				String campoIndicador = camposIndicador(op.getNombre(),"Campo");
				
				//Recuperamos el indicador de la lista de indicadores
				IndicadorProxy indicador = getIndicadorNombre(this.getAnalisis(),nombreIndicador); 
				log.info(cabeceralog + " Interpretar el operando de tipo indicador: "+op.getNombre());
				
				//Lista para almacenar los indicadores linkados al indicador origen
				List<String> listaIndicadoreAsociado = null;
						
				//Si el indicador no ha sido interpretado
				if (indicador.noejecutado()) 
				{
					//Recupero la lista de Indicadores linkados al indicador origen
					listaIndicadoreAsociado = indicador.ObtenerIndicadorAsociado(null);
					listaIndicadoreAsociado.add(indicador.getIndicador().getNombre());
					IndicadorProxy indicadorAsociado = null;
					
					if (listaIndicadoreAsociado!=null && listaIndicadoreAsociado.size()>0)
					{	
						//Ejecutar hasta que el indicador origen haya sido interpretado
						while (!indicador.ejecutado())
						{					
							for (int i=0; i<listaIndicadoreAsociado.size(); i++)
							{
								String nombreIndicadorAsociado = listaIndicadoreAsociado.get(i);	
								
								//Recupero el indicador asociado
								indicadorAsociado = getIndicadorNombre(this.getAnalisis(),nombreIndicadorAsociado); 
								
								//Si el indicadorasociadlo no está ejecutado y no es depediente de ningun otro indicador puede ejecutarse
								if (indicadorAsociado!=null && !indicadorAsociado.ejecutado() && !indicadorAsociado.ejecutando() && !indicadorAsociado.esDependiente())
								{ 
									log.info(cabeceralog+" Interpretar el indicador asociado: "+indicadorAsociado.getIndicador().getNombre());
									
									//Recuperamos los valores de los parametros del indicador
									indicadorAsociado.parametrosIndicador();
									//Ejecuto el indicador
									if (numHilos<=minHilos)
										indicadorAsociado.ejecutar();
									else
										super.nuevoHilo(indicadorAsociado);
								}
								//Alguno de los indicadores dependientes se ha ejecutado pero sin resultado, damos por hecho que todos los indicadores dependientes son stoppers
								else if (indicadorAsociado!=null && indicadorAsociado.ejecutado() && !indicadorAsociado.getDescripcionEstado().equals(FIN_OK))
								{
									log.info(cabeceralog+" Interpretado el indicador asociado: "+indicadorAsociado.getIndicador().getNombre()+" sin resultados");
									
									indicador.setEstado(ESTADO_EJECUTADO);
									indicador.setDescripcionEstado(indicadorAsociado.getDescripcionEstado());
								}
							}
							
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						if (indicador.getDescripcionEstado().equals(FIN_OK))
						{
							log.info(cabeceralog+" Interpretado el operando: "+op.getNombre()+ " satisfactoriamente");
							
							Object valParam = new Object();
							if (campoIndicador.equals("ROWCOUNT"))
								valParam = indicador.getResultadoEjecucion()==null?-1:indicador.getResultadoEjecucion().size();
							else
							{
								if (indicador.getResultadoEjecucion()!=null && indicador.getResultadoEjecucion().size()>0)
								{
									Object[] linea = indicador.getResultadoEjecucion().elementAt(0);
									int c = 0;
									while (c < indicador.getIndicador().getResultado().length) 
									{
										if (campoIndicador.equals(indicador.getIndicador().getResultado()[c])) 
										{
											valParam = linea[c];
											break;
										}
										c++;
									}
								}
							}
							op.setResultado(valParam);
							interpretado = true;
						}
						else 
							interpretado = false;
					}		
				}
				else
				{
					if (indicador.getDescripcionEstado().equals(FIN_OK))
					{
						Object valParam = new Object();
						if (campoIndicador.equals("ROWCOUNT"))
							valParam = indicador.getResultadoEjecucion()==null?0:indicador.getResultadoEjecucion().size();
						else
						{
							if (indicador.getResultadoEjecucion()!=null && indicador.getResultadoEjecucion().size()>0)
							{
								Object[] linea = indicador.getResultadoEjecucion().elementAt(0);
								int c = 0;
								while (c < indicador.getIndicador().getResultado().length) {
									if (campoIndicador.equals(indicador.getIndicador().getResultado()[c])) {
										valParam = linea[c];
										break;
									}
									c++;
								}
							}
						}
						op.setResultado(valParam);
						interpretado = true;
					}
					else
						interpretado = false;
				}
			} 
			else 
			{
				log.info(cabeceralog + " Interpretar el operando de tipo valor: "+op.getNombre());
				
				if (op.getTipo() == Comunes.tpValor) {
					if (op.getTipoValor() == Comunes.tpVlBoolean) {
						op.setResultado(Boolean.parseBoolean(op.getNombre()));
						interpretado = true;
					} else {
						if (op.getTipoValor() == Comunes.tpVlString) {
							op.setResultado(op.getNombre());
							interpretado = true;
						} else {
							if (op.getTipoValor() == Comunes.tpVlInt) {
								op.setResultado(Integer.parseInt(op.getNombre()));
								interpretado = true;
							} 
						}
					}	
				}
				log.info(cabeceralog + " Interpretado el operando: "+op.getNombre() + " satisfactoriamente");
			}
		}
		
		return interpretado;
	}
}
