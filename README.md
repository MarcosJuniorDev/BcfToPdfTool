# BCF to PDF Converter (`BcfToPdfTool`)

Uma ferramenta em Java rápida, leve e extensível para conversão de arquivos **BCF (BIM Collaboration Format)** em relatórios **PDF** formatados, com suporte tanto para interface gráfica com Arraste e Solte (Drag & Drop em Java Swing) quanto via Linha de Comando (CLI).

---

## 📦 Downloads e Instalação (Releases)

Você pode baixar os instaladores prontos diretamente na aba [**Releases do GitHub**](../../releases):

- **Windows (`.exe`)**: `BcfToPdfTool-x.x.x-windows-x64-setup.exe` (Instalador com atalho na Área de Trabalho e Menu Iniciar).
- **Debian / Ubuntu / Mint (`.deb`)**: `bcftopdf-x.x.x-linux-amd64.deb`
- **Fedora / RHEL / openSUSE (`.rpm`)**: `bcftopdf-x.x.x-linux-x86_64.rpm`
- **Multiplataforma (`.jar`)**: `BcfToPdfTool-1.0.0.jar` (requer Java 25+).

> *Os pacotes nativos (`.exe`, `.deb` e `.rpm`) já incluem seu próprio runtime Java encapsulado via `jpackage`, **não exigindo** que você tenha o Java instalado na máquina.*

### Instalando no Linux:

```bash
# Ubuntu / Debian
sudo dpkg -i bcftopdf-1.0.0-linux-amd64.deb

# Fedora / Red Hat / CentOS
sudo rpm -i bcftopdf-1.0.0-linux-x86_64.rpm
```

---

## 📋 Funcionalidades

- **Multi-versão BCF**: Suporte completo a arquivos nas versões BCF 2.0, 2.1 e 3.0 (padrões buildingSMART, Trimble Connect, Solibri, Revit, BIMCollab, etc.).
- **Múltiplos formatos de entrada**:
  - Arquivos compactados `.bcf`, `.bcfzip` ou `.zip`.
  - Pastas/diretórios descompactados contendo arquivos de tópicos BCF (`markup.bcf`, `markup.xml`, etc.).
- **Geração de PDF Completa (Apache PDFBox)**:
  - Cabeçalho do projeto e metadados de exportação.
  - Tópicos detalhados com título, descrição, datas, autores e status.
  - Seção de Coordenação (*Created By*, *Assigned To*, *Priority*, *Status*, *Type*, *Due Date*, *Tags*).
  - Comentários e histórico de discussões em ordem cronológica.
  - Visualizações e snapshots/fotos do modelo BIM.
  - Quebra de página inteligente (anti-órfão).
  - Marcadores / Bookmarks (PDF Outline) para navegação rápida entre os tópicos.
  - Suporte completo a acentuação e caracteres Unicode (fontes Noto Sans embutidas).
- **Interface Gráfica Intuitiva (Swing)**:
  - Área de "Arraste e Solte" (Drag & Drop) para carregar arquivos instantaneamente.
  - Diálogo automático para escolher o local e o nome do arquivo PDF final.
  - Conversão em segundo plano (sem travamento de tela) com feedback visual.
- **Interface de Linha de Comando (CLI)**:
  - Facilmente integrável em pipelines CI/CD, servidores ou scripts automatizados.

---

## 🚀 Como Utilizar

### 1. Modo Gráfico (GUI)

- **Instalador Windows / Linux**: Basta abrir o aplicativo através do atalho no **Menu Iniciar** ou na **Área de Trabalho**.
- **Arquivo `.jar`**: Dê um **duplo clique** no `.jar` ou execute no terminal:
  ```bash
  java -jar BcfToPdfTool-1.0.0.jar
  ```

> Arraste qualquer arquivo `.bcf`, `.bcfzip` ou pasta BCF para dentro da janela pontilhada e escolha onde salvar o PDF gerado.

---

### 2. Modo Linha de Comando (CLI)

#### Se estiver usando o comando instalado nativamente no Linux (`bcftopdf`):
```bash
# Conversão direta (salva arquivo.pdf na mesma pasta)
bcftopdf arquivo.bcf.zip

# A partir de pasta descompactada
bcftopdf pasta_do_bcf/

# Especificando caminho de saída
bcftopdf arquivo.bcf.zip -o relatorio.pdf
```

#### Se estiver usando o arquivo `.jar`:
```bash
# Conversão padrão
java -jar BcfToPdfTool-1.0.0.jar arquivo.bcf.zip

# Especificando o destino do PDF
java -jar BcfToPdfTool-1.0.0.jar arquivo.bcf.zip -o /caminho/relatorio.pdf

# Ajuda e Versão
java -jar BcfToPdfTool-1.0.0.jar --help
java -jar BcfToPdfTool-1.0.0.jar --version
```

---

## 🔨 Como Compilar a partir do Código-Fonte (Build)

### Pré-requisitos:
- **Java JDK 25** ou superior
- **Apache Maven 3.8+**

### Gerar o JAR executável:
```bash
mvn clean package
```
O arquivo único executável será gerado em `target/BcfToPdfTool-1.0.0.jar`.

### Gerar os instaladores nativos manualmente (opcional):
```bash
# Pacote DEB (Linux Debian/Ubuntu)
jpackage --type deb --input target --name bcftopdf --main-jar BcfToPdfTool-1.0.0.jar --main-class com.bcf.Main

# Pacote RPM (Linux Fedora/RHEL)
jpackage --type rpm --input target --name bcftopdf --main-jar BcfToPdfTool-1.0.0.jar --main-class com.bcf.Main

# Instalador EXE (Windows)
jpackage --type exe --input target --name BcfToPdfTool --main-jar BcfToPdfTool-1.0.0.jar --main-class com.bcf.Main --win-shortcut --win-menu
```

---

## 🏷️ Como Fazer um Release no GitHub

O workflow do GitHub Actions em [`.github/workflows/release.yml`](.github/workflows/release.yml) é acionado automaticamente ao criar uma tag de versão:

```bash
git tag v1.0.0
git push origin v1.0.0
```

O GitHub Actions irá:
1. Compilar o projeto no Linux e no Windows utilizando o JDK 25.
2. Empacotar os instaladores `.exe`, `.deb` e `.rpm`.
3. Criar uma nova Release pública no GitHub e anexar os instaladores automaticamente.

*(Você também pode acionar o workflow manualmente na aba **Actions** do GitHub via **Run workflow**).*

---

## 🧪 Executando os Testes

Para rodar todos os testes de leitura BCF e renderização de PDF:

```bash
mvn test
```

---

## 📄 Licença

Este projeto está licenciado sob a licença open-source **GNU General Public License v2.0 (GPL-2.0)**. Consulte o arquivo [LICENSE](LICENSE) para o texto integral da licença.

---

## 🤖 Disclaimer de IA

> **Aviso**: Este projeto foi desenvolvido com assistência de Inteligência Artificial (IA).
