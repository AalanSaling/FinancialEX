# FinancialEX 💎 - Gestão Financeira Pessoal & Investimentos

**FinancialEX** é um aplicativo Android completo, seguro e moderno de finanças pessoais desenvolvido com **Kotlin**, **Jetpack Compose** e **Material Design 3**. Com foco em privacidade e segurança, todos os dados são armazenados exclusivamente **offline** no próprio dispositivo do usuário através de um banco de dados **Room (SQLite)**.

---

## 🔥 Funcionalidades Principais e Como Utilizar

### 🔒 1. Gaiola de Segurança (Biometria & PIN de 4 Dígitos)
- **O que faz**: Bloqueia a exibição de qualquer dado financeiro até que a identidade do usuário seja confirmada.
- **Como usar**: Ao abrir o aplicativo pela primeira vez, cadastre um PIN de 4 dígitos. Se o seu smartphone possuir leitor de impressão digital ou reconhecimento facial, a autenticação biométrica será solicitada automaticamente.

### 📊 2. Dashboard Patrimonial & Fluxo de Caixa
- **O que faz**: Oferece uma visão panorâmica e imediata do seu patrimônio consolidado, receitas do mês, despesas e saldo disponível.
- **Como usar**: Visualize gráficos dinâmicos de barras e rosca para entender onde seu dinheiro está sendo gasto por categoria (Alimentação, Moradia, Lazer, Transporte, etc.).

### 🎯 3. Gestão de Metas Financeiras com Histórico
- **O que faz**: Permite criar objetivos de economia (ex: *Viagem*, *Reserva de Emergência*, *Carro Novo*) com barras de progresso e datas limite.
- **Como usar**: Dentro de cada meta, clique no botão **Histórico** para registrar cada aporte ou resgate realizado. O aplicativo calcula automaticamente a porcentagem concluída e o valor restante.

### 📈 4. Carteira de Investimentos & Calculadora de Juros Compostos
- **O que faz**: Acompanhe ativos em Renda Fixa, Ações, FIIs, Criptomoedas ou Poupança.
- **Como usar**: Registre aportes, resgates e proventos/rendimentos no histórico do ativo. Utilize a aba **Juros Compostos** para simular a evolução do seu patrimônio ao longo dos anos com aportes mensais e taxa de juros personalizável.

### 🧾 5. Lembretes de Contas & Faturas (Bill Reminders)
- **O que faz**: Evita o pagamento de multas e juros lembrando das contas a vencer no mês (Luz, Água, Cartão de Crédito, Aluguel).
- **Como usar**: Cadastre a conta, o valor e o dia de vencimento. Marque como **Paga** com um clique assim que efetuar o pagamento.

### 💱 6. Conversor de Moedas com Cotação em Tempo Real
- **O que faz**: Converte valores entre diversas moedas globais (USD, BRL, EUR, GBP, JPY, BTC, etc.) com taxas de câmbio atualizadas.
- **Como usar**: Acesse a aba **Moedas**, escolha a moeda de origem e destino e digite o valor para conversão instantânea.

### 📄 7. Relatórios Financeiros & Exportação em PDF
- **O que faz**: Gera documentos formais de extrato e balanço financeiro.
- **Como usar**: Acesse a seção de relatórios, escolha o período e exporte um arquivo PDF diretamente para o armazenamento do seu dispositivo.

### 🌍 8. Suporte Multi-idioma Nativo
- Suporte completo e dinâmico aos idiomas: **Português (Brasil)**, **Espanhol** e **Inglês**.
- Troque de idioma instantaneamente no menu de configurações do aplicativo.

---

## 🛠️ Arquitetura & Tecnologias Utilizadas

- **Linguagem**: Kotlin 100%
- **Interface Gráfica**: Jetpack Compose (Material Design 3)
- **Banco de Dados Local**: Room Database (SQLite)
- **Arquitetura**: MVVM (Model-View-ViewModel) + StateFlow / Coroutines
- **Autenticação**: AndroidX Biometric API
- **Conectividade de Câmbio**: Retrofit2 / OkHttp (Consultas read-only de cotação pública)

---

## 🔐 Privacidade e Segurança dos Dados

- **Zero Cloud / Zero Tracking**: Suas transações, saldos, senhas e informações pessoais **nunca** saem do seu celular.
- **Sem Anúncios e Sem Coleta de Dados**: O banco de dados fica cifrado no armazenamento privado do aplicativo.

---

*Desenvolvido com o Google AI Studio.* 🚀
