INSERT OVERWRITE LOCAL DIRECTORY '/home/xxx' ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' select * from london_smart.tablon where CODMES >= 201606 and CODMES <= 201707 and RUBRO_BCP in
 ('20100070970','20508352434','20115675843','20366131222','20109072177','20384891943');