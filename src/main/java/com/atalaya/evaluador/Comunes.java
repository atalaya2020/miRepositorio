package com.atalaya.evaluador;

public abstract class Comunes {
	
	public static final int tpIndErroneo = -1;
	public static final int tpNoIndicador = 0;
	public static final int tpIndicador	= 1;
	public static final String tpMarcaIndicador = "#";
	public static final String tpParametroGet = "$";
	public static final int tpValor	= 2;	
	public static final int tpVlNoTipo = 0;
	public static final int tpVlBoolean = 1;
	public static final int tpVlString = 2;
	public static final int tpVlInt = 3;
	public static final int tpVlDate = 4;
	public static final int tpVlIndicador = 5;
	public static final String verdadero = "TRUE";
	public static final String falso = "FALSE";
	public static final String comillas = "" + (char) 34;
	public static final String tpSeparador = "\\.";
}
