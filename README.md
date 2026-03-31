# 🐾 Hyvel Pets (Sistema Avançado de Pets)

![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=java)
![Bukkit/Spigot](https://img.shields.io/badge/API-Spigot%2FPaper-lightgrey?style=for-the-badge)
![Economy](https://img.shields.io/badge/Economy-yEconomias-green?style=for-the-badge)
![Database](https://img.shields.io/badge/Database-SQLite%20%7C%20MySQL-blue?style=for-the-badge)

Um plugin de Pets de última geração para servidores de Minecraft. Muito mais do que simples cosméticos, este plugin introduz um ecossistema completo de bónus, evolução, economia rotativa e um sistema de leilões dinâmico em tempo real.

---

## ✨ Funcionalidades Principais

### 👻 Visual e Física Realista
* **Follow Engine:** Os pets seguem o jogador de forma suave usando interpolação vetorial.
* **Efeito "Respiração":** Flutuação orgânica usando funções matemáticas (`Math.sin()`).
* **Hologramas Integrados:** O nome do pet flutua perfeitamente acima da sua cabeça sem atrapalhar a hitbox do jogador (uso de entidades duplas).
* **Cross-World:** Teleporte automático e inteligente entre dimensões.

### 🎒 Coleção e Evolução
* **Sistema de Coleção:** Menus interativos para visualizar pets desbloqueados e bloqueados.
* **Guarda-Roupa:** Equipe acessórios (chapéus, colares) nos pets para conceder **Bónus Permanentes** ao jogador.
* **Alimentação (Buffs Temporários):** Dê comidas especiais aos pets para ativar multiplicadores por tempo limitado (ex: +50% XP por 60 minutos).

### 🏪 Economia e Lojas
* **Loja Rotativa (Estilo Hypixel):** A loja atualiza o seu catálogo de Roupas e Comidas a cada *X* minutos, gerando escassez e retorno diário dos jogadores. Tudo configurável via `config.yml`.
* **Casa de Leilões (Auction House):** * Leilões de itens entre jogadores (Coins ou Cash).
  * Menus paginados (até 45 itens por página) com setas de navegação.
  * **Lances via Chat:** Sistema seguro anti-spam e anti-duplicação.
* **Integração Assíncrona:** Suporte total à API do **yEconomias** da yStore. Os pagamentos e devoluções (refunds) de lances são processados em segundo plano, garantindo **Zero Lag** no servidor.

### ⚙️ Motor de Bónus (Regex)
Um sistema de cálculo de status dinâmico. O `PetBonusManager` lê as descrições (*Lore*) dos pets, roupas e comidas e soma automaticamente os multiplicadores (ex: Dano, Mineração, XP) usando Expressões Regulares.

---

## 💻 Comandos e Permissões

| Comando | Descrição | Permissão |
| :--- | :--- | :--- |
| `/pets` | Abre o menu principal de gestão de pets. | `@Nenhuma` |
| `/pets leilao <lance> <tempo> <moeda>` | Inicia um leilão com o item da mão. | `@Nenhuma` |
| `/pets give <jogador> <pet_id>` | Dá um pet em forma de item consumível. | `hyvel.pets.admin` |
| `/pets reset <jogador>` | Limpa a base de dados de pets de um jogador. | `hyvel.pets.admin` |

**Permissões de Slots (Máx: 4 pets equipados em simultâneo):**
* `hyvel.pets.2` - Liberta o 2º Slot
* `hyvel.pets.3` - Liberta o 3º Slot
* `hyvel.pets.4` - Liberta o 4º Slot

---

## 🛠️ Configuração (`config.yml`)

O plugin permite adicionar novos itens rotativos de forma extremamente fácil sem precisar de mexer no código Java.

```yaml
loja:
  tempo_rotacao_minutos: 60
  quantidade_roupas_venda: 3
  quantidade_comidas_venda: 5

roupas:
  CHAPEU_MINEIRO:
    nome: "&eChapéu de Mineiro"
    textura: "eyJ0ZXh0dXJlcyI..."
    raridade: "&7Comum"
    preco: 5000.0
    bonus:
      - "&7- &f+5% de Sorte na Mineração"

comidas:
  MACA_OURO:
    nome: "&eMaçã Dourada Brilhante"
    textura: "eyJ0ZXh0dXJlcyI..."
    raridade: "&6Lendário"
    duracao_minutos: 60
    preco: 15000.0
    bonus:
      - "&7- &f+50% de XP ganho"
```


## 📦 Dependências

Para que o plugin funcione corretamente a 100%, é necessário ter os seguintes plugins instalados no servidor:

```
yEconomias (ou equivalente configurado na API) para a gestão financeira de Coins e Cash.
```

 ## 💾 Banco de Dados
 
O plugin cria as suas tabelas automaticamente na inicialização. Suporta SQLite (local) e MySQL.
Tabelas geridas:

```
unlocked_pets, equipped_pets, visual_pets

unlocked_clothing, equipped_clothing

unlocked_food, active_food_buffs
```

Desenvolvido com ☕ e ❤️ para elevar a economia e o RPG do servidor.
