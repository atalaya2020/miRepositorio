package com.atalaya;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.modelodatos.Analisis;


@Repository
public interface AnalisisMongoRepository extends MongoRepository<Analisis, String> {

	  public Analisis findByNombre(String name);
	  //public List<Analisis> findByDepartamento(String fuente);

}
