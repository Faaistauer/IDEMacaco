package gals;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.json.JSONArray;
import org.json.JSONObject;

public class Semantico {

    public static final int ERR = -1;
    public static final int OK_ = 0;
    public static final int WAR = 1;


    public static final int INT = 0;
    public static final int FLO = 1;
    public static final int CHA = 2;
    public static final int STR = 3;
    public static final int BOO = 4;

    public static final int SUM = 0;
    public static final int SUB = 1;
    public static final int MUL = 2;
    public static final int DIV = 3;
    public static final int REL = 4; // qualquer operador relacional

    // TIPO DE RETORNO DAS EXPRESSOES ENTRE TIPOS
    // 5 x 5 X 5  = TIPO X TIPO X OPER
    static int expTable [][][] =
            {/*       INT       */ /*       FLOAT     */ /*      CHAR       */ /*      STRING     */ /*     BOOL        */
                    /*   INT*/ {{INT,INT,INT,FLO,BOO},{FLO,FLO,FLO,FLO,BOO},{ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR}},
                    /* FLOAT*/ {{FLO,FLO,FLO,FLO,BOO},{FLO,FLO,FLO,FLO,BOO},{ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR}},
                    /*  CHAR*/ {{ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR},{STR,CHA,ERR,ERR,BOO},{STR,ERR,ERR,ERR,BOO},{ERR,ERR,ERR,ERR,ERR}},
                    /* STRING*/{{ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR},{STR,STR,ERR,ERR,BOO},{STR,STR,STR,STR,BOO},{ERR,ERR,ERR,ERR,ERR}},
                    /*  BOOL*/ {{ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR},{BOO,BOO,BOO,BOO,BOO}}
            };

    // atribuicoes compativeis
    // 5 x 5 = TIPO X TIPO
    static int atribTable [][]={/* INT FLO CHA STR BOO  */
            /*INT*/ {OK_,WAR,ERR,ERR,ERR},
            /*FLO*/ {OK_,OK_,ERR,ERR,ERR},
            /*CHA*/ {ERR,ERR,OK_,ERR,ERR},
            /*STR*/ {ERR,ERR,OK_,OK_,ERR},
            /*BOO*/ {ERR,ERR,ERR,ERR,OK_}
    };

    public static int resultType (int TP1, int TP2, int OP){
        return (expTable[TP1][TP2][OP]);
    }

    public static int atribType (int TP1, int TP2){
        return (atribTable[TP1][TP2]);
    }

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
        }
    }

    private static int expr_type = INT; // Tipo da expressão atual
    private static Stack<Integer> pilha_tipos = new Stack<>(); // Pilha para tipos durante expressões
    private static List<Simbolo> lista_simbolos = new ArrayList<>();
    private static List<Integer> pilha_escopo = new ArrayList<>();
    private static List<Simbolo> lista_simb_aux = new ArrayList<>(); // essa lista serve pra marcar os simbolos como inicializados
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

    // Método para converter string de tipo para código de tipo
    private static int stringParaTipo(String tipoStr) {
        if (tipoStr == null) return ERR;
        switch(tipoStr.toUpperCase()) {
            case "macaque": return INT;
            case "chimp": return FLO;
            case "mico": return CHA;
            case "bugio": return STR;
            case "BOOLEAN": return BOO;
            default: return ERR;
        }
    }

    // Método para obter nome do tipo a partir do código
    private static String nomeTipo(int tipo) {
        switch(tipo) {
            case INT: return "INT";
            case FLO: return "FLOAT";
            case CHA: return "CHAR";
            case STR: return "STRING";
            case BOO: return "BOOLEAN";
            default: return "DESCONHECIDO";
        }
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
        lista_simbolos.add(simb);
    }

    public void executeAction(int action, Token token) throws SemanticError {
        String str = token.getLexeme();
        Simbolo simb;
        Simbolo simb_aux = null;
        String operador;
        String rotulo_temp;
        String rotulo_temp2;
        boolean flag;

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
                    // Encontra o símbolo na tabela e armazena seu tipo
                    for (Simbolo s : lista_simbolos) {
                        if (s.nome.equals(str) && verifica_escopo(s.escopo)) {
                            System.out.println("insere na lista aux" + s.nome + s.escopo + "\n");
                            lista_simb_aux.add(s);

                            // Guarda o tipo do identificador para uso em expressões
                            expr_type = stringParaTipo(s.tipo);
                            pilha_tipos.push(expr_type);
                            break;
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
                }
                break;

            case 6:
                if (!tipo_declaracao.isEmpty()) {
                    simb = iniciliaza_simbolo();
                    simb.nome = str;
                    simb.tipo = tipo_declaracao;
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
                        // Guarda o tipo para uso em verificação de expressões
                        expr_type = stringParaTipo(s.tipo);
                        if (pilha_tipos.size() > 0 && !pilha_operador.isEmpty()) {
                            int tipo1 = pilha_tipos.pop();
                            int operacao = -1;

                            // Mapeia operadores para os códigos de operação
                            String op = pilha_operador.peek();
                            if (op.equals("SOMA")) operacao = SUM;
                            else if (op.equals("SUBTRACAO")) operacao = SUB;
                            else if (op.equals("MULTIPLICACAO")) operacao = MUL;
                            else if (op.equals("DIVISAO")) operacao = DIV;
                            else if (op.equals("MAIOR") || op.equals("MENOR") || op.equals("MAIOR_IGUAL") ||
                                    op.equals("MENOR_IGUAL") || op.equals("IGUAL") || op.equals("DIFERENTE"))
                                operacao = REL;

                            if (operacao != -1) {
                                int resultadoTipo = resultType(tipo1, expr_type, operacao);
                                if (resultadoTipo == ERR) {
                                    throw new SemanticError("Incompatibilidade de tipos: operação " +
                                            op + " entre " + nomeTipo(tipo1) +
                                            " e " + nomeTipo(expr_type));
                                }
                                // Atualiza o tipo resultante da expressão
                                expr_type = resultadoTipo;
                            }
                        }
                        pilha_tipos.push(expr_type);
                    }
                }

                // geracao de codigo
                // [...código de geração como estava...]

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
                }
                // [...continua código de geração como estava...]

                entrando_no_indice = false;
                break;

            case 13:
                System.out.println(str);
                break;

            case 14:
                if (str.equals("+")) {
                    pilha_operador.push("SOMA");
                } else if (str.equals("-")) {
                    pilha_operador.push("SUBTRACAO");
                }
                break;

            case 15:
                if (str.equals("*")) {
                    pilha_operador.push("MULTIPLICACAO");
                } else if (str.equals("/")) {
                    pilha_operador.push("DIVISAO");
                } else if (str.equals("%")) {
                    pilha_operador.push("RESTO");
                }
                break;

            case 20:
                // geracao de codigo
                expr_type = INT;
                pilha_tipos.push(expr_type);
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
                // Obtém o operador atual (se houver)
                if (!pilha_operador.isEmpty()) {
                    operador = pilha_operador.peek();
                } else {
                    operador = "";
                }

                // Caso seja o primeiro valor da expressão
                if (temp1 == false) {
                    ponto_text += "\nSTO 1000";
                    temp1 = true;
                }
                // Caso tenha um operador pendente e um valor já armazenado
                else if (temp1 == true && !operador.equals("")) {
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
                }
                // Caso seja uma atribuição final
                else if (temp1 == true && operador.equals("")) {
                    // Verificação de compatibilidade de tipos
                    if (!lista_simb_aux.isEmpty()) {
                        simb_aux = lista_simb_aux.get(0);

                        // Obtém o tipo da variável de destino
                        int tipoDestino = stringParaTipo(simb_aux.tipo);

                        // Utiliza o tipo atual da expressão
                        int tipoExpressao = expr_type;

                        // Se houver valor na pilha de tipos, usa o topo
                        if (!pilha_tipos.isEmpty()) {
                            tipoExpressao = pilha_tipos.peek();
                        }

                        // Verifica a compatibilidade usando atribType
                        int resultado = atribType(tipoDestino, tipoExpressao);

                        if (resultado == ERR) {
                            throw new SemanticError("Incompatibilidade de tipos na atribuição. " +
                                    "Não é possível atribuir " + nomeTipo(tipoExpressao) +
                                    " para " + simb_aux.nome + " de tipo " + nomeTipo(tipoDestino));
                        } else if (resultado == WAR) {
                            System.out.println("Aviso: Possível perda de dados na atribuição de " +
                                    nomeTipo(tipoExpressao) + " para " + simb_aux.nome +
                                    " de tipo " + nomeTipo(tipoDestino));
                        }
                    }

                    // Geração de código da atribuição
                    ponto_text += "\nLD 1000";

                    if (parametro_aux.equals("") || parametro_aux.equals("main")) {
                        ponto_text += "\nSTO " + simb_aux.nome;
                    } else {
                        ponto_text += "\nSTO " + parametro_aux + "_" + simb_aux.nome;
                    }
                    temp1 = false;

                    // Limpa a pilha de tipos após uma atribuição completa
                    while (!pilha_tipos.empty()) {
                        pilha_tipos.pop();
                    }
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
            jsonSimbolos.put(simboloJson);
        }

        if (!jsonSimbolos.isEmpty()) {
            // Abrir o arquivo para escrita
            try (FileWriter file = new FileWriter("simbolos.json")) {
                // Escrever o JSON no arquivo com indentação de 4 espaços
                file.write(jsonSimbolos.toString(4));
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
        expr_type = INT;
        pilha_tipos.clear();
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
