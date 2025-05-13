package gals;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import compil.SemanticTable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class Semantico {

    private static class Simbolo {
        String tipo;
        String nome;
        boolean iniciado;
        boolean usado;
        int escopo;
        boolean parametro;
        int pos_param;
        boolean vetor;
        boolean matriz;
        boolean ref;
        boolean funcao;
        boolean proc; // procedimento || funcao sem return || void
        boolean parametro_lido;
        int tipo_id; // Adicionando campo para armazenar o ID do tipo conforme SemanticTable

        // Initialize all fields with default values
        public Simbolo() {
            this.tipo = "";
            this.nome = "";
            this.iniciado = false;
            this.usado = false;
            this.escopo = 0;
            this.parametro = false;
            this.pos_param = 0;
            this.vetor = false;
            this.matriz = false;
            this.ref = false;
            this.funcao = false;
            this.proc = false;
            this.parametro_lido = false;
            this.tipo_id = -1; // Valor padrão para tipo indefinido
        }
    }

    private static List<Simbolo> lista_simbolos = new ArrayList<>();
    private static List<Integer> pilha_escopo = new ArrayList<>();
    private static List<Simbolo> lista_simb_aux = new ArrayList<>(); // essa lista serve pra marcar os simbolos como inicializados
    private static Stack<Integer> pilha_tipos = new Stack<>(); // Pilha para armazenar tipos das expressões
    private static String tipo_declaracao = "";
    private static int escopo_cont = 0;

    // Geração de código
    private static String ponto_data = ".data\n";
    private static String ponto_text = ".text\nJMP _main";
    private static String entrada_saida_dado = "";
    private static Stack<String> pilha_operador = new Stack<>();
    private static String operador_relacional = "";
    private static String recebe_atrib = "";
    private static int vetor_tamanho = 0;
    private static boolean escrever_text = false;
    private static boolean temp1 = false;
    private static boolean temp2 = false;
    private static boolean temp3 = false;
    private static boolean calculando_indice = false;
    private static boolean inicio_atribuicao = true;
    private static boolean entrando_no_indice = false;
    private static Stack<String> pilha_rotulo = new Stack<>();
    private static int rotulo_cont = 0;
    private static String parametro_aux = "";
    private static String chamada_nome = "";
    private static String retorno = "";
    private static int conta_parm = 0;

    // Converte uma string de tipo para o ID correspondente na SemanticTable
    private static int obterTipoId(String tipoStr) {
        switch (tipoStr.toLowerCase()) {
            case "int":
                return SemanticTable.INT;
            case "float":
                return SemanticTable.FLO;
            case "char":
                return SemanticTable.CHA;
            case "string":
                return SemanticTable.STR;
            case "bool":
            case "boolean":
                return SemanticTable.BOO;
            default:
                return -1; // tipo inválido
        }
    }

    // Converte ID de tipo para string representativa
    private static String obterTipoString(int tipoId) {
        switch (tipoId) {
            case SemanticTable.INT:
                return "int";
            case SemanticTable.FLO:
                return "float";
            case SemanticTable.CHA:
                return "char";
            case SemanticTable.STR:
                return "string";
            case SemanticTable.BOO:
                return "bool";
            default:
                return "tipo_desconhecido";
        }
    }

    // Obtém o tipo de operação baseado no operador
    private static int obterTipoOperacao(String operador) {
        switch (operador) {
            case "+":
                return SemanticTable.SUM;
            case "-":
                return SemanticTable.SUB;
            case "*":
                return SemanticTable.MUL;
            case "/":
                return SemanticTable.DIV;
            case ">":
            case "<":
            case ">=":
            case "<=":
            case "==":
            case "!=":
                return SemanticTable.REL;
            default:
                return -1; // operação inválida
        }
    }

    private static boolean verifica_escopo(int simb_escopo) {
        // Percorre o vetor procurando pelo valor
        for (int i = 0; i < pilha_escopo.size(); ++i) {
            if (pilha_escopo.get(i) == simb_escopo) {
                return true;
            }
        }
        return false;
    }

    private static boolean procura_simbolo(String nome) {
        // Iterando pela lista
        for (Simbolo s : lista_simbolos) {
            if (s.nome.equals(nome)) {
                if (verifica_escopo(s.escopo)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean procura_simbolo(String nome, boolean funcao) {
        // Iterando pela lista
        for (Simbolo s : lista_simbolos) {
            if (s.nome.equals(nome) && (s.funcao == funcao || s.proc == funcao)) {
                if (verifica_escopo(s.escopo)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Procura símbolo e retorna ele se encontrado
    private static Simbolo encontra_simbolo(String nome) {
        for (Simbolo s : lista_simbolos) {
            if (s.nome.equals(nome)) {
                if (verifica_escopo(s.escopo)) {
                    return s;
                }
            }
        }
        return null;
    }

    private static Simbolo iniciliaza_simbolo() {
        return new Simbolo();
    }

    private static void insere_na_tabela(Simbolo simb) throws SemanticError {
        for (Simbolo s : lista_simbolos) {
            // Verifica se a variável já foi declarada no mesmo escopo
            if (simb.nome.equals(s.nome) && simb.escopo == s.escopo) {
                throw new SemanticError("Variavel ja declarada no escopo atual.");
            }
        }
        // Define o tipo_id baseado na string do tipo
        simb.tipo_id = obterTipoId(simb.tipo);
        lista_simbolos.add(simb);
    }

    public void executeAction(int action, Token token) throws SemanticError {
        String str = token.getLexeme();
        Simbolo simb;
        Simbolo simb_aux;
        String operador;
        String rotulo_temp;
        String rotulo_temp2;
        boolean flag;
        int tipo1, tipo2, resultado, tipoOp;

        if (pilha_escopo.isEmpty()) {
            pilha_escopo.add(0);
        }

        System.out.println("action: " + action + " str: " + str);

        switch (action) {
            case 1:
                tipo_declaracao = str;
                lista_simb_aux.clear();
                break;

            case 2:
                if (!tipo_declaracao.isEmpty()) { // verifica se foi definido o tipo do simbolo [ex: int, float, etc...]
                    simb = iniciliaza_simbolo();
                    simb.nome = str;
                    simb.tipo = tipo_declaracao;
                    simb.tipo_id = obterTipoId(tipo_declaracao); // Define o tipo_id
                    simb.escopo = pilha_escopo.get(pilha_escopo.size() - 1);
                    insere_na_tabela(simb);

                    lista_simb_aux.add(simb); //coloca na lista para caso chegar na action #10, marcar como inicializado
                } else {
                    throw new SemanticError("Tipo nao declarado.");
                }
                break;

            case 3:
                // geração de codigo
                for (Simbolo i : lista_simb_aux) {
                    for (Simbolo s : lista_simbolos) {
                        if (s.nome.equals(i.nome) && s.escopo == i.escopo) {
                            if (s.vetor == false) {
                                ponto_data += s.nome + ":0\n";
                                escrever_text = true;
                            }
                        }
                    }
                }
                //geração de codigo
                tipo_declaracao = "";
                break;

            case 4:
                if (procura_simbolo(str) == false) {
                    throw new SemanticError("Variavel nao declarada.");
                } else {
                    //lista_simb_aux.clear();
                    for (Simbolo s : lista_simbolos) {
                        if (s.nome.equals(str) && verifica_escopo(s.escopo)) {
                            System.out.println("insere na lista aux" + s.nome + s.escopo + "\n");
                            lista_simb_aux.add(s);

                            // Adicionar o tipo à pilha de tipos para expressões
                            pilha_tipos.push(s.tipo_id);
                        }
                    }

                    escrever_text = true;

                    if (entrada_saida_dado.equals("ENTRADA")) {
                        ponto_text += "\nLD $in_port";
                        ponto_text += "\nSTO " + str;
                        entrada_saida_dado = "";
                    }

                    if (recebe_atrib.equals("")) {
                        recebe_atrib = str;
                    }
                }
                retorno = str;
                break;

            case 5:
                if (procura_simbolo(str, true) == false) {
                    throw new SemanticError("Funcao nao declarada.");
                } else {
                    chamada_nome = str;

                    // Adicionar o tipo de retorno da função à pilha de tipos
                    Simbolo funcao = encontra_simbolo(str);
                    if (funcao != null && !funcao.proc) {
                        pilha_tipos.push(funcao.tipo_id);
                    }
                }
                break;

            case 6:
                if (!tipo_declaracao.isEmpty()) {
                    simb = iniciliaza_simbolo();
                    simb.nome = str;
                    simb.tipo = tipo_declaracao;
                    simb.tipo_id = obterTipoId(tipo_declaracao);
                    simb.funcao = true;
                    insere_na_tabela(simb);
                } else {
                    throw new SemanticError("Tipo nao declarado.");
                }

                parametro_aux = str;
                ponto_text += "\n\n_" + parametro_aux;
                break;

            case 7:
                simb = iniciliaza_simbolo();
                simb.nome = str;
                simb.proc = true;
                simb.tipo_id = -1; // Procedimento não tem tipo de retorno
                insere_na_tabela(simb);
                break;

            case 8:
                escopo_cont++;
                pilha_escopo.add(escopo_cont);
                break;

            case 9:
                pilha_escopo.remove(pilha_escopo.size() - 1);
                break;

            case 10:
                // Verificação de atribuição tipo alvo = tipo origem
                if (!lista_simb_aux.isEmpty() && !pilha_tipos.isEmpty()) {
                    Simbolo alvo = lista_simb_aux.get(0); // Variável que recebe o valor
                    int tipoOrigem = pilha_tipos.pop(); // Tipo da expressão avaliada

                    // Verificar compatibilidade de tipos na atribuição
                    int resultadoAtrib = SemanticTable.atribType(alvo.tipo_id, tipoOrigem);

                    if (resultadoAtrib == SemanticTable.ERR) {
                        throw new SemanticError("Atribuição incompatível: não é possível atribuir " +
                                obterTipoString(tipoOrigem) + " a " + alvo.tipo);
                    } else if (resultadoAtrib == SemanticTable.WAR) {
                        System.out.println("Aviso: possível perda de precisão ao atribuir " +
                                obterTipoString(tipoOrigem) + " a " + alvo.tipo);
                    }
                }

                for (Simbolo i : lista_simb_aux) {
                    for (Simbolo s : lista_simbolos) {
                        if (s.nome.equals(i.nome) && s.escopo == i.escopo) {
                            s.iniciado = true;

                            if (s.vetor == true) {
                                ponto_text += "\nSTO 1002";
                                temp3 = true;
                            }
                        }
                    }
                }

                inicio_atribuicao = false;
                break;

            case 11:
                for (Simbolo i : lista_simb_aux) {
                    for (Simbolo s : lista_simbolos) {
                        if (s.nome.equals(i.nome) && s.escopo == i.escopo) {
                            s.vetor = true;

                            // adiciona no .data o nome do vetor
                            ponto_data += s.nome + ":";

                            // e a quantidade de '0' para definir o tamanho
                            for (int i2 = 0; i2 < vetor_tamanho; i2++) {
                                ponto_data += '0';
                                if (i2 < vetor_tamanho - 1) {
                                    ponto_data += ',';
                                }
                            }
                            ponto_data += "\n";
                        }
                    }
                }
                break;

            case 12:
                System.out.println("\n------------- lista de simbolos ------------");
                for (Simbolo s : lista_simbolos) {
                    if (s.nome.equals(str) && s.escopo == pilha_escopo.get(pilha_escopo.size() - 1)) {
                        s.usado = true;
                    }
                }

                // Adicionar o tipo da variável à pilha de tipos
                Simbolo varSimbolo = encontra_simbolo(str);
                if (varSimbolo != null) {
                    pilha_tipos.push(varSimbolo.tipo_id);
                }

                // geracao de codigo
                if (!pilha_operador.isEmpty()) {
                    operador = pilha_operador.peek();
                } else {
                    operador = "";
                }

                if (operador.equals("")) {
                    if (parametro_aux.equals("") || parametro_aux.equals("main")) {
                        ponto_text += "\nLD " + str;
                    } else {
                        ponto_text += "\nLD " + parametro_aux + "_" + str;
                    }

                    if (temp1 == false && calculando_indice == false) {
                        ponto_text += "\nSTO 1000";
                        temp1 = true;
                    }

                    if (!chamada_nome.equals("")) {
                        ponto_text += "\nLD 1000";

                        flag = false;

                        for (Simbolo s : lista_simbolos) {
                            if (flag == true && s.parametro_lido == false) {
                                ponto_text += "\nSTO " + chamada_nome + "_" + s.nome;
                                s.parametro_lido = true;
                                temp1 = false;
                                break;
                            }

                            if (chamada_nome.equals(s.nome) && (s.funcao == true || s.proc == true)) {
                                flag = true;
                            }
                        }
                    }
                } else if (operador.equals("SOMA")) {
                    if (temp1 == true) {
                        ponto_text += "\nLD 1000";
                        ponto_text += "\nADD " + str;
                        ponto_text += "\nSTO 1000";
                    } else {
                        ponto_text += "\nADD " + str;
                    }
                    pilha_operador.pop();
                } else if (operador.equals("SUBTRACAO")) {
                    if (temp1 == true) {
                        ponto_text += "\nLD 1000";
                        ponto_text += "\nSUB " + str;
                        ponto_text += "\nSTO 1000";
                    } else {
                        ponto_text += "\nSUB " + str;
                    }
                    pilha_operador.pop();
                } else if (operador.equals("AND")) {
                    if (temp1 == true) {
                        ponto_text += "\nLD 1000";
                        ponto_text += "\nAND " + str;
                        ponto_text += "\nSTO 1000";

                        pilha_operador.pop();
                    }
                } else if (operador.equals("OR_BIT")) {
                    if (temp1 == true) {
                        ponto_text += "\nLD 1000";
                        ponto_text += "\nOR " + str;
                        ponto_text += "\nSTO 1000";

                        pilha_operador.pop();
                    }
                } else if (operador.equals("XOR_BIT")) {
                    if (temp1 == true) {
                        ponto_text += "\nLD 1000";
                        ponto_text += "\nXOR " + str;
                        ponto_text += "\nSTO 1000";

                        pilha_operador.pop();
                    }
                } else if (operador.equals("NOT")) {
                    ponto_text += "\nNOT " + str;
                    pilha_operador.pop();
                }

                entrando_no_indice = false;
                break;

            case 13:
                System.out.println(str);
                break;

            case 14:
                // Operador + ou -
                if (str.equals("+") || str.equals("-")) {
                    // Verifica tipos dos operandos para soma/subtração na pilha
                    if (pilha_tipos.size() >= 2) {
                        tipo2 = pilha_tipos.pop();
                        tipo1 = pilha_tipos.pop();
                        tipoOp = str.equals("+") ? SemanticTable.SUM : SemanticTable.SUB;

                        resultado = SemanticTable.resultType(tipo1, tipo2, tipoOp);

                        if (resultado == SemanticTable.ERR) {
                            throw new SemanticError("Operação " + str + " incompatível entre os tipos " +
                                    obterTipoString(tipo1) + " e " + obterTipoString(tipo2));
                        }

                        // Coloca o tipo resultante de volta na pilha
                        pilha_tipos.push(resultado);
                    }

                    if (str.equals("+")) {
                        pilha_operador.push("SOMA");
                    } else {
                        pilha_operador.push("SUBTRACAO");
                    }
                }
                break;

            case 15:
                // Operador *, / ou %
                if (str.equals("*") || str.equals("/") || str.equals("%")) {
                    // Verifica tipos dos operandos para multiplicação/divisão/resto na pilha
                    if (pilha_tipos.size() >= 2) {
                        tipo2 = pilha_tipos.pop();
                        tipo1 = pilha_tipos.pop();

                        // Define o tipo de operação
                        if (str.equals("*")) {
                            tipoOp = SemanticTable.MUL;
                        } else if (str.equals("/")) {
                            tipoOp = SemanticTable.DIV;
                        } else {
                            tipoOp = SemanticTable.DIV; // % é tratado como divisão para fins de tipo
                        }

                        resultado = SemanticTable.resultType(tipo1, tipo2, tipoOp);

                        if (resultado == SemanticTable.ERR) {
                            throw new SemanticError("Operação " + str + " incompatível entre os tipos " +
                                    obterTipoString(tipo1) + " e " + obterTipoString(tipo2));
                        }

                        // Verifica casos especiais para divisão
                        if (tipoOp == SemanticTable.DIV) {
                            // Divisão sempre resulta em float
                            resultado = SemanticTable.FLO;
                        }

                        // Coloca o tipo resultante de volta na pilha
                        pilha_tipos.push(resultado);
                    }

                    // Define o operador para geração de código
                    if (str.equals("*")) {
                        pilha_operador.push("MULTIPLICA");
                    } else if (str.equals("/")) {
                        pilha_operador.push("DIVIDE");
                    } else if (str.equals("%")) {
                        pilha_operador.push("RESTO");
                    }
                } else {
                    throw new SemanticError("Operador inválido: " + str);
                }
                break;

            case 20:
                // geracao de codigo
                if (!pilha_operador.isEmpty()) {
                    operador = pilha_operador.peek();
                } else {
                    operador = "";
                }

                if (operador.equals("") || (operador.equals("") && calculando_indice == true)) {
                    if (escrever_text) {
                        ponto_text += "\nLDI " + str;

                        if (temp1 == false && calculando_indice == false) {
                            ponto_text += "\nSTO 1000";
                            temp1 = true;
                        }
                        entrando_no_indice = false;
                    }
                    if (!chamada_nome.equals("")) {
                        ponto_text += "\nLDI " + str;

                        flag = false;

                        for (Simbolo s : lista_simbolos) {
                            if (flag == true && s.parametro_lido == false) {
                                ponto_text += "\nSTO " + chamada_nome + "_" + s.nome;
                                s.parametro_lido = true;
                                break;
                            }

                            if (chamada_nome.equals(s.nome) && (s.funcao == true || s.proc == true)) {
                                flag = true;
                            }
                        }
                    }

                    vetor_tamanho = Integer.parseInt(str);
                } else if (operador.equals("SOMA")) {
                    if (escrever_text) {
                        if (calculando_indice == false) {
                            if (temp1 == true) {
                                ponto_text += "\nLD 1000";
                                ponto_text += "\nADDI " + str;
                                ponto_text += "\nSTO 1000";
                                pilha_operador.pop();
                            }
                        } else if (calculando_indice == true && entrando_no_indice == false) {
                            ponto_text += "\nADDI " + str;
                            pilha_operador.pop();
                        } else if (calculando_indice == true && entrando_no_indice == true) {
                            ponto_text += "\nLDI " + str;
                            entrando_no_indice = false;
                        }
                    }

                    vetor_tamanho += Integer.parseInt(str);
                } else if (operador.equals("SUBTRACAO")) {
                    if (escrever_text) {
                        if (calculando_indice == false) {
                            if (temp1 == true) {
                                ponto_text += "\nLD 1000";
                                ponto_text += "\nSUBI " + str;
                                ponto_text += "\nSTO 1000";
                                pilha_operador.pop();
                            }
                        } else if (calculando_indice == true && entrando_no_indice == false) {
                            ponto_text += "\nSUBI " + str;
                            pilha_operador.pop();
                        } else if (calculando_indice == true && entrando_no_indice == true) {
                            ponto_text += "\nLDI " + str;
                            entrando_no_indice = false;
                        }
                    }

                    vetor_tamanho += Integer.parseInt(str);
                } else if (operador.equals("AND")) {
                    if (escrever_text) {
                        if (temp1 == true) {
                            ponto_text += "\nLD 1000";
                            ponto_text += "\nANDI " + str;
                            ponto_text += "\nSTO 1000";

                            pilha_operador.pop();
                        }
                    }
                } else if (operador.equals("OR_BIT")) {
                    if (escrever_text) {
                        if (temp1 == true) {
                            ponto_text += "\nLD 1000";
                            ponto_text += "\nORI " + str;
                            ponto_text += "\nSTO 1000";

                            pilha_operador.pop();
                        }
                    }
                } else if (operador.equals("XOR_BIT")) {
                    if (escrever_text) {
                        if (temp1 == true) {
                            ponto_text += "\nLD 1000";
                            ponto_text += "\nXORI " + str;
                            ponto_text += "\nSTO 1000";

                            pilha_operador.pop();
                        }
                    }
                } else if (operador.equals("NOT")) {
                    if (escrever_text) {
                        ponto_text += "\nNOT " + str;

                        pilha_operador.pop();
                    }
                } else if (operador.equals("MAIOR") || operador.equals("MENOR") || operador.equals("MAIOR_IGUAL") ||
                        operador.equals("MENOR_IGUAL") || operador.equals("IGUAL") || operador.equals("DIFERENTE")) {
                    if (escrever_text) {
                        if (temp1 == true) {
                            ponto_text += "\nLD 1000";
                            ponto_text += "\nSUB " + str;
                            ponto_text += "\nSTO 1000";

                            pilha_operador.pop();
                        }
                    }
                }
                break;

            case 21:
                entrada_saida_dado = "ENTRADA";
                break;

            case 22:
                entrada_saida_dado = "SAIDA";
                break;

            case 23:
                if (!pilha_operador.isEmpty()) {
                    operador = pilha_operador.peek();
                } else {
                    operador = "";
                }

                if (temp1 == false) {
                    ponto_text += "\nSTO 1000";
                    temp1 = true;
                } else if (temp1 == true && !operador.equals("")) {
                    ponto_text += "\nSTO 1001";
                    ponto_text += "\nLD 1000";

                    if (operador.equals("SOMA")) {
                        ponto_text += "\nADD 1001";
                    } else if (operador.equals("SUBTRACAO")) {
                        ponto_text += "\nSUB 1001";
                    }
                    ponto_text += "\nSTO 1000";
                    if (!pilha_operador.isEmpty()) {
                        pilha_operador.pop();
                    }
                } else if (temp1 == true && operador.equals("")) {
                    simb_aux = lista_simb_aux.get(0);
                    ponto_text += "\nLD 1000";

                    if (parametro_aux.equals("") || parametro_aux.equals("main")) {
                        ponto_text += "\nSTO " + simb_aux.nome;
                    } else {
                        ponto_text += "\nSTO " + parametro_aux + "_" + simb_aux.nome;
                    }
                    temp1 = false;
                }
                break;

            case 24:
                pilha_operador.push("AND");
                break;

            case 25:
                for (Simbolo i : lista_simb_aux) {
                    for (Simbolo s : lista_simbolos) {
                        if (s.nome.equals(i.nome) && s.escopo == i.escopo) {
                            ponto_text += "\nLD 1000 ";
                            ponto_text += "\nSTO " + s.nome;
                            temp1 = false;
                        }
                    }
                }
                break;

            case 26:
                lista_simb_aux.clear();
                tipo_declaracao = "";
                entrada_saida_dado = "";
                pilha_operador.clear();
                recebe_atrib = "";
                vetor_tamanho = 0;
                escrever_text = false;
                temp1 = false;
                temp2 = false;
                temp3 = false;
                inicio_atribuicao = true;
                entrando_no_indice = false;
                break;

            case 27:
                simb_aux = lista_simb_aux.get(0);

                if (temp3 == true) {
                    ponto_text += "\nLD 1002";
                    ponto_text += "\nSTO $indr";

                    if (temp1 == true) {
                        ponto_text += "\nLD 1000";
                        ponto_text += "\nSTOV " + simb_aux.nome;
                        temp1 = false;
                    }
                } else {
                    if (temp1 == true) {
                        ponto_text += "\nLD 1000";
                        ponto_text += "\nSTO " + simb_aux.nome;
                        temp1 = false;
                    }
                }
                break;

            case 28:
                // inicio de indice vetor
                calculando_indice = true;
                entrando_no_indice = true;
                break;

            case 29:
                // fim de indice vetor
                calculando_indice = false;

                if (!pilha_operador.isEmpty()) {
                    operador = pilha_operador.peek();
                } else {
                    operador = "";
                }

                simb_aux = lista_simb_aux.get(lista_simb_aux.size() - 1);

                if (inicio_atribuicao == false) {
                    ponto_text += "\nSTO $indr";
                    ponto_text += "\nLDV " + simb_aux.nome;
                }

                if (inicio_atribuicao == false) {
                    if (temp1 == false) {
                        ponto_text += "\nSTO 1000";
                        temp1 = true;
                    } else {
                        ponto_text += "\nSTO 1001";
                        temp2 = true;

                        ponto_text += "\nLD 1000";
                        if (operador.equals("SOMA")) {
                            ponto_text += "\nADD 1001";
                            ponto_text += "\nSTO 1000";
                            temp2 = false;
                            pilha_operador.pop();
                        } else if (operador.equals("SUBTRACAO")) {
                            ponto_text += "\nSUB 1001";
                            ponto_text += "\nSTO 1000";
                            temp2 = false;
                            pilha_operador.pop();
                        }
                    }
                }
                break;

            case 30:
                pilha_operador.push("OR_BIT");
                break;

            case 31:
                pilha_operador.push("XOR_BIT");
                break;

            case 32:
                pilha_operador.push("NOT");
                break;

            case 33:
                ponto_text += "\nLD 1000";
                ponto_text += "\nSTO $out_port";
                entrada_saida_dado = "";
                break;

            case 34: case 35: case 36: case 37: case 38: case 39:
                if (pilha_tipos.size() >= 2) {
                    tipo2 = pilha_tipos.pop();
                    tipo1 = pilha_tipos.pop();
                    
                    resultado = SemanticTable.resultType(tipo1, tipo2, SemanticTable.REL);
                    
                    if (resultado == SemanticTable.ERR) {
                        throw new SemanticError("Comparação incompatível entre os tipos " +
                                obterTipoString(tipo1) + " e " + obterTipoString(tipo2));
                    }
                    
                    // O resultado de uma comparação é sempre booleano
                    pilha_tipos.push(SemanticTable.BOO);
                }
                
                switch (action) {
                    case 34: 
                        pilha_operador.push("MAIOR");
                        operador_relacional = "MAIOR";
                        break;
                    case 35:
                        pilha_operador.push("MENOR");
                        operador_relacional = "MENOR";
                        break;
                    case 36:
                        pilha_operador.push("MAIOR_IGUAL");
                        operador_relacional = "MAIOR_IGUAL";
                        break;
                    case 37:
                        pilha_operador.push("MENOR_IGUAL");
                        operador_relacional = "MENOR_IGUAL";
                        break;
                    case 38:
                        pilha_operador.push("IGUAL");
                        operador_relacional = "IGUAL";
                        break;
                    case 39:
                        pilha_operador.push("DIFERENTE");
                        operador_relacional = "DIFERENTE";
                        break;
                }
                break;

            case 40:
                rotulo_cont++;
                rotulo_temp = "R" + rotulo_cont;
                pilha_rotulo.push(rotulo_temp);

                if (temp1 == true) {
                    ponto_text += "\nLD 1000";
                }

                if (operador_relacional.equals("MAIOR")) {
                    ponto_text += "\nBLT " + rotulo_temp;
                } else if (operador_relacional.equals("MENOR")) {
                    ponto_text += "\nBGT " + rotulo_temp;
                } else if (operador_relacional.equals("MAIOR_IGUAL")) {
                    ponto_text += "\nBLE " + rotulo_temp;
                } else if (operador_relacional.equals("MENOR_IGUAL")) {
                    ponto_text += "\nBGE " + rotulo_temp;
                } else if (operador_relacional.equals("IGUAL")) {
                    ponto_text += "\nBNE " + rotulo_temp;
                } else if (operador_relacional.equals("DIFERENTE")) {
                    ponto_text += "\nBEQ " + rotulo_temp;
                }
                operador_relacional = "";
                if (temp1 == true) {
                    temp1 = false;
                }
                break;

            case 41:
                if (!pilha_rotulo.isEmpty()) {
                    rotulo_temp = pilha_rotulo.pop();
                } else {
                    rotulo_temp = "";
                }
                ponto_text += "\n\n" + rotulo_temp + ":";
                rotulo_cont--;
                break;

            case 42:
                if (!pilha_rotulo.isEmpty()) {
                    rotulo_temp = pilha_rotulo.pop();
                } else {
                    rotulo_temp = "";
                }

                rotulo_cont++;
                rotulo_temp2 = "R" + rotulo_cont;
                pilha_rotulo.push(rotulo_temp2);
                ponto_text += "\nJMP " + rotulo_temp2;
                ponto_text += "\n\n" + rotulo_temp + ":";
                rotulo_cont--;
                break;

            case 43:
                if (!pilha_rotulo.isEmpty()) {
                    rotulo_temp = pilha_rotulo.pop();
                } else {
                    rotulo_temp = "";
                }
                ponto_text += "\n\n" + rotulo_temp + ":";
                rotulo_cont--;
                break;

            case 44:
                rotulo_cont++;
                rotulo_temp = "R" + rotulo_cont;
                pilha_rotulo.push(rotulo_temp);
                ponto_text += "\n\n" + rotulo_temp + ":";
                break;

            case 45:
            rotulo_cont++;
            rotulo_temp = "R" + Integer.toString(rotulo_cont);
            pilha_rotulo.add(rotulo_temp);

            if (temp1) {
                ponto_text += "\nLD 1000";
            }

            if (operador_relacional.equals("MAIOR")) {
                ponto_text += "\nBLT " + rotulo_temp;
            } else if (operador_relacional.equals("MENOR")) {
                ponto_text += "\nBGT " + rotulo_temp;
            } else if (operador_relacional.equals("MAIOR_IGUAL")) {
                ponto_text += "\nBLE " + rotulo_temp;
            } else if (operador_relacional.equals("MENOR_IGUAL")) {
                ponto_text += "\nBGE " + rotulo_temp;
            } else if (operador_relacional.equals("IGUAL")) {
                ponto_text += "\nBNE " + rotulo_temp;
            } else if (operador_relacional.equals("DIFERENTE")) {
                ponto_text += "\nBEQ " + rotulo_temp;
            }
            operador_relacional = "";
            if (temp1) {
                temp1 = false;
            }
            break;

        case 46:
            if (!pilha_rotulo.isEmpty()) {
                rotulo_temp = pilha_rotulo.get(pilha_rotulo.size() - 1); // r2
            } else {
                rotulo_temp = "";
            }
            pilha_rotulo.remove(pilha_rotulo.size() - 1);

            if (!pilha_rotulo.isEmpty()) {
                rotulo_temp2 = pilha_rotulo.get(pilha_rotulo.size() - 1); // r1
            } else {
                rotulo_temp2 = "";
            }
            pilha_rotulo.remove(pilha_rotulo.size() - 1);

            ponto_text += "\nJMP " + rotulo_temp2;
            ponto_text += "\n\n" + rotulo_temp + ":";
            break;

        case 47:
            rotulo_cont++;
            rotulo_temp = "R" + Integer.toString(rotulo_cont);
            pilha_rotulo.add(rotulo_temp);
            ponto_text += "\n\n" + rotulo_temp + ":";
            break;

        case 48:
            if (!pilha_rotulo.isEmpty()) {
                rotulo_temp = pilha_rotulo.get(pilha_rotulo.size() - 1); // r1
            } else {
                rotulo_temp = "";
            }
            pilha_rotulo.remove(pilha_rotulo.size() - 1);

            if (temp1) {
                ponto_text += "\nLD 1000";
            }

            if (operador_relacional.equals("MAIOR")) {
                ponto_text += "\nBGT " + rotulo_temp;
            } else if (operador_relacional.equals("MENOR")) {
                ponto_text += "\nBLT " + rotulo_temp;
            } else if (operador_relacional.equals("MAIOR_IGUAL")) {
                ponto_text += "\nBGE " + rotulo_temp;
            } else if (operador_relacional.equals("MENOR_IGUAL")) {
                ponto_text += "\nBLE " + rotulo_temp;
            } else if (operador_relacional.equals("IGUAL")) {
                ponto_text += "\nBEQ " + rotulo_temp;
            } else if (operador_relacional.equals("DIFERENTE")) {
                ponto_text += "\nBNE " + rotulo_temp;
            }
            operador_relacional = "";
            if (temp1) {
                temp1 = false;
            }
            rotulo_cont--;
            break;

        case 49:
            rotulo_cont++;
            rotulo_temp = "R" + Integer.toString(rotulo_cont);
            pilha_rotulo.add(rotulo_temp);
            ponto_text += "\n\n" + rotulo_temp + ":";
            break;

        case 50:
            rotulo_cont++;
            rotulo_temp = "R" + Integer.toString(rotulo_cont);
            pilha_rotulo.add(rotulo_temp);

            if (temp1) {
                ponto_text += "\nLD 1000";
            }

            if (operador_relacional.equals("MAIOR")) {
                ponto_text += "\nBLT " + rotulo_temp;
            } else if (operador_relacional.equals("MENOR")) {
                ponto_text += "\nBGT " + rotulo_temp;
            } else if (operador_relacional.equals("MAIOR_IGUAL")) {
                ponto_text += "\nBLE " + rotulo_temp;
            } else if (operador_relacional.equals("MENOR_IGUAL")) {
                ponto_text += "\nBGE " + rotulo_temp;
            } else if (operador_relacional.equals("IGUAL")) {
                ponto_text += "\nBNE " + rotulo_temp;
            } else if (operador_relacional.equals("DIFERENTE")) {
                ponto_text += "\nBEQ " + rotulo_temp;
            }
            operador_relacional = "";
            if (temp1) {
                temp1 = false;
            }
            rotulo_cont--;
            break;

        case 51:
            if (!pilha_rotulo.isEmpty()) {
                rotulo_temp = pilha_rotulo.get(pilha_rotulo.size() - 1); // r2
            } else {
                rotulo_temp = "";
            }
            pilha_rotulo.remove(pilha_rotulo.size() - 1);

            if (!pilha_rotulo.isEmpty()) {
                rotulo_temp2 = pilha_rotulo.get(pilha_rotulo.size() - 1); // r1
            } else {
                rotulo_temp2 = "";
            }
            pilha_rotulo.remove(pilha_rotulo.size() - 1);

            ponto_text += "\nJMP " + rotulo_temp2;
            ponto_text += "\n\n" + rotulo_temp + ":";
            break;

        case 52:
            ponto_text += "\n\n_" + str + ":";

            simb = iniciliaza_simbolo();
            simb.nome = str;
            simb.tipo = "VOID";
            simb.proc = true;
            insere_na_tabela(simb);
            break;

        case 53:
            if (!parametro_aux.equals("main")) {
                ponto_text += "\nRETURN 0 ";
            }

            parametro_aux = "";
            break;

        case 54:
            if (parametro_aux.equals("") || parametro_aux.equals("main")) {
                ponto_text += "\nLD " + retorno;
            } else {
                ponto_text += "\nLD " + parametro_aux + "_" + retorno;
            }
            break;

        case 55:
            if (!parametro_aux.equals("main")) {
                ponto_data += parametro_aux + "_" + str + ": 0\n";
            }

            if (!tipo_declaracao.isEmpty()) { // verifica se foi definido o tipo do simbolo [ex: int, float, etc...]
                simb = iniciliaza_simbolo();
                simb.nome = str;
                simb.tipo = tipo_declaracao;
                simb.escopo = pilha_escopo.get(pilha_escopo.size() - 1);
                simb.parametro = true;
                insere_na_tabela(simb);

                lista_simb_aux.add(simb); // coloca na lista para caso chegar na action #10, marcar como inicializado
            } else {
                throw new SemanticError("Tipo nao declarado.");
            }
            break;

        case 56: {
            boolean inicia_contagem = false;
            boolean funcao_existe = false;
            int parametros_totais = 0;

            ponto_text += "\nCALL _" + chamada_nome;
            for (Simbolo s : lista_simbolos) {
                s.parametro_lido = false;
            }

            for (Simbolo s : lista_simbolos) {
                if (s.nome.equals(chamada_nome) && (s.funcao || s.proc)) {
                    inicia_contagem = true;
                    funcao_existe = true;
                    continue;
                }

                if (inicia_contagem && s.parametro) {
                    ++parametros_totais;
                    continue;
                }

                if (inicia_contagem && !s.parametro) {
                    inicia_contagem = false;
                    break;
                }
            }

            if (!chamada_nome.equals("main") && !funcao_existe) {
                throw new SemanticError("Funcao ou procedimento " + chamada_nome + " nao criada");
            }

            if (!chamada_nome.equals("main") && conta_parm != parametros_totais) {
                throw new SemanticError("Numero de parametros incorreto para " + chamada_nome);
            }

            conta_parm = 0;
            chamada_nome = "";
            break;
        }

        case 57: {
            ++conta_parm;
            break;
        }
    }

        // Iterando pela lista e imprimindo os símbolos
        System.out.println("\n------------- lista de simbolos ------------");
        for (Simbolo s : lista_simbolos) {
            System.out.println("Tipo: " + s.tipo + ", Nome: " + s.nome +
                    ", Escopo: " + s.escopo + ", Iniciado: " + s.iniciado +
                    ", Vetor: " + s.vetor + ", Usado: " + s.usado);
        }

        // Iterando pela lista e imprimindo os símbolos
        System.out.println("\n------------- Pilha de escopo ------------");
        for (Integer e : pilha_escopo) {
            System.out.println("Escopo: " + e);
        }
        System.out.println("------------- Fim ------------\n");
        System.out.println("------------ codigo ------------\n");
        System.out.println(ponto_data + "\n" + ponto_text + "\n" + "HLT 0");

        // Criando o JSON com a lista de símbolos
        JSONArray jsonSimbolos = new JSONArray();
        String codigoAssembly = ponto_data + "\n" + ponto_text;

        for (Simbolo s : lista_simbolos) {
            JSONObject simboloJson = new JSONObject();
            simboloJson.put("tipo", s.tipo);
            simboloJson.put("nome", s.nome);
            simboloJson.put("iniciado", s.iniciado);
            simboloJson.put("usado", s.usado);
            simboloJson.put("escopo", s.escopo);
            simboloJson.put("parametro", s.parametro);
            simboloJson.put("pos_param", s.pos_param);
            simboloJson.put("vetor", s.vetor);
            simboloJson.put("matriz", s.matriz);
            simboloJson.put("ref", s.ref);
            simboloJson.put("funcao", s.funcao);
            simboloJson.put("proc", s.proc);
            jsonSimbolos.add(simboloJson);
        }

        if (!jsonSimbolos.isEmpty()) {
            // Abrir o arquivo para escrita
            try (FileWriter file = new FileWriter("simbolos.json")) {
                // Escrever o JSON no arquivo com indentação de 4 espaços
                file.write(jsonSimbolos.toString());
                // O arquivo é fechado automaticamente com try-with-resources
            } catch (IOException e) {
                // Se houver um problema ao abrir o arquivo, exibir uma mensagem de erro
                System.out.println("Erro ao abrir o arquivo para escrita: " + e.getMessage());
            }
        }

        if (codigoAssembly != null && !codigoAssembly.isEmpty()) {
            // Nome do arquivo onde a string será salva
            String nomeDoArquivo = "temp.tmp";

            // Escrever no arquivo
            try {
                Files.writeString(Paths.get(nomeDoArquivo), codigoAssembly);
                // System.out.println("String salva com sucesso no arquivo " + nomeDoArquivo);
            } catch (IOException e) {
                // System.out.println("Não foi possível abrir o arquivo " + nomeDoArquivo);
                e.printStackTrace();
            }
        }


    }

// Método para limpar o estado do analisador semântico
public void limpaSemantico() {
    lista_simbolos.clear();
    pilha_escopo.clear();
    lista_simb_aux.clear();
    tipo_declaracao = "";
    entrada_saida_dado = "";
    pilha_operador.clear();
    recebe_atrib = "";
    vetor_tamanho = 0;
    escrever_text = false;
    temp1 = false;
    temp2 = false;
    temp3 = false;
    inicio_atribuicao = true;
    entrando_no_indice = false;
    ponto_data = ".data\n";
    ponto_text = ".text\n JMP _main \n";
    parametro_aux = "";
    chamada_nome = "";
    retorno = "";
    conta_parm = 0;
}

// Classe de erro semântico
static class SemanticError extends RuntimeException {
    public SemanticError(String message) {
        super(message);
    }
}
}