# AIAD
java -cp %JADE_PATH%;[path das classes compiladas] jade.Boot -agents [agentes]


agente recurso:
- [nome]:[package].[nome do agente]([lista de exames possiveis])

exemplo:
	drHouse:agents.RecursoAgent(raio-x)

agente paciente:
- [nome]:[package].[nome do agente]([health],[isSequencial],[lista de exames a realizar])
	
exemplo:
	p1:agents.PacienteAgent(0.3,false,raio-x)
	
	
NOTA:
	- existem ja exames pre-definidos:
		- raio-x
		- ecografia
		- tac
		- colonoscopia
		- quimioterapia
		- engessar
		- cirurgia1
		- cirurgia2

	- para correr com o modo FIRST COME FIRST SERVED
		- por a true a variavel na classe utils.Utilities
	- no modo normal (prioridade por valor de funcao de utilidade)
		- por a false a variavel
