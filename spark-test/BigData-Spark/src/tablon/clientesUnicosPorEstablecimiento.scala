package tablon

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.log4j._

object clientesUnicosPorEstablecimiento {
  
  

def kpi_establecimientos_clientes_unicos(ruta: String) = {
   val sc = new SparkContext("local[*]", "clientesUnicosPorEstablecimiento")
	val rdd = sc.textFile(ruta)
	val r1 = rdd.map(r => r.split("\t")).map(r => (r(6),r(14)))
	r1.distinct().map(_._1).countByValue().foreach(println)
}
  
  
  def main(args: Array[String]) {

    kpi_establecimientos_clientes_unicos("tablon.tsv")
    
  }
  
  
}