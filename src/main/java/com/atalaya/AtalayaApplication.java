package com.atalaya;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atalaya.interpretes.AnalisisProxy;
import com.atalaya.interpretes.Ejecutable;
//import com.atalaya.evaluador.ParametroProxy;
import com.modelodatos.Analisis;
import com.modelodatos.Parametro;

@SpringBootApplication
@RestController
public class AtalayaApplication {
	
	@Autowired
	private AnalisisMongoRepository analisisrepo;
	
	public static void main(String[] args) {
		SpringApplication.run(AtalayaApplication.class, args);	
	}
	
	@GetMapping("/quiensoy")
	public String quiensoy() {
		
		return String.format(" %s!", "Soy un atalaya cualquiera....");
		
		
	}	
			
	@GetMapping("/ejecutaranalisis")
	public String consultaranalisis(@RequestParam Map<String, String> params) {
		ArrayList<Parametro> anaParams = new ArrayList<Parametro>(); 
		String nombreAnalisis = null;
		
		//Recuperamos los parametros recibidos en la peticion http
		Iterator<String> itParams = params.keySet().iterator();
		while (itParams.hasNext()) {
			String clave = itParams.next().toString();
			if (clave.equalsIgnoreCase("nombreAnalisis")) {
				nombreAnalisis = params.get(clave);
			} else {
				Parametro param = new Parametro();
				param.setNombre(clave);
				param.setTipo("String");
				param.setValor(params.get(clave));
				anaParams.add(param);
			}
		}	
		
		if (nombreAnalisis!=null)
		{
			//Recuperamos de bbdd el analisis a interpretar
			Analisis analisis = analisisrepo.findByNombre(nombreAnalisis);
			
			if (analisis!=null)
			{
				AnalisisProxy analisisproxy = new AnalisisProxy(analisis, anaParams);
				analisisproxy.ejecutar();
				
				return (analisisproxy.volcadoResultado(Ejecutable.VOLCADO_HTML));
			}
			else
				return String.format(" %s!", "NO Recuperado el analisis con nombre: "+ nombreAnalisis);
		}
		else
			return String.format(" %s!", "Debe informar el parametro con nombre: nombreAnalisis");
	}		
		
}