package br.com.arthurhenrique.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import br.com.arthurhenrique.api.Api;
import br.com.arthurhenrique.client.ApiClient;
import br.com.arthurhenrique.model.Projeto;
import br.com.arthurhenrique.service.ProjetoService;

public class TelaPrincipal extends JFrame {

    private ProjetoService service;
    private ApiClient apiClient;

    // Campos de texto
    private JTextField campoNome;
    private JTextField campoDescricao;

    // Seletores
    private JComboBox<String> comboCategoria;
    private JComboBox<String> comboStatus;

    // Tabela
    private DefaultTableModel modelo;
    private JTable tabela;

    // Botões principais
    private JButton botaoCadastrar;
    private JButton botaoLimpar;
    private JButton botaoExcluir;
    private JButton botaoAlterar;

    // Cabeçalho dinâmico
    private JLabel subtituloCabecalho;

    // Paleta e tipografia
    private static final Color COR_FUNDO = new Color(244, 246, 248);
    private static final Color COR_TOPO = new Color(38, 50, 56);
    private static final Color COR_CARD = Color.WHITE;
    private static final Color COR_BORDA = new Color(220, 226, 232);
    private static final Color COR_TEXTO = new Color(33, 37, 41);
    private static final Color COR_MUTED = new Color(108, 117, 125);

    private static final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONTE_SECAO = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONTE_NUMERO = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONTE_PADRAO = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 13);

    public TelaPrincipal() throws Exception {
        setTitle("Sistema de Projetos");
        setSize(1080, 840);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().setBackground(COR_FUNDO);
        setLayout(new BorderLayout());
        add(criarCabecalho(), BorderLayout.NORTH);

        service = new ProjetoService();
        apiClient = new ApiClient();

        criarComponentes();
        criarEventos();

        try {
            service.carregar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar: " + e.getMessage());
        }
        carregarTabela();
    }

    public void criarComponentes() {
        // 1. Elementos do formulário (Aba Projetos)
        campoNome = new JTextField(22);
        campoDescricao = new JTextField(34);

        comboCategoria = new JComboBox<>(new String[]{"Web", "Software", "Mobile", "Outro"});
        comboStatus = new JComboBox<>(new String[]{"Planejado", "Em desenvolvimento", "Concluído"});

        botaoCadastrar = new JButton("Cadastrar");
        botaoAlterar = new JButton("Alterar");
        botaoExcluir = new JButton("Excluir");
        botaoLimpar = new JButton("Limpar");

        estilizarCampo(campoNome);
        estilizarCampo(campoDescricao);
        estilizarCombo(comboCategoria);
        estilizarCombo(comboStatus);
        estilizarBotao(botaoCadastrar, new Color(33, 150, 243), Color.WHITE);
        estilizarBotao(botaoAlterar, new Color(255, 152, 0), Color.WHITE);
        estilizarBotao(botaoExcluir, new Color(220, 53, 69), Color.WHITE);
        estilizarBotao(botaoLimpar, new Color(108, 117, 125), Color.WHITE);

        modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        modelo.addColumn("ID");
        modelo.addColumn("Nome");
        modelo.addColumn("Categoria");
        modelo.addColumn("Status");

        tabela = new JTable(modelo);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollTabelaProjetos = new JScrollPane(tabela);
        estilizarTabela(tabela, scrollTabelaProjetos);

        TableRowSorter<TableModel> sorterRelatorios = new TableRowSorter<>(modelo);

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int linhaView = tabela.getSelectedRow();
                if (linhaView != -1) {
                    int linhaModel = tabela.convertRowIndexToModel(linhaView);
                    int id = Integer.parseInt(modelo.getValueAt(linhaModel, 0).toString());

                    for (Projeto p : service.listar()) {
                        if (p.getId() == id) {
                            campoNome.setText(p.getNome() != null ? p.getNome() : "");
                            campoDescricao.setText(p.getDescricao() != null ? p.getDescricao() : "");
                            if (p.getCategoria() != null) {
                                comboCategoria.setSelectedItem(p.getCategoria());
                            }
                            if (p.getStatus() != null) {
                                comboStatus.setSelectedItem(p.getStatus());
                            }
                            break;
                        }
                    }
                }
            }
        });

        JPanel painelFormulario = new JPanel();
        painelFormulario.setLayout(new BoxLayout(painelFormulario, BoxLayout.Y_AXIS));
        painelFormulario.setBackground(COR_CARD);

        JLabel tituloFormulario = new JLabel("Cadastrar / Editar Projeto");
        tituloFormulario.setFont(FONTE_SECAO);
        tituloFormulario.setForeground(COR_TEXTO);
        tituloFormulario.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel linha1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        linha1.setBackground(COR_CARD);
        linha1.setAlignmentX(Component.LEFT_ALIGNMENT);
        linha1.add(criarRotulo("Nome:"));
        linha1.add(campoNome);
        linha1.add(criarRotulo("Categoria:"));
        linha1.add(comboCategoria);
        linha1.add(criarRotulo("Status:"));
        linha1.add(comboStatus);

        JPanel linha2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        linha2.setBackground(COR_CARD);
        linha2.setAlignmentX(Component.LEFT_ALIGNMENT);
        linha2.add(criarRotulo("Descrição:"));
        linha2.add(campoDescricao);

        JPanel linhaBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        linhaBotoes.setBackground(COR_CARD);
        linhaBotoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaBotoes.add(botaoCadastrar);
        linhaBotoes.add(botaoAlterar);
        linhaBotoes.add(botaoExcluir);
        linhaBotoes.add(botaoLimpar);

        painelFormulario.add(tituloFormulario);
        painelFormulario.add(Box.createVerticalStrut(6));
        painelFormulario.add(linha1);
        painelFormulario.add(linha2);
        painelFormulario.add(linhaBotoes);

        JPanel abaProjetos = new JPanel();
        abaProjetos.setLayout(new BoxLayout(abaProjetos, BoxLayout.Y_AXIS));
        abaProjetos.setBackground(COR_FUNDO);
        abaProjetos.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel cardFormulario = criarCard();
        cardFormulario.setLayout(new BorderLayout());
        cardFormulario.add(painelFormulario, BorderLayout.CENTER);
        cardFormulario.setMaximumSize(new Dimension(Integer.MAX_VALUE, cardFormulario.getPreferredSize().height));

        JPanel cardTabelaProjetos = criarCard();
        cardTabelaProjetos.setLayout(new BorderLayout(0, 10));
        JLabel tituloTabelaProjetos = new JLabel("Projetos Cadastrados");
        tituloTabelaProjetos.setFont(FONTE_SECAO);
        tituloTabelaProjetos.setForeground(COR_TEXTO);
        cardTabelaProjetos.add(tituloTabelaProjetos, BorderLayout.NORTH);
        cardTabelaProjetos.add(scrollTabelaProjetos, BorderLayout.CENTER);

        abaProjetos.add(cardFormulario);
        abaProjetos.add(Box.createVerticalStrut(16));
        abaProjetos.add(cardTabelaProjetos);

        // 2. Aba Categorias completa (Filtrar por Categoria + Todos os Projetos com contador)
        JPanel abaCategorias = new JPanel();
        abaCategorias.setLayout(new BoxLayout(abaCategorias, BoxLayout.Y_AXIS));
        abaCategorias.setBackground(COR_FUNDO);
        abaCategorias.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel cardFiltroCategoria = criarCard();
        cardFiltroCategoria.setLayout(new BoxLayout(cardFiltroCategoria, BoxLayout.Y_AXIS));
        cardFiltroCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardFiltroCategoria.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JLabel tituloFiltroCategoria = new JLabel("Filtrar por Categoria");
        tituloFiltroCategoria.setFont(FONTE_SECAO);
        tituloFiltroCategoria.setForeground(COR_TEXTO);
        tituloFiltroCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel painelBotoesCategoria = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        painelBotoesCategoria.setBackground(COR_CARD);
        painelBotoesCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);

        final String[] categoriaAtiva = {"Todas"};
        String[] opcoesCategoria = {"Todas", "Web", "Software", "Mobile", "Outro"};
        Map<String, JButton> botoesCategoria = new LinkedHashMap<>();

        DefaultTableModel modeloCategorias = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        modeloCategorias.addColumn("ID");
        modeloCategorias.addColumn("Nome");
        modeloCategorias.addColumn("Categoria");
        modeloCategorias.addColumn("Status");

        JTable tabelaCategorias = new JTable(modeloCategorias);
        tabelaCategorias.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollCategorias = new JScrollPane(tabelaCategorias);
        estilizarTabela(tabelaCategorias, scrollCategorias);

        JPanel cardTodosProjetosCat = criarCard();
        cardTodosProjetosCat.setLayout(new BorderLayout(0, 10));
        cardTodosProjetosCat.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel cabecalhoCatProjetos = new JPanel(new BorderLayout());
        cabecalhoCatProjetos.setBackground(COR_CARD);

        JLabel tituloTodosCat = new JLabel("Todos os Projetos");
        tituloTodosCat.setFont(FONTE_SECAO);
        tituloTodosCat.setForeground(COR_TEXTO);

        JLabel lblBadgeProjetosCat = new JLabel("0 projeto(s)");
        lblBadgeProjetosCat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBadgeProjetosCat.setForeground(COR_TEXTO);
        lblBadgeProjetosCat.setOpaque(true);
        lblBadgeProjetosCat.setBackground(new Color(238, 241, 244));
        lblBadgeProjetosCat.setBorder(new EmptyBorder(5, 10, 5, 10));

        cabecalhoCatProjetos.add(tituloTodosCat, BorderLayout.WEST);
        cabecalhoCatProjetos.add(lblBadgeProjetosCat, BorderLayout.EAST);

        cardTodosProjetosCat.add(cabecalhoCatProjetos, BorderLayout.NORTH);
        cardTodosProjetosCat.add(scrollCategorias, BorderLayout.CENTER);

        Runnable atualizarCategorias = () -> {
            int totalGeral = modelo.getRowCount();
            int web = 0;
            int software = 0;
            int mobile = 0;
            int outro = 0;

            for (int i = 0; i < totalGeral; i++) {
                String cat = modelo.getValueAt(i, 2) != null ? modelo.getValueAt(i, 2).toString().trim() : "";
                if ("Web".equalsIgnoreCase(cat)) {
                    web++;
                } else if ("Software".equalsIgnoreCase(cat)) {
                    software++;
                } else if ("Mobile".equalsIgnoreCase(cat)) {
                    mobile++;
                } else if (!cat.isEmpty()) {
                    outro++;
                }
            }

            modeloCategorias.setRowCount(0);
            for (int i = 0; i < totalGeral; i++) {
                String cat = modelo.getValueAt(i, 2) != null ? modelo.getValueAt(i, 2).toString().trim() : "";
                boolean corresponde = "Todas".equalsIgnoreCase(categoriaAtiva[0])
                        || ("Outro".equalsIgnoreCase(categoriaAtiva[0])
                        ? (!"Web".equalsIgnoreCase(cat) && !"Software".equalsIgnoreCase(cat) && !"Mobile".equalsIgnoreCase(cat))
                        : cat.equalsIgnoreCase(categoriaAtiva[0]));

                if (corresponde) {
                    modeloCategorias.addRow(new Object[]{
                        modelo.getValueAt(i, 0),
                        modelo.getValueAt(i, 1),
                        modelo.getValueAt(i, 2),
                        modelo.getValueAt(i, 3)
                    });
                }
            }

            int qtdExibida = modeloCategorias.getRowCount();
            lblBadgeProjetosCat.setText(qtdExibida + " projeto(s)");

            for (String op : opcoesCategoria) {
                JButton btn = botoesCategoria.get(op);
                if (btn != null) {
                    int qtdOp = "Todas".equals(op) ? totalGeral
                            : "Web".equals(op) ? web
                            : "Software".equals(op) ? software
                            : "Mobile".equals(op) ? mobile : outro;
                    btn.setText(op + " (" + qtdOp + ")");

                    boolean selecionada = op.equalsIgnoreCase(categoriaAtiva[0]);
                    if (selecionada) {
                        estilizarBotao(btn, COR_TOPO, Color.WHITE);
                    } else {
                        estilizarBotao(btn, new Color(241, 243, 245), COR_TEXTO);
                    }
                }
            }
        };

        for (String op : opcoesCategoria) {
            JButton btn = new JButton(op);
            estilizarBotao(btn, new Color(241, 243, 245), COR_TEXTO);
            btn.addActionListener(e -> {
                categoriaAtiva[0] = op;
                atualizarCategorias.run();
            });
            botoesCategoria.put(op, btn);
            painelBotoesCategoria.add(btn);
        }

        cardFiltroCategoria.add(tituloFiltroCategoria);
        cardFiltroCategoria.add(painelBotoesCategoria);

        abaCategorias.add(cardFiltroCategoria);
        abaCategorias.add(Box.createVerticalStrut(16));
        abaCategorias.add(cardTodosProjetosCat);

        // 3. Aba Dashboard completa (KPIs, Distribuição por Categoria, Taxa de Conclusão e Últimos Projetos)
        JPanel conteudoDashboard = new JPanel();
        conteudoDashboard.setLayout(new BoxLayout(conteudoDashboard, BoxLayout.Y_AXIS));
        conteudoDashboard.setBackground(COR_FUNDO);
        conteudoDashboard.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel linhaKpis = new JPanel(new GridLayout(1, 4, 14, 0));
        linhaKpis.setBackground(COR_FUNDO);
        linhaKpis.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaKpis.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel lblKpiTotal = new JLabel("0");
        JLabel lblKpiConcluidos = new JLabel("0");
        JLabel lblKpiDesenvolvimento = new JLabel("0");
        JLabel lblKpiPlanejados = new JLabel("0");

        linhaKpis.add(criarCartaoKpi("Total de Projetos", lblKpiTotal, new Color(33, 150, 243)));
        linhaKpis.add(criarCartaoKpi("Concluídos", lblKpiConcluidos, new Color(40, 167, 69)));
        linhaKpis.add(criarCartaoKpi("Em Desenvolvimento", lblKpiDesenvolvimento, new Color(255, 152, 0)));
        linhaKpis.add(criarCartaoKpi("Planejados", lblKpiPlanejados, new Color(123, 31, 162)));

        JPanel linhaMeioDashboard = new JPanel(new GridLayout(1, 2, 14, 0));
        linhaMeioDashboard.setBackground(COR_FUNDO);
        linhaMeioDashboard.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaMeioDashboard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        JPanel cardDistribuicao = criarCard();
        cardDistribuicao.setLayout(new BoxLayout(cardDistribuicao, BoxLayout.Y_AXIS));
        JLabel tituloDistribuicao = new JLabel("Distribuição por Categoria");
        tituloDistribuicao.setFont(FONTE_SECAO);
        tituloDistribuicao.setForeground(COR_TEXTO);

        JLabel lblDistWeb = new JLabel("Web: 0");
        JLabel lblDistSoftware = new JLabel("Software: 0");
        JLabel lblDistMobile = new JLabel("Mobile: 0");
        JLabel lblDistOutro = new JLabel("Outro: 0");
        for (JLabel lbl : new JLabel[]{lblDistWeb, lblDistSoftware, lblDistMobile, lblDistOutro}) {
            lbl.setFont(FONTE_PADRAO);
            lbl.setForeground(COR_TEXTO);
        }

        cardDistribuicao.add(tituloDistribuicao);
        cardDistribuicao.add(Box.createVerticalStrut(10));
        cardDistribuicao.add(lblDistWeb);
        cardDistribuicao.add(Box.createVerticalStrut(4));
        cardDistribuicao.add(lblDistSoftware);
        cardDistribuicao.add(Box.createVerticalStrut(4));
        cardDistribuicao.add(lblDistMobile);
        cardDistribuicao.add(Box.createVerticalStrut(4));
        cardDistribuicao.add(lblDistOutro);

        JPanel cardTaxaConclusao = criarCard();
        cardTaxaConclusao.setLayout(new BoxLayout(cardTaxaConclusao, BoxLayout.Y_AXIS));
        JLabel tituloTaxa = new JLabel("Taxa de Conclusão");
        tituloTaxa.setFont(FONTE_SECAO);
        tituloTaxa.setForeground(COR_TEXTO);

        JLabel lblPercentualConclusao = new JLabel("0%");
        lblPercentualConclusao.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblPercentualConclusao.setForeground(new Color(40, 167, 69));

        JLabel lblDetalheTaxa = new JLabel("0 de 0 projetos concluídos");
        lblDetalheTaxa.setFont(FONTE_PADRAO);
        lblDetalheTaxa.setForeground(COR_MUTED);

        JProgressBar barraConclusao = new JProgressBar(0, 100);
        barraConclusao.setValue(0);
        barraConclusao.setStringPainted(true);
        barraConclusao.setForeground(new Color(40, 167, 69));
        barraConclusao.setAlignmentX(Component.LEFT_ALIGNMENT);

        cardTaxaConclusao.add(tituloTaxa);
        cardTaxaConclusao.add(Box.createVerticalStrut(10));
        cardTaxaConclusao.add(lblPercentualConclusao);
        cardTaxaConclusao.add(Box.createVerticalStrut(4));
        cardTaxaConclusao.add(lblDetalheTaxa);
        cardTaxaConclusao.add(Box.createVerticalStrut(12));
        cardTaxaConclusao.add(barraConclusao);

        linhaMeioDashboard.add(cardDistribuicao);
        linhaMeioDashboard.add(cardTaxaConclusao);

        JPanel cardUltimosProjetos = criarCard();
        cardUltimosProjetos.setLayout(new BoxLayout(cardUltimosProjetos, BoxLayout.Y_AXIS));
        cardUltimosProjetos.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardUltimosProjetos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));

        JLabel tituloUltimos = new JLabel("Últimos Projetos Cadastrados");
        tituloUltimos.setFont(FONTE_SECAO);
        tituloUltimos.setForeground(COR_TEXTO);

        JPanel listaUltimosProjetos = new JPanel();
        listaUltimosProjetos.setLayout(new BoxLayout(listaUltimosProjetos, BoxLayout.Y_AXIS));
        listaUltimosProjetos.setBackground(COR_CARD);
        listaUltimosProjetos.setAlignmentX(Component.LEFT_ALIGNMENT);

        cardUltimosProjetos.add(tituloUltimos);
        cardUltimosProjetos.add(Box.createVerticalStrut(10));
        cardUltimosProjetos.add(listaUltimosProjetos);

        conteudoDashboard.add(linhaKpis);
        conteudoDashboard.add(Box.createVerticalStrut(14));
        conteudoDashboard.add(linhaMeioDashboard);
        conteudoDashboard.add(Box.createVerticalStrut(14));
        conteudoDashboard.add(cardUltimosProjetos);

        JScrollPane scrollDashboard = new JScrollPane(conteudoDashboard);
        scrollDashboard.setBorder(null);
        scrollDashboard.getViewport().setBackground(COR_FUNDO);

        JPanel abaDashboard = new JPanel(new BorderLayout());
        abaDashboard.setBackground(COR_FUNDO);
        abaDashboard.add(scrollDashboard, BorderLayout.CENTER);

        Runnable atualizarDashboard = () -> {
            int total = modelo.getRowCount();
            int concluido = 0;
            int emDesenvolvimento = 0;
            int planejado = 0;

            int web = 0;
            int software = 0;
            int mobile = 0;
            int outro = 0;

            for (int i = 0; i < total; i++) {
                String status = modelo.getValueAt(i, 3) != null ? modelo.getValueAt(i, 3).toString().trim() : "";
                String categoria = modelo.getValueAt(i, 2) != null ? modelo.getValueAt(i, 2).toString().trim() : "";

                if ("Concluído".equalsIgnoreCase(status) || "Concluido".equalsIgnoreCase(status)) {
                    concluido++;
                } else if ("Em desenvolvimento".equalsIgnoreCase(status)) {
                    emDesenvolvimento++;
                } else if ("Planejado".equalsIgnoreCase(status)) {
                    planejado++;
                }

                if ("Web".equalsIgnoreCase(categoria)) {
                    web++;
                } else if ("Software".equalsIgnoreCase(categoria)) {
                    software++;
                } else if ("Mobile".equalsIgnoreCase(categoria)) {
                    mobile++;
                } else if (!categoria.isEmpty()) {
                    outro++;
                }
            }

            lblKpiTotal.setText(String.valueOf(total));
            lblKpiConcluidos.setText(String.valueOf(concluido));
            lblKpiDesenvolvimento.setText(String.valueOf(emDesenvolvimento));
            lblKpiPlanejados.setText(String.valueOf(planejado));

            lblDistWeb.setText("Web: " + web);
            lblDistSoftware.setText("Software: " + software);
            lblDistMobile.setText("Mobile: " + mobile);
            lblDistOutro.setText("Outro: " + outro);

            int percentual = total > 0 ? Math.round((concluido * 100f) / total) : 0;
            lblPercentualConclusao.setText(percentual + "%");
            lblDetalheTaxa.setText(concluido + " de " + total + " projetos concluídos");
            barraConclusao.setValue(percentual);

            listaUltimosProjetos.removeAll();
            if (total == 0) {
                JLabel vazio = new JLabel("Nenhum projeto cadastrado ainda.");
                vazio.setFont(FONTE_PADRAO);
                vazio.setForeground(COR_MUTED);
                listaUltimosProjetos.add(vazio);
            } else {
                int inicio = Math.max(0, total - 5);
                for (int i = total - 1; i >= inicio; i--) {
                    String idStr = modelo.getValueAt(i, 0) != null ? modelo.getValueAt(i, 0).toString() : "";
                    String nomeStr = modelo.getValueAt(i, 1) != null ? modelo.getValueAt(i, 1).toString() : "";
                    String catStr = modelo.getValueAt(i, 2) != null ? modelo.getValueAt(i, 2).toString() : "";
                    String statusStr = modelo.getValueAt(i, 3) != null ? modelo.getValueAt(i, 3).toString() : "";

                    JLabel item = new JLabel("#" + idStr + " • " + nomeStr + " (" + catStr + " — " + statusStr + ")");
                    item.setFont(FONTE_PADRAO);
                    item.setForeground(COR_TEXTO);
                    listaUltimosProjetos.add(item);
                    listaUltimosProjetos.add(Box.createVerticalStrut(6));
                }
            }

            listaUltimosProjetos.revalidate();
            listaUltimosProjetos.repaint();
        };

        // 4. Aba Relatórios com filtros, Exportar CSV e Imprimir / Salvar PDF
        JPanel abaRelatorios = new JPanel();
        abaRelatorios.setLayout(new BoxLayout(abaRelatorios, BoxLayout.Y_AXIS));
        abaRelatorios.setBackground(COR_FUNDO);
        abaRelatorios.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel cardFiltrosRelatorio = criarCard();
        cardFiltrosRelatorio.setLayout(new BoxLayout(cardFiltrosRelatorio, BoxLayout.Y_AXIS));
        cardFiltrosRelatorio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JLabel tituloFiltros = new JLabel("Filtros do Relatório");
        tituloFiltros.setFont(FONTE_SECAO);
        tituloFiltros.setForeground(COR_TEXTO);
        tituloFiltros.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField campoBuscaRelatorio = new JTextField(20);
        estilizarCampo(campoBuscaRelatorio);

        JComboBox<String> comboFiltroCategoria = new JComboBox<>(
                new String[]{"Todas", "Web", "Software", "Mobile", "Outro"});
        JComboBox<String> comboFiltroStatus = new JComboBox<>(
                new String[]{"Todos", "Planejado", "Em desenvolvimento", "Concluído"});
        estilizarCombo(comboFiltroCategoria);
        estilizarCombo(comboFiltroStatus);

        JButton botaoLimparFiltros = new JButton("Limpar filtros");
        JButton botaoExportarCsv = new JButton("Exportar CSV");
        JButton botaoImprimirPdf = new JButton("Imprimir / Salvar PDF");

        estilizarBotao(botaoLimparFiltros, new Color(108, 117, 125), Color.WHITE);
        estilizarBotao(botaoExportarCsv, new Color(40, 167, 69), Color.WHITE);
        estilizarBotao(botaoImprimirPdf, new Color(33, 150, 243), Color.WHITE);

        JPanel linhaFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        linhaFiltros.setBackground(COR_CARD);
        linhaFiltros.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaFiltros.add(criarRotulo("Buscar:"));
        linhaFiltros.add(campoBuscaRelatorio);
        linhaFiltros.add(criarRotulo("Categoria:"));
        linhaFiltros.add(comboFiltroCategoria);
        linhaFiltros.add(criarRotulo("Status:"));
        linhaFiltros.add(comboFiltroStatus);

        JPanel linhaAcoesRelatorio = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        linhaAcoesRelatorio.setBackground(COR_CARD);
        linhaAcoesRelatorio.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaAcoesRelatorio.add(botaoLimparFiltros);
        linhaAcoesRelatorio.add(botaoExportarCsv);
        linhaAcoesRelatorio.add(botaoImprimirPdf);

        cardFiltrosRelatorio.add(tituloFiltros);
        cardFiltrosRelatorio.add(Box.createVerticalStrut(6));
        cardFiltrosRelatorio.add(linhaFiltros);
        cardFiltrosRelatorio.add(linhaAcoesRelatorio);

        JPanel cardListagemRelatorio = criarCard();
        cardListagemRelatorio.setLayout(new BorderLayout(0, 10));

        JPanel cabecalhoListagem = new JPanel(new BorderLayout());
        cabecalhoListagem.setBackground(COR_CARD);
        JLabel tituloListagem = new JLabel("Listagem Analítica");
        tituloListagem.setFont(FONTE_SECAO);
        tituloListagem.setForeground(COR_TEXTO);

        JLabel lblQuantidadeRegistros = new JLabel("0 registro(s)");
        lblQuantidadeRegistros.setFont(FONTE_PADRAO);
        lblQuantidadeRegistros.setForeground(COR_MUTED);

        cabecalhoListagem.add(tituloListagem, BorderLayout.WEST);
        cabecalhoListagem.add(lblQuantidadeRegistros, BorderLayout.EAST);

        JTable tabelaRelatorio = new JTable(modelo);
        tabelaRelatorio.setRowSorter(sorterRelatorios);
        tabelaRelatorio.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollRelatorio = new JScrollPane(tabelaRelatorio);
        estilizarTabela(tabelaRelatorio, scrollRelatorio);

        cardListagemRelatorio.add(cabecalhoListagem, BorderLayout.NORTH);
        cardListagemRelatorio.add(scrollRelatorio, BorderLayout.CENTER);

        abaRelatorios.add(cardFiltrosRelatorio);
        abaRelatorios.add(Box.createVerticalStrut(16));
        abaRelatorios.add(cardListagemRelatorio);

        Runnable aplicarFiltrosRelatorio = () -> {
            String termoBusca = campoBuscaRelatorio.getText().trim().toLowerCase();
            String catSelecionada = comboFiltroCategoria.getSelectedItem().toString();
            String statusSelecionado = comboFiltroStatus.getSelectedItem().toString();

            sorterRelatorios.setRowFilter(new RowFilter<TableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                    String nome = entry.getStringValue(1) != null ? entry.getStringValue(1).toLowerCase() : "";
                    String categoria = entry.getStringValue(2) != null ? entry.getStringValue(2).trim() : "";
                    String status = entry.getStringValue(3) != null ? entry.getStringValue(3).trim() : "";

                    boolean matchTexto = termoBusca.isEmpty() || nome.contains(termoBusca);
                    boolean matchCategoria = "Todas".equalsIgnoreCase(catSelecionada)
                            || categoria.equalsIgnoreCase(catSelecionada);
                    boolean matchStatus = "Todos".equalsIgnoreCase(statusSelecionado)
                            || status.equalsIgnoreCase(statusSelecionado);

                    return matchTexto && matchCategoria && matchStatus;
                }
            });

            lblQuantidadeRegistros.setText(tabelaRelatorio.getRowCount() + " registro(s)");
        };

        campoBuscaRelatorio.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltrosRelatorio.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltrosRelatorio.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltrosRelatorio.run();
            }
        });

        comboFiltroCategoria.addActionListener(e -> aplicarFiltrosRelatorio.run());
        comboFiltroStatus.addActionListener(e -> aplicarFiltrosRelatorio.run());

        botaoLimparFiltros.addActionListener(e -> {
            campoBuscaRelatorio.setText("");
            comboFiltroCategoria.setSelectedIndex(0);
            comboFiltroStatus.setSelectedIndex(0);
            aplicarFiltrosRelatorio.run();
        });

        botaoExportarCsv.addActionListener(e -> {
            JFileChooser seletor = new JFileChooser();
            seletor.setSelectedFile(new java.io.File("relatorio_projetos.csv"));

            int resultado = seletor.showSaveDialog(this);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                java.io.File arquivo = seletor.getSelectedFile();
                if (!arquivo.getName().toLowerCase().endsWith(".csv")) {
                    arquivo = new java.io.File(arquivo.getAbsolutePath() + ".csv");
                }

                try (java.io.PrintWriter pw = new java.io.PrintWriter(
                        new java.io.OutputStreamWriter(
                                new java.io.FileOutputStream(arquivo),
                                java.nio.charset.StandardCharsets.UTF_8))) {

                    for (int col = 0; col < modelo.getColumnCount(); col++) {
                        if (col > 0) {
                            pw.print(";");
                        }
                        pw.print(escaparCsv(modelo.getColumnName(col)));
                    }
                    pw.println();

                    for (int viewRow = 0; viewRow < tabelaRelatorio.getRowCount(); viewRow++) {
                        int modelRow = tabelaRelatorio.convertRowIndexToModel(viewRow);
                        for (int col = 0; col < modelo.getColumnCount(); col++) {
                            if (col > 0) {
                                pw.print(";");
                            }
                            Object valor = modelo.getValueAt(modelRow, col);
                            pw.print(escaparCsv(valor != null ? valor.toString() : ""));
                        }
                        pw.println();
                    }

                    JOptionPane.showMessageDialog(
                            this,
                            "Relatório exportado com sucesso para:\n" + arquivo.getAbsolutePath(),
                            "Exportação concluída",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Erro ao exportar CSV: " + ex.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        botaoImprimirPdf.addActionListener(e -> {
            try {
                MessageFormat cabecalhoImpressao = new MessageFormat("Relatório de Projetos");
                MessageFormat rodapeImpressao = new MessageFormat("Página {0}");

                boolean concluido = tabelaRelatorio.print(
                        JTable.PrintMode.FIT_WIDTH,
                        cabecalhoImpressao,
                        rodapeImpressao
                );

                if (concluido) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Impressão / exportação enviada com sucesso.",
                            "Relatório",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Erro ao imprimir / salvar PDF: " + ex.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // Abas e subtítulo dinâmico no cabeçalho
        JTabbedPane abas = new JTabbedPane();
        abas.setFont(FONTE_PADRAO);
        abas.setBackground(COR_FUNDO);
        abas.addTab("Projetos", abaProjetos);
        abas.addTab("Categorias", abaCategorias);
        abas.addTab("Dashboard", abaDashboard);
        abas.addTab("Relatórios", abaRelatorios);

        abas.addChangeListener(e -> {
            Component selecionada = abas.getSelectedComponent();
            if (selecionada == abaProjetos) {
                subtituloCabecalho.setText("Cadastro, edição e gestão dos projetos do portfólio");
            } else if (selecionada == abaCategorias) {
                subtituloCabecalho.setText("Exploração e filtragem de projetos por categoria");
                atualizarCategorias.run();
            } else if (selecionada == abaDashboard) {
                subtituloCabecalho.setText("Visão geral e indicadores do portfólio");
                atualizarDashboard.run();
            } else if (selecionada == abaRelatorios) {
                subtituloCabecalho.setText("Relatórios detalhados, filtros e exportação de dados");
                aplicarFiltrosRelatorio.run();
            }
        });

        JPanel painelConteudo = new JPanel(new BorderLayout());
        painelConteudo.setBackground(COR_FUNDO);
        painelConteudo.add(abas, BorderLayout.CENTER);

        add(painelConteudo, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel criarCartaoKpi(String titulo, JLabel lblValor, Color corDestaque) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 4, 1, 1, corDestaque),
            new EmptyBorder(12, 14, 12, 14)
        ));

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValor.setForeground(COR_TEXTO);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(FONTE_PADRAO);
        lblTitulo.setForeground(COR_MUTED);

        card.add(lblValor, BorderLayout.NORTH);
        card.add(lblTitulo, BorderLayout.SOUTH);
        return card;
    }

    private JLabel criarRotulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FONTE_PADRAO);
        label.setForeground(COR_TEXTO);
        return label;
    }

    private String escaparCsv(String texto) {
        if (texto == null) {
            return "";
        }
        String escapado = texto.replace("\"", "\"\"");
        if (escapado.contains(";") || escapado.contains("\"") || escapado.contains("\n") || escapado.contains("\r")) {
            return "\"" + escapado + "\"";
        }
        return escapado;
    }

    public void criarEventos() {
        botaoCadastrar.addActionListener(e -> {
            try {
                cadastrar();
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(this, "Erro: " + e1.getMessage());
            }
        });

        botaoLimpar.addActionListener(e -> limparFormulário());
        botaoExcluir.addActionListener(e -> excluirProjeto());
        botaoAlterar.addActionListener(e -> alterarProjeto());
    }

    public void limparFormulário() {
        campoNome.setText("");
        campoDescricao.setText("");
        comboCategoria.setSelectedIndex(0);
        comboStatus.setSelectedIndex(0);
        campoNome.requestFocus();
    }

    public void cadastrar() {
        try {
            int id = service.gerarId();
            String nome = campoNome.getText().trim();
            String descricao = campoDescricao.getText().trim();
            String categoria = comboCategoria.getSelectedItem().toString();
            String status = comboStatus.getSelectedItem().toString();

            if (nome.isBlank()) {
                JOptionPane.showMessageDialog(this, "Informe o nome do projeto");
                return;
            }

            Projeto projeto = new Projeto(id, nome, descricao, categoria, status);
            if (!service.adicionar(projeto)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Não foi possível cadastrar o projeto (verifique o nome ou ID)."
                );
                return;
            }

            service.salvar();
            JOptionPane.showMessageDialog(this, "Projeto Cadastrado: " + nome);

            limparFormulário();
            carregarTabela();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void excluirProjeto() {
        try {
            int linhaSelecionada = tabela.getSelectedRow();

            if (linhaSelecionada != -1) {
                int modelRow = tabela.convertRowIndexToModel(linhaSelecionada);
                int id = Integer.parseInt(modelo.getValueAt(modelRow, 0).toString());

                int option = JOptionPane.showConfirmDialog(
                        this,
                        "Um projeto será deletado.\nEstá certo disso?",
                        "Aviso",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (option == JOptionPane.YES_OPTION) {
                    apiClient.excluirProjeto(id);
                    service.carregar();
                    carregarTabela();
                    limparFormulário();
                    JOptionPane.showMessageDialog(
                            this,
                            "Projeto excluido com sucesso",
                            "Sucesso",
                            JOptionPane.PLAIN_MESSAGE
                    );
                }
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Por favor, selecione um projeto",
                        "Aviso",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao excluir projeto: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void alterarProjeto() {
        int linhaSelecionada = tabela.getSelectedRow();

        if (linhaSelecionada != -1) {
            try {
                int modelRow = tabela.convertRowIndexToModel(linhaSelecionada);
                int id = Integer.parseInt(modelo.getValueAt(modelRow, 0).toString());
                String nome = campoNome.getText().trim();
                String descricao = campoDescricao.getText().trim();
                String categoria = comboCategoria.getSelectedItem().toString();
                String status = comboStatus.getSelectedItem().toString();

                if (nome.isBlank()) {
                    JOptionPane.showMessageDialog(this, "Informe o nome do projeto", "Aviso",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                int option = JOptionPane.showConfirmDialog(this, "Um projeto será alterado.\nEstá certo disso?",
                        "Aviso", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (option == JOptionPane.YES_OPTION) {
                    Projeto projetoAtualizado = new Projeto(id, nome, descricao, categoria, status);

                    apiClient.alterarProjeto(id, projetoAtualizado);
                    service.carregar();
                    carregarTabela();
                    limparFormulário();

                    JOptionPane.showMessageDialog(this, "Projeto alterado com sucesso", "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Não foi possível alterar o projeto:\n" + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um projeto", "Aviso",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void carregarTabela() {
        modelo.setRowCount(0);
        for (Projeto p : service.listar()) {
            modelo.addRow(new Object[]{
                p.getId(),
                p.getNome(),
                p.getCategoria(),
                p.getStatus()
            });
        }
    }

    public void atualizarTabela(List<Projeto> projetos) {
        modelo.setRowCount(0);
        for (Projeto projeto : projetos) {
            modelo.addRow(new Object[]{
                projeto.getId(),
                projeto.getNome(),
                projeto.getCategoria(),
                projeto.getStatus()
            });
        }
    }

    public static void main(String[] args) throws Exception {
        Api.main(args);
        TelaPrincipal tela = new TelaPrincipal();
        tela.setVisible(true);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> SwingUtilities.invokeLater(() -> {
            try {
                if (tela.tabela.getSelectedRow() == -1) {
                    tela.service.carregar();
                    tela.carregarTabela();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        scheduler.scheduleAtFixedRate(task, 5, 5, TimeUnit.SECONDS);
    }

    private JPanel criarCard() {
        JPanel card = new JPanel();
        card.setBackground(COR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COR_BORDA, 1, true),
            new EmptyBorder(18, 20, 18, 20)
        ));
        return card;
    }

    private void estilizarCampo(JTextField campo) {
        campo.setFont(FONTE_PADRAO);
        campo.setForeground(COR_TEXTO);
        campo.setBackground(Color.WHITE);
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COR_BORDA, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
    }

    private void estilizarCombo(JComboBox<?> combo) {
        combo.setFont(FONTE_PADRAO);
        combo.setForeground(COR_TEXTO);
        combo.setBackground(Color.WHITE);
    }

    private void estilizarBotao(JButton botao, Color fundo, Color texto) {
        botao.setFont(FONTE_BOTAO);
        botao.setBackground(fundo);
        botao.setForeground(texto);
        botao.setFocusPainted(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(fundo.darker(), 1, true),
            new EmptyBorder(8, 16, 8, 16)
        ));
    }

    private JPanel criarCabecalho() {
        JPanel topo = new JPanel();
        topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));
        topo.setBackground(COR_TOPO);
        topo.setBorder(new EmptyBorder(20, 24, 16, 24));

        JLabel titulo = new JLabel("Sistema de Projetos");
        titulo.setFont(FONTE_TITULO);
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        subtituloCabecalho = new JLabel("Cadastro, edição e gestão dos projetos do portfólio");
        subtituloCabecalho.setFont(FONTE_SUBTITULO);
        subtituloCabecalho.setForeground(new Color(207, 216, 220));
        subtituloCabecalho.setAlignmentX(Component.CENTER_ALIGNMENT);

        topo.add(titulo);
        topo.add(Box.createVerticalStrut(4));
        topo.add(subtituloCabecalho);
        return topo;
    }

    private void estilizarTabela(JTable tabela, JScrollPane scroll) {
        tabela.setFont(FONTE_PADRAO);
        tabela.setForeground(COR_TEXTO);
        tabela.setBackground(Color.WHITE);
        tabela.setRowHeight(28);
        tabela.setGridColor(COR_BORDA);
        tabela.setSelectionBackground(new Color(227, 242, 253));
        tabela.setSelectionForeground(COR_TEXTO);

        if (tabela.getTableHeader() != null) {
            tabela.getTableHeader().setFont(FONTE_BOTAO);
            tabela.getTableHeader().setBackground(new Color(248, 249, 250));
            tabela.getTableHeader().setForeground(COR_TEXTO);
        }

        if (scroll != null) {
            scroll.getViewport().setBackground(Color.WHITE);
            scroll.setBorder(BorderFactory.createLineBorder(COR_BORDA, 1, true));
        }
    }
}