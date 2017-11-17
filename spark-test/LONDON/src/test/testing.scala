package test

import org.apache.spark._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._

object testing {

  val sc = new SparkContext("local[*]", "testing")
  val sqlContext = new org.apache.spark.sql.SQLContext(sc)

  def main(args: Array[String]) {

    val lista = List("100070934", "100070905")
    println("El primer valor de la lista es: " + lista(0))
    println("El segundo valor de la lista es: " + lista(1))

    val cabeceras = "Nombre Edad"
 
    val camposDF = cabeceras.split(" ").map(fieldName => StructField(fieldName, StringType, nullable = true))
    val schema = StructType(camposDF)
    
    val dataAlumnos = sc.parallelize(List(("Martin", 20), ("Iveth", 17), ("Jorge", 15)))
    //dataAlumnos.map()
    
    val rdd1 = dataAlumnos.map(r => Row(r._1,r._1))
    
    //val rddTablon = sc.textFile(ruta).map(r => r.split("\t")).map(r => Row(r(6), r(25), r(valor1(0)), r(valor2(0))))
    
    
    //val rddTablon = sc.parallelize(List(("Martin", 28), ("Iveth", 28), ("Jorge", 34))).map(r => (r._1,r._2)).map(r => Row(r(0), r(1))))
    
    //val dataAlumnos = sc.parallelize(Array(("Martin", 28), ("Iveth", 28), ("Jorge", 34)))
    
    //val df = sqlContext.createDataFrame(dataAlumnos, schema)
    
    //val df = sqlContext.createDataFrame(dataAlumnos, schema)
    
    println(lista.length)

  }

}