package gals;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Semantico implements Constants
{
    Stack<Integer> stack = new Stack();
    Map<String, Integer> vars = new HashMap<String, Integer>();
    String variavelAtual;
    
    public void executeAction(int action, Token token)	throws SemanticError
    {
        switch (action)
        { 
            case 1: //Valida_função_main
              // Verifica se a função é main
              // Se for, imprime mensagem
              // Se não for, lança erro semântico
              if (token.getLexeme().equals("main")) {
                System.out.println("Função main encontrada");
              } else {
                throw new SemanticError("Função não encontrada");
              }
              break;
              
            case 2: // verifica se a variavel existe
              // Se não existir, lança erro semântico
              if (!vars.containsKey(token.getLexeme())) {
                throw new SemanticError("Variável não existe");
              }
              // Se existir, empilha o valor da variável
              stack.push(vars.get(token.getLexeme()));
  
              break;
              
            case 3: // Valida se a variável já foi declarada
              if (vars.containsKey(token.getLexeme())) {
                throw new SemanticError("Variável já declarada");
              }
              // Cria uma nova entrada na tabela
              InsereTabela entrada = new InsereTabela(token.getTokenType(), token.getLexeme());
              vars.put(entrada.getNome(), entrada.getTipo());
              break;
           
            case 4: // valida o tipo da variável
            
    }	
  }

}
