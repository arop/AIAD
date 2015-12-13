java -cp %JADE_PATH%;out/production/AIAD jade.Boot -agents drHouse:agents.RecursoAgent(raio-x);drHouse2:agents.RecursoAgent(tac);p1:agents.PacienteAgent(0.5,false,raio-x,tac);p2:agents.PacienteAgent(0.6,true,raio-x,tac);snif:jade.tools.sniffer.Sniffer -gui 


